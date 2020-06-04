package saker.apple.api;

public class SakerAppleUtils {

	public static final String SDK_NAME_XCODE = "Xcode";
	public static final String SDK_NAME_DEVELOPER_MAC_OS = "DevMacOS";

	public static final String SDK_NAME_PLATFORM_IPHONEOS = "iPhoneOS";
	public static final String SDK_NAME_PLATFORM_IPHONESIMULATOR = "iPhoneSimulator";
	public static final String SDK_NAME_PLATFORM_MACOS = "MacOS";
	public static final String SDK_NAME_PLATFORM_APPLETVOS = "AppleTVOS";
	public static final String SDK_NAME_PLATFORM_APPLETVSIMULATOR = "AppleTVSimulator";
	public static final String SDK_NAME_PLATFORM_WATCHOS = "WatchOS";
	public static final String SDK_NAME_PLATFORM_WATCHSIMULATOR = "WatchSimulator";

	public static final String SDK_NAME_STRIP = "Strip";

	/**
	 * E.g.
	 * <code>/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.12.sdk</code>
	 */
	public static final String SDK_APPLEPLATFORM_PATH_PATH = "path";
	/**
	 * E.g. <code>/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform</code>
	 */
	public static final String SDK_APPLEPLATFORM_PATH_PLATFORM_PATH = "platform.path";

	/**
	 * E.g. <code>macosx10.12</code>
	 */
	public static final String SDK_APPLEPLATFORM_PROPERTY_NAME = "name";
	/**
	 * E.g. <code>10.12</code>
	 */
	public static final String SDK_APPLEPLATFORM_PROPERTY_SDK_VERSION = "sdk.version";
	/**
	 * E.g. <code>1.1</code>
	 */
	public static final String SDK_APPLEPLATFORM_PROPERTY_PLATFORM_VERSION = "platform.version";
	/**
	 * E.g. <code>16C58</code>
	 */
	public static final String SDK_APPLEPLATFORM_PROPERTY_PRODUCT_BUILD_VERSION = "product.build.version";
	/**
	 * E.g. <code>1983-2016 Apple Inc.</code>
	 */
	public static final String SDK_APPLEPLATFORM_PROPERTY_PRODUCT_COPYRIGHT = "product.copyright";
	/**
	 * E.g. <code>Mac OS X</code>
	 */
	public static final String SDK_APPLEPLATFORM_PROPERTY_PRODUCT_NAME = "product.name";
	/**
	 * E.g. <code>10.12.2</code>
	 */
	public static final String SDK_APPLEPLATFORM_PROPERTY_PRODUCT_VERSION = "product.version";
	/**
	 * E.g. <code>10.12.2</code>
	 */
	public static final String SDK_APPLEPLATFORM_PROPERTY_PRODUCT_USER_VISIBLE_VERSION = "product.user.visible.version";

	/**
	 * E.g. <code>8.2.1</code>
	 */
	public static final String SDK_XCODE_PROPERTY_VERSION = "version";
	/**
	 * E.g. <code>8C1002</code>
	 */
	public static final String SDK_XCODE_PROPERTY_BUILD_VERSION = "build.version";

	/**
	 * The version of Xcode in the format that the <code>DTXcode</code> plist property expects.
	 * <p>
	 * E.g. <code>0821</code>
	 */
	public static final String SDK_XCODE_PROPERTY_VERSION_DTXCODE = "version.dtxcode";

	/**
	 * E.g. <code>Mac OS X</code>
	 */
	public static final String SDK_DEVELOPER_MAC_OS_PROPERTY_PRODUCT_NAME = "product.name";
	/**
	 * E.g. <code>10.11.6</code>
	 */
	public static final String SDK_DEVELOPER_MAC_OS_PROPERTY_PRODUCT_VERSION = "product.version";
	/**
	 * E.g. <code>15G22010</code>
	 */
	public static final String SDK_DEVELOPER_MAC_OS_PROPERTY_BUILD_VERSION = "build.version";

	public static final String SDK_XCODE_EXECUTABLE_PATH_EXECUTABLE = "exe";

	public static final String XCODE_EXECUTABLE_NAME_CLANG = "clang";
	public static final String XCODE_EXECUTABLE_NAME_STRIP = "strip";
	public static final String XCODE_EXECUTABLE_NAME_CLANGXX = "clang++";
	public static final String XCODE_EXECUTABLE_NAME_LIPO = "lipo";
	public static final String XCODE_EXECUTABLE_NAME_DSYMUTIL = "dsymutil";

	private SakerAppleUtils() {
		throw new UnsupportedOperationException();
	}
}
