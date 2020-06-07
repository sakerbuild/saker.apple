/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
#include <saker_apple_impl_plist_lib_Plist.h>
#include <CoreFoundation/CoreFoundation.h>
#include <utility>

template<typename T>
class CFReference {
private:
	template<typename >
	friend class CFReference;

	bool owned = TRUE;
	T _ref;
public:
	CFReference(T ref = NULL) :
			_ref(ref) {
	}
	CFReference(T ref, bool owned) :
			_ref(ref), owned(owned) {
	}
	template<typename E>
	CFReference(CFReference<E> &&o) :
			_ref(o._ref) {
		o._ref = NULL;
	}
	~CFReference() {
		if (owned && _ref != NULL) {
			CFRelease(_ref);
		}
	}
	T operator->() {
		return _ref;
	}
	T* operator&() {
		return &_ref;
	}
	operator T() {
		return _ref;
	}

	bool operator==(decltype(nullptr)) const {
		return _ref == NULL;
	}

	T ref() {
		return _ref;
	}
};

class PlistImpl {
private:
public:
	CFReference<CFPropertyListRef> propertyList;
	CFPropertyListFormat format;

	PlistImpl(CFPropertyListRef propertyList, CFPropertyListFormat format) :
			propertyList(propertyList), format(format) {
	}

	operator CFPropertyListRef() {
		return propertyList;
	}
};

static void javaException(JNIEnv *env, const char *type, const char *message) {
	jclass jc = env->FindClass(type);
	env->ThrowNew(jc, message);
}
static CFReference<CFStringRef> toCFString(JNIEnv *env, jstring string) {
	if (string == NULL) {
		return NULL;
	}
	const jchar *raw = env->GetStringChars(string, 0);
	jsize len = env->GetStringLength(string);
	CFReference<CFStringRef> result = CFStringCreateWithCharacters(NULL, reinterpret_cast<const UniChar*>(raw), len);
	env->ReleaseStringChars(string, raw);
	return std::move(result);
}
static CFReference<CFNumberRef> toCFObject(JNIEnv *env, jdouble value) {
	double val = value;
	CFReference<CFNumberRef> ref = CFNumberCreate(NULL, kCFNumberDoubleType, &val);
	if (ref == nullptr) {
		//TODO reify
		javaException(env, "java/lang/RuntimeException", "Failed to create CFNumber for double.");
		return nullptr;
	}
	return std::move(ref);
}
static CFReference<CFNumberRef> toCFObject(JNIEnv *env, jlong value) {
	long long val = value;
	CFReference<CFNumberRef> ref = CFNumberCreate(NULL, kCFNumberLongLongType, &val);
	if (ref == nullptr) {
		//TODO reify
		javaException(env, "java/lang/RuntimeException", "Failed to create CFNumber for long.");
		return nullptr;
	}
	return std::move(ref);
}

struct CFConversionContext {
	JNIEnv *env;
	jclass strclass = NULL;
	jclass longclass = NULL;
	jclass doubleclass = NULL;
	jclass booleanclass = NULL;
	jclass mapclass = NULL;
	jclass objarrayclass = NULL;
	jclass setclass = NULL;
	jclass iteratorclass = NULL;
	jclass entryclass = NULL;

	CFConversionContext(JNIEnv *env) :
			env(env) {
	}
	operator JNIEnv*() {
		return env;
	}
};

static jclass getClass(JNIEnv *env, jclass *c, const char *classname) {
	if (*c == NULL) {
		*c = env->FindClass(classname);
		if (c == NULL) {
			//an exception is thrown automatically
			return NULL;
		}
	}
	return *c;
}

static bool isObjectInstanceOf(JNIEnv *env, jclass *c, const char *classname, jobject obj) {
	jclass cc = getClass(env, c, classname);
	if (cc == NULL) {
		return false;
	}
	return env->IsInstanceOf(obj, cc);
}

