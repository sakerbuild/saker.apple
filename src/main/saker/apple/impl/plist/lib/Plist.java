package saker.apple.impl.plist.lib;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Native;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.StreamUtils;

public class Plist implements AutoCloseable {
	@Native
	public static final int FORMAT_SAME_AS_INPUT = 0;
	@Native
	public static final int FORMAT_XML = 1;
	@Native
	public static final int FORMAT_BINARY = 2;

	private static final AtomicIntegerFieldUpdater<Plist> AIFU_useCounter = AtomicIntegerFieldUpdater
			.newUpdater(Plist.class, "useCounter");
	private volatile int useCounter = 1;

	private static final AtomicIntegerFieldUpdater<Plist> AIFU_closed = AtomicIntegerFieldUpdater
			.newUpdater(Plist.class, "closed");
	private volatile int closed;

	private final long ptr;

	private Plist(long ptr) {
		this.ptr = ptr;
	}

	public static Plist createEmpty() {
		checkLoaded();
		return new Plist(createEmptyPlist());
	}

	public static Plist readFrom(InputStream is) throws IOException {
		checkLoaded();
		ByteArrayRegion bytes = StreamUtils.readStreamFully(is);
		return new Plist(createFromBytes(bytes.getArray(), bytes.getOffset(), bytes.getLength()));
	}

	public Object get(String key) {
		Objects.requireNonNull(key, "key");
		return getValue(ptr, key);
	}

	public void set(String key, String value) {
		Objects.requireNonNull(key, "key");
		Objects.requireNonNull(value, "value");
		use();
		try {
			setStringKeyValue(ptr, key, value);
		} finally {
			release();
		}
	}

	public void set(String key, boolean value) {
		Objects.requireNonNull(key, "key");
		use();
		try {
			setBooleanKeyValue(ptr, key, value);
		} finally {
			release();
		}
	}

	public void set(String key, long value) {
		Objects.requireNonNull(key, "key");
		use();
		try {
			setLongKeyValue(ptr, key, value);
		} finally {
			release();
		}
	}

	public void set(String key, double value) {
		Objects.requireNonNull(key, "key");
		use();
		try {
			setDoubleKeyValue(ptr, key, value);
		} finally {
			release();
		}
	}

	public void set(String key, Object[] value) {
		Objects.requireNonNull(key, "key");
		Objects.requireNonNull(value, "value");
		use();
		try {
			setArrayKeyValue(ptr, key, value);
		} finally {
			release();
		}
	}

	public void set(String key, Object value) {
		Objects.requireNonNull(key, "key");
		Objects.requireNonNull(value, "value");
		use();
		try {
			setObjectKeyValue(ptr, key, value);
		} finally {
			release();
		}
	}

	/**
	 * Serializes the plist in the specified format.
	 * 
	 * @param format
	 *            One of the <code>FORMAT_*</code> constants.
	 * @return The serialized bytes.
	 */
	public byte[] serialize(int format) {
		return serialize(ptr, format);
	}

	private static native long createEmptyPlist();

	private static native long createFromBytes(byte[] bytes, int offset, int length) throws IOException;

	private static native void release(long ptr);

	private static native byte[] serialize(long ptr, int format);

	private static native void setStringKeyValue(long ptr, String key, String value);

	private static native void setBooleanKeyValue(long ptr, String key, boolean value);

	private static native void setLongKeyValue(long ptr, String key, long value);

	private static native void setDoubleKeyValue(long ptr, String key, double value);

	private static native void setArrayKeyValue(long ptr, String key, Object[] value);

	private static native void setObjectKeyValue(long ptr, String key, Object value);

	private static native Object getValue(long ptr, String key);

	private void use() throws IllegalStateException {
		AIFU_useCounter.updateAndGet(this, c -> {
			if (c <= 0) {
				throw new IllegalStateException("Closed.");
			}
			return c + 1;
		});
	}

	private void release() {
		int c = AIFU_useCounter.decrementAndGet(this);
		if (c == 0) {
			release(ptr);
		}
	}

	@Override
	public void close() {
		if (!AIFU_closed.compareAndSet(this, 0, 1)) {
			//already closed
			return;
		}
		release();
	}

	private static void checkLoaded() {
		if (!PlatformLib.LOADED) {
			throw new RuntimeException("Failed to load plist handling native library.", PlatformLib.loadCause);
		}
	}

}
