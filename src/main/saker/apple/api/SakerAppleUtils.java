package saker.apple.api;

import saker.apple.impl.sdk.VersionsApplePlatformSDKDescription;
import saker.apple.impl.sdk.VersionsDeveloperMacOsSDKDescription;
import saker.apple.impl.sdk.VersionsXcodeSDKDescription;
import saker.sdk.support.api.SDKDescription;

/**
 * Utility class for interacting with the saker.apple package.
 */
public class SakerAppleUtils {

	/**
	 * SDK name for the Xcode SDK.
	 */
	public static final String SDK_NAME_XCODE = "Xcode";
	/**
	 * SDK name for the SDK representing the developer macOS information.
	 */
	public static final String SDK_NAME_DEVELOPER_MAC_OS = "DevMacOS";

	/**
	 * Apple platform SDK name for iPhone.
	 * <p>
	 * SDK constants in this class starting with <code>SDK_APPLEPLATFORM_*</code> can be used with this SDK.
	 */
	public static final String SDK_NAME_PLATFORM_IPHONEOS = "iPhoneOS";
	/**
	 * Apple platform SDK name for iPhone simulator.
	 * <p>
	 * SDK constants in this class starting with <code>SDK_APPLEPLATFORM_*</code> can be used with this SDK.
	 */
	public static final String SDK_NAME_PLATFORM_IPHONESIMULATOR = "iPhoneSimulator";
	/**
	 * Apple platform SDK name for macOS.
	 * <p>
	 * SDK constants in this class starting with <code>SDK_APPLEPLATFORM_*</code> can be used with this SDK.
	 */
	public static final String SDK_NAME_PLATFORM_MACOS = "MacOS";
	/**
	 * Apple platform SDK name for Apple TV.
	 * <p>
	 * SDK constants in this class starting with <code>SDK_APPLEPLATFORM_*</code> can be used with this SDK.
	 */
	public static final String SDK_NAME_PLATFORM_APPLETVOS = "AppleTVOS";
	/**
	 * Apple platform SDK name for Apple TV simulator.
	 * <p>
	 * SDK constants in this class starting with <code>SDK_APPLEPLATFORM_*</code> can be used with this SDK.
	 */
	public static final String SDK_NAME_PLATFORM_APPLETVSIMULATOR = "AppleTVSimulator";
	/**
	 * Apple platform SDK name for watchOS.
	 * <p>
	 * SDK constants in this class starting with <code>SDK_APPLEPLATFORM_*</code> can be used with this SDK.
	 */
	public static final String SDK_NAME_PLATFORM_WATCHOS = "WatchOS";
	/**
	 * Apple platform SDK name for watchOS simulator.
	 * <p>
	 * SDK constants in this class starting with <code>SDK_APPLEPLATFORM_*</code> can be used with this SDK.
	 */
	public static final String SDK_NAME_PLATFORM_WATCHSIMULATOR = "WatchSimulator";

	/**
	 * SDK name for the strip tool.
	 */
	public static final String SDK_NAME_STRIP = "Strip";
	/**
	 * SDK name for the lipo tool.
	 */
	public static final String SDK_NAME_LIPO = "Lipo";

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
	 * E.g. <code>macosx</code>
	 */
	public static final String SDK_APPLEPLATFORM_PROPERTY_PLATFORM_NAME = "platform.name";
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

	public static SDKDescription getDefaultXcodeSDKDescription() {
		return VersionsXcodeSDKDescription.create(null);
	}

	public static SDKDescription getDefaultDeveloperMacOsSDKDescription() {
		return VersionsDeveloperMacOsSDKDescription.create(null);
	}

	public static SDKDescription getDefaultIPhoneOsSDKDescription() {
		return VersionsApplePlatformSDKDescription.create("iphoneos", null);
	}

	public static SDKDescription getDefaultIPhoneSimulatorSDKDescription() {
		return VersionsApplePlatformSDKDescription.create("iphonesimulator", null);
	}

	public static SDKDescription getDefaultMacOsSDKDescription() {
		return VersionsApplePlatformSDKDescription.create("macosx", null);
	}

	public static SDKDescription getDefaultAppleTvOsSDKDescription() {
		return VersionsApplePlatformSDKDescription.create("appletvos", null);
	}

	public static SDKDescription getDefaultAppleTvSimulatorSDKDescription() {
		return VersionsApplePlatformSDKDescription.create("appletvsimulator", null);
	}

	public static SDKDescription getDefaultWatchOsSDKDescription() {
		return VersionsApplePlatformSDKDescription.create("watchos", null);
	}

	public static SDKDescription getDefaultWatchSimulatorSDKDescription() {
		return VersionsApplePlatformSDKDescription.create("watchsimulator", null);
	}

}