static CFReference<CFTypeRef> toCFObject(CFConversionContext &cc, jobject o);
static CFReference<CFTypeRef> toCFObject(JNIEnv *env, jobject o) {
	CFConversionContext cc(env);
	return toCFObject(cc, o);
}
static CFReference<CFTypeRef> toCFObject(CFConversionContext &cc, jobjectArray value) {
	JNIEnv *env = cc.env;
	jsize len = env->GetArrayLength(value);
	CFMutableArrayRef arrayref = CFArrayCreateMutable(NULL, len, &kCFTypeArrayCallBacks);
	if (arrayref == NULL) {
		javaException(env, "java/lang/RuntimeException", "Failed to create CFArray.");
		return nullptr;
	}
	for (jsize i = 0; i < len; ++i) {
		jobject o = env->GetObjectArrayElement(value, i);
		auto cfobj = toCFObject(cc, o);
		env->DeleteLocalRef(o);
		if (cfobj == nullptr) {
			return nullptr;
		}
		CFArraySetValueAtIndex(arrayref, i, cfobj);
	}
	return arrayref;
}
static CFReference<CFTypeRef> toCFObject(CFConversionContext &cc, jobject o) {
	JNIEnv *env = cc.env;
	if (o == NULL) {
		javaException(env, "java/lang/NullPointerException", "Null plist element.");
		return nullptr;
	}
	if (isObjectInstanceOf(env, &cc.strclass, "java/lang/String", o)) {
		return toCFString(env, (jstring) o);
	}
	if (isObjectInstanceOf(env, &cc.longclass, "java/lang/Long", o)) {
		static jmethodID valuemethod = env->GetMethodID(cc.longclass, "longValue", "()J");
		jlong val = env->CallLongMethod(o, valuemethod);
		return toCFObject(cc, val);
	}
	if (isObjectInstanceOf(env, &cc.doubleclass, "java/lang/Double", o)) {
		static jmethodID valuemethod = env->GetMethodID(cc.doubleclass, "doubleValue", "()D");
		jdouble val = env->CallDoubleMethod(o, valuemethod);
		return toCFObject(cc, val);
	}
	if (isObjectInstanceOf(env, &cc.booleanclass, "java/lang/Boolean", o)) {
		static jmethodID valuemethod = env->GetMethodID(cc.booleanclass, "booleanValue", "()Z");
		jboolean val = env->CallBooleanMethod(o, valuemethod);
		//not owned
		return CFReference<CFBooleanRef>(val ? kCFBooleanTrue : kCFBooleanFalse, false);
	}
	if (isObjectInstanceOf(env, &cc.objarrayclass, "[Ljava/lang/Object;", o)) {
		jobjectArray array = (jobjectArray) o;
		return toCFObject(cc, array);
	}
	if (isObjectInstanceOf(env, &cc.mapclass, "java/util/Map", o)) {
		static jmethodID sizemethod = env->GetMethodID(cc.mapclass, "size", "()I");
		jint size = env->CallIntMethod(o, sizemethod);
		if (env->ExceptionCheck()) {
			return nullptr;
		}
		CFMutableDictionaryRef ref = CFDictionaryCreateMutable(NULL, size, &kCFTypeDictionaryKeyCallBacks,
				&kCFTypeDictionaryValueCallBacks);
		if (ref == NULL) {
			javaException(env, "java/lang/RuntimeException", "Failed to create CFDictionary for plist.");
			return nullptr;
		}
		static jmethodID entrySetMethod = env->GetMethodID(cc.mapclass, "entrySet", "()Ljava/util/Set;");
		jobject entryset = env->CallObjectMethod(o, entrySetMethod);
		if (entryset == nullptr) {
			if (env->ExceptionCheck()) {
				return nullptr;
			}
			javaException(env, "java/lang/NullPointerException", "Null entry set.");
			return nullptr;
		}
		static jmethodID iteratorMethod = env->GetMethodID(getClass(env, &cc.setclass, "java/util/Set"), "iterator",
				"()Ljava/util/Iterator;");
		jobject iterator = env->CallObjectMethod(entryset, iteratorMethod);
		if (iterator == NULL) {
			if (env->ExceptionCheck()) {
				return nullptr;
			}
			javaException(env, "java/lang/NullPointerException", "Null iterator.");
			return nullptr;
		}
		env->DeleteLocalRef(entryset);
		static jmethodID hasNextMethod = env->GetMethodID(getClass(env, &cc.iteratorclass, "java/util/Iterator"),
				"hasNext", "()Z");
		while (env->CallBooleanMethod(iterator, hasNextMethod)) {
			static jmethodID nextMethod = env->GetMethodID(getClass(env, &cc.iteratorclass, "java/util/Iterator"),
					"next", "()Ljava/lang/Object;");
			//Entry<?, ?>
			jobject entry = env->CallObjectMethod(iterator, nextMethod);
			if (entry == NULL) {
				if (env->ExceptionCheck()) {
					return nullptr;
				}
				javaException(env, "java/lang/NullPointerException", "Null plist entry.");
				return nullptr;
			}

			static jmethodID getKeyMethod = env->GetMethodID(getClass(env, &cc.entryclass, "java/util/Map$Entry"),
					"getKey", "()Ljava/lang/Object;");
			static jmethodID getValueMethod = env->GetMethodID(getClass(env, &cc.entryclass, "java/util/Map$Entry"),
					"getValue", "()Ljava/lang/Object;");
			jstring key = (jstring) env->CallObjectMethod(entry, getKeyMethod);
			if (key == NULL) {
				if (env->ExceptionCheck()) {
					return nullptr;
				}
				javaException(env, "java/lang/NullPointerException", "Null key.");
				return nullptr;
			}
			jobject value = env->CallObjectMethod(entry, getValueMethod);
			if (value == NULL) {
				if (env->ExceptionCheck()) {
					return nullptr;
				}
				javaException(env, "java/lang/NullPointerException", "Null value.");
				return nullptr;
			}
			auto cfkey = toCFString(env, key);
			env->DeleteLocalRef(key);
			auto cfval = toCFObject(cc, value);
			env->DeleteLocalRef(value);
			if (cfkey == nullptr || cfval == nullptr) {
				return nullptr;
			}

			CFDictionarySetValue(ref, cfkey, cfval);

			env->DeleteLocalRef(entry);
		}
		if (env->ExceptionCheck()) {
			return nullptr;
		}
		return ref;
	}
	javaException(env, "java/lang/IllegalArgumentException", "Unrecognized Java plist type.");
	return nullptr;
}

