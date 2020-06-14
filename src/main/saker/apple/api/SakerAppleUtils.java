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
	 * SDK identifier for the path of the Apple platform SDK. This is the <code>Path</code> property of the SDK
	 * information displayed by <code>xcodebuild -version -sdk</code>.
	 * <p>
	 * E.g.
	 * <code>/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.12.sdk</code>
	 */
	public static final String SDK_APPLEPLATFORM_PATH_PATH = "path";
	/**
	 * SDK identifier for the platform path of the Apple platform SDK. This is the <code>PlatformPath</code> property of
	 * the SDK information displayed by <code>xcodebuild -version -sdk</code>.
	 * <p>
	 * E.g. <code>/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform</code>
	 */
	public static final String SDK_APPLEPLATFORM_PATH_PLATFORM_PATH = "platform.path";

	/**
	 * SDK property identifier for the name of the Apple platform SDK. This is the name part in parentheses of the SDK
	 * information displayed by <code>xcodebuild -version -sdk</code>.
	 * <p>
	 * E.g. <code>macosx10.12</code>
	 */
	public static final String SDK_APPLEPLATFORM_PROPERTY_NAME = "name";
	/**
	 * SDK property identifier for the platform name of the Apple platform SDK. The property value is determined based
	 * on {@link #SDK_APPLEPLATFORM_PROPERTY_NAME} by leaving out the version number.
	 * <p>
	 * E.g. <code>macosx</code>
	 */
	public static final String SDK_APPLEPLATFORM_PROPERTY_PLATFORM_NAME = "platform.name";
	/**
	 * SDK property identifier for the version of the Apple platform SDK. This is the <code>SDKVersion</code> property
	 * of the SDK information displayed by <code>xcodebuild -version -sdk</code>.
	 * <p>
	 * E.g. <code>10.12</code>
	 */
	public static final String SDK_APPLEPLATFORM_PROPERTY_SDK_VERSION = "sdk.version";
	/**
	 * SDK property identifier for the platform version of the Apple platform SDK. This is the
	 * <code>PlatformVersion</code> property of the SDK information displayed by <code>xcodebuild -version -sdk</code>.
	 * <p>
	 * E.g. <code>1.1</code>
	 */
	public static final String SDK_APPLEPLATFORM_PROPERTY_PLATFORM_VERSION = "platform.version";
	/**
	 * SDK property identifier for the product build version of the Apple platform SDK. This is the
	 * <code>ProductBuildVersion</code> property of the SDK information displayed by
	 * <code>xcodebuild -version -sdk</code>.
	 * <p>
	 * E.g. <code>16C58</code>
	 */
	public static final String SDK_APPLEPLATFORM_PROPERTY_PRODUCT_BUILD_VERSION = "product.build.version";
	/**
	 * SDK property identifier for the product copyright of the Apple platform SDK. This is the
	 * <code>ProductCopyright</code> property of the SDK information displayed by <code>xcodebuild -version -sdk</code>.
	 * <p>
	 * E.g. <code>1983-2016 Apple Inc.</code>
	 */
	public static final String SDK_APPLEPLATFORM_PROPERTY_PRODUCT_COPYRIGHT = "product.copyright";
	/**
	 * SDK property identifier for the product name of the Apple platform SDK. This is the <code>ProductName</code>
	 * property of the SDK information displayed by <code>xcodebuild -version -sdk</code>.
	 * <p>
	 * E.g. <code>Mac OS X</code>
	 */
	public static final String SDK_APPLEPLATFORM_PROPERTY_PRODUCT_NAME = "product.name";
	/**
	 * SDK property identifier for the product version of the Apple platform SDK. This is the
	 * <code>ProductVersion</code> property of the SDK information displayed by <code>xcodebuild -version -sdk</code>.
	 * <p>
	 * E.g. <code>10.12.2</code>
	 */
	public static final String SDK_APPLEPLATFORM_PROPERTY_PRODUCT_VERSION = "product.version";
	/**
	 * SDK property identifier for the product user visible version of the Apple platform SDK. This is the
	 * <code>ProductUserVisibleVersion</code> property of the SDK information displayed by
	 * <code>xcodebuild -version -sdk</code>.
	 * <p>
	 * E.g. <code>10.12.2</code>
	 */
	public static final String SDK_APPLEPLATFORM_PROPERTY_PRODUCT_USER_VISIBLE_VERSION = "product.user.visible.version";

	/**
	 * SDK property identifier for the version of Xcode. This is the version number displayed by the
	 * <code>xcodebuild -version</code> command.
	 * <p>
	 * E.g. <code>8.2.1</code>
	 */
	public static final String SDK_XCODE_PROPERTY_VERSION = "version";
	/**
	 * SDK property identifier for the build version of Xcode. This is the build version number displayed by the
	 * <code>xcodebuild -version</code> command.
	 * <p>
	 * E.g. <code>8C1002</code>
	 */
	public static final String SDK_XCODE_PROPERTY_BUILD_VERSION = "build.version";

	/**
	 * SDK property identifier for the version of Xcode in the format that the <code>DTXcode</code> plist property
	 * expects.
	 * <p>
	 * E.g. <code>0821</code>
	 */
	public static final String SDK_XCODE_PROPERTY_VERSION_DTXCODE = "version.dtxcode";

	/**
	 * SDK property identifier for the product name of macOS. This is the <code>ProductName</code> property of the
	 * <code>sw_vers</code> command output.
	 * <p>
	 * E.g. <code>Mac OS X</code>
	 */
	public static final String SDK_DEVELOPER_MAC_OS_PROPERTY_PRODUCT_NAME = "product.name";
	/**
	 * SDK property identifier for the product version of macOS. This is the <code>ProductVersion</code> property of the
	 * <code>sw_vers</code> command output.
	 * <p>
	 * E.g. <code>10.11.6</code>
	 */
	public static final String SDK_DEVELOPER_MAC_OS_PROPERTY_PRODUCT_VERSION = "product.version";
	/**
	 * SDK property identifier for the build version of macOS. This is the <code>BuildVersion</code> property of the
	 * <code>sw_vers</code> command output.
	 * <p>
	 * E.g. <code>15G22010</code>
	 */
	public static final String SDK_DEVELOPER_MAC_OS_PROPERTY_BUILD_VERSION = "build.version";

	/**
	 * SDK path identifier for Xcode based executable SDKs that represents the path to the executable.
	 */
	public static final String SDK_XCODE_EXECUTABLE_PATH_EXECUTABLE = "exe";

	/**
	 * Name of the clang executable that comes with Xcode.
	 */
	public static final String XCODE_EXECUTABLE_NAME_CLANG = "clang";
	/**
	 * Name of the clang++ executable that comes with Xcode.
	 */
	public static final String XCODE_EXECUTABLE_NAME_CLANGXX = "clang++";
	/**
	 * Name of the strip executable that comes with Xcode.
	 */
	public static final String XCODE_EXECUTABLE_NAME_STRIP = "strip";
	/**
	 * Name of the lipo executable that comes with Xcode.
	 */
	public static final String XCODE_EXECUTABLE_NAME_LIPO = "lipo";
	/**
	 * Name of the dsymutil executable that comes with Xcode.
	 */
	public static final String XCODE_EXECUTABLE_NAME_DSYMUTIL = "dsymutil";

	private SakerAppleUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets the default {@link SDKDescription} for Xcode.
	 * 
	 * @return The default {@link SDKDescription}.
	 */
	public static SDKDescription getDefaultXcodeSDKDescription() {
		return VersionsXcodeSDKDescription.create(null);
	}

	/**
	 * Gets the default {@link SDKDescription} for the developer macOS.
	 * 
	 * @return The default {@link SDKDescription}.
	 */
	public static SDKDescription getDefaultDeveloperMacOsSDKDescription() {
		return VersionsDeveloperMacOsSDKDescription.create(null);
	}

	/**
	 * Gets the default {@link SDKDescription} for the iPhone OS platform.
	 * 
	 * @return The default {@link SDKDescription}.
	 */
	public static SDKDescription getDefaultIPhoneOsSDKDescription() {
		return VersionsApplePlatformSDKDescription.create("iphoneos", null);
	}

	/**
	 * Gets the default {@link SDKDescription} for the iPhone simulator platform.
	 * 
	 * @return The default {@link SDKDescription}.
	 */
	public static SDKDescription getDefaultIPhoneSimulatorSDKDescription() {
		return VersionsApplePlatformSDKDescription.create("iphonesimulator", null);
	}

	/**
	 * Gets the default {@link SDKDescription} for the macOS platform.
	 * 
	 * @return The default {@link SDKDescription}.
	 */
	public static SDKDescription getDefaultMacOsSDKDescription() {
		return VersionsApplePlatformSDKDescription.create("macosx", null);
	}

	/**
	 * Gets the default {@link SDKDescription} for the Apple TV platform.
	 * 
	 * @return The default {@link SDKDescription}.
	 */
	public static SDKDescription getDefaultAppleTvOsSDKDescription() {
		return VersionsApplePlatformSDKDescription.create("appletvos", null);
	}

	/**
	 * Gets the default {@link SDKDescription} for the Apple TV simulator platform.
	 * 
	 * @return The default {@link SDKDescription}.
	 */
	public static SDKDescription getDefaultAppleTvSimulatorSDKDescription() {
		return VersionsApplePlatformSDKDescription.create("appletvsimulator", null);
	}

	/**
	 * Gets the default {@link SDKDescription} for the watchOS platform.
	 * 
	 * @return The default {@link SDKDescription}.
	 */
	public static SDKDescription getDefaultWatchOsSDKDescription() {
		return VersionsApplePlatformSDKDescription.create("watchos", null);
	}

	/**
	 * Gets the default {@link SDKDescription} for the watchOS simulator platform.
	 * 
	 * @return The default {@link SDKDescription}.
	 */
	public static SDKDescription getDefaultWatchSimulatorSDKDescription() {
		return VersionsApplePlatformSDKDescription.create("watchsimulator", null);
	}
}