JNIEXPORT jlong JNICALL Java_saker_apple_impl_plist_lib_Plist_createEmptyPlist(JNIEnv *env, jclass clazz) {
	CFMutableDictionaryRef ref = CFDictionaryCreateMutable(NULL, 0, &kCFTypeDictionaryKeyCallBacks,
			&kCFTypeDictionaryValueCallBacks);
	if (ref == NULL) {
		javaException(env, "java/lang/RuntimeException", "Failed to create CFDictionary for plist.");
		return NULL;
	}
	return reinterpret_cast<jlong>(new PlistImpl(ref, kCFPropertyListXMLFormat_v1_0));
}
JNIEXPORT jlong JNICALL Java_saker_apple_impl_plist_lib_Plist_createFromBytes(JNIEnv *env, jclass clazz,
		jbyteArray bytes, jint offset, jint length) {
	jbyte *nativebytes = env->GetByteArrayElements(bytes, NULL);
	if (nativebytes == NULL) {
		javaException(env, "java/io/IOException", "Failed to retrieve pointer to byte array.");
		return NULL;
	}
	CFDataRef cfdata = CFDataCreateWithBytesNoCopy(NULL, reinterpret_cast<const UInt8*>(nativebytes + offset), length,
			kCFAllocatorNull);
	//abort is used because we don't need to copy bytes back as we didn't modify them
	env->ReleaseByteArrayElements(bytes, nativebytes, JNI_ABORT);
	if (cfdata == NULL) {
		javaException(env, "java/io/IOException", "Failed to create CFData for the byte array.");
		return NULL;
	}
	CFPropertyListFormat format;
	CFReference<CFErrorRef> error;
	CFPropertyListRef proplistref = CFPropertyListCreateWithData(NULL, cfdata,
			kCFPropertyListMutableContainersAndLeaves, &format, &error);
	if (proplistref == NULL) {
		//TODO include error description
		javaException(env, "java/io/IOException", "Failed to parse property list.");
		return NULL;
	}
	if (CFGetTypeID(proplistref) != CFDictionaryGetTypeID()) {
		CFRelease(proplistref);
		//TODO description
		javaException(env, "java/lang/IllegalArgumentException", "Plist content is not a dictionary.");
		return NULL;
	}
	return reinterpret_cast<jlong>(new PlistImpl(proplistref, format));
}

JNIEXPORT jbyteArray JNICALL Java_saker_apple_impl_plist_lib_Plist_serialize(JNIEnv *env, jclass clazz, jlong ptr,
		jint format) {
	PlistImpl &plist = *reinterpret_cast<PlistImpl*>(ptr);
	CFPropertyListFormat plformat;
	switch (format) {
		case Java_const_saker_apple_impl_plist_lib_Plist_FORMAT_SAME_AS_INPUT: {
			plformat = plist.format;
			break;
		}
		case Java_const_saker_apple_impl_plist_lib_Plist_FORMAT_XML: {
			plformat = kCFPropertyListXMLFormat_v1_0;
			break;
		}
		case Java_const_saker_apple_impl_plist_lib_Plist_FORMAT_BINARY: {
			plformat = kCFPropertyListBinaryFormat_v1_0;
			break;
		}
		default: {
			// TODO include the format
			javaException(env, "java/lang/IllegalArgumentException", "Invalid plist format specified.");
			return NULL;
		}
	}

	CFReference<CFErrorRef> error;
	CFReference<CFDataRef> cfdata = CFPropertyListCreateData(NULL, plist.propertyList, plformat, 0, &error);
	if (cfdata == nullptr) {
		//TODO reify
		javaException(env, "java/lang/IllegalArgumentException", "Failed to serialize plist.");
		return NULL;
	}
	unsigned int len = CFDataGetLength(cfdata);

	jbyteArray result = env->NewByteArray(len);
	if (result == NULL) {
		//TODO include count
		javaException(env, "java/lang/OutOfMemoryError", "Failed to allocate memory for plist bytes.");
		return NULL;
	}
	env->SetByteArrayRegion(result, 0, len, reinterpret_cast<const jbyte*>(CFDataGetBytePtr(cfdata)));
	return result;
}

JNIEXPORT void JNICALL Java_saker_apple_impl_plist_lib_Plist_setStringKeyValue(
		JNIEnv* env, jclass clazz, jlong ptr, jstring key, jstring value) {
	PlistImpl& plist = *reinterpret_cast<PlistImpl*>(ptr);
	CFDictionarySetValue((CFMutableDictionaryRef) plist.propertyList.ref(), toCFString(env, key), toCFString(env, value));
}

JNIEXPORT void JNICALL Java_saker_apple_impl_plist_lib_Plist_setBooleanKeyValue(
		JNIEnv* env, jclass clazz, jlong ptr, jstring key, jboolean value) {
	PlistImpl& plist = *reinterpret_cast<PlistImpl*>(ptr);
	CFDictionarySetValue((CFMutableDictionaryRef) plist.propertyList.ref(), toCFString(env, key),
			value ? kCFBooleanTrue : kCFBooleanFalse);
}

JNIEXPORT void JNICALL Java_saker_apple_impl_plist_lib_Plist_setLongKeyValue(
		JNIEnv* env, jclass clazz, jlong ptr, jstring key, jlong value) {
	auto ref = toCFObject(env, value);
	if (ref == nullptr) {
		return;
	}
	PlistImpl& plist = *reinterpret_cast<PlistImpl*>(ptr);
	CFDictionarySetValue((CFMutableDictionaryRef) plist.propertyList.ref(), toCFString(env, key), ref);
}

JNIEXPORT void JNICALL Java_saker_apple_impl_plist_lib_Plist_setDoubleKeyValue(
		JNIEnv* env, jclass clazz, jlong ptr, jstring key, jdouble value) {
	auto ref = toCFObject(env, value);
	if (ref == nullptr) {
		return;
	}
	PlistImpl& plist = *reinterpret_cast<PlistImpl*>(ptr);
	CFDictionarySetValue((CFMutableDictionaryRef) plist.propertyList.ref(), toCFString(env, key), ref);
}

JNIEXPORT void JNICALL Java_saker_apple_impl_plist_lib_Plist_setArrayKeyValue(
		JNIEnv* env, jclass clazz, jlong ptr, jstring key, jobjectArray value) {
	auto ref = toCFObject(env, value);
	if (ref == nullptr) {
		return;
	}
	PlistImpl& plist = *reinterpret_cast<PlistImpl*>(ptr);
	CFDictionarySetValue((CFMutableDictionaryRef) plist.propertyList.ref(), toCFString(env, key), ref);
}

JNIEXPORT void JNICALL Java_saker_apple_impl_plist_lib_Plist_setObjectKeyValue(
		JNIEnv* env, jclass clazz, jlong ptr, jstring key, jobject value) {
	auto ref = toCFObject(env, value);
	if (ref == nullptr) {
		return;
	}
	PlistImpl& plist = *reinterpret_cast<PlistImpl*>(ptr);
	CFDictionarySetValue((CFMutableDictionaryRef) plist.propertyList.ref(), toCFString(env, key), ref);
}

JNIEXPORT void JNICALL Java_saker_apple_impl_plist_lib_Plist_release(
		JNIEnv* env, jclass clazz, jlong ptr) {
	delete reinterpret_cast<PlistImpl*>(ptr);
}

