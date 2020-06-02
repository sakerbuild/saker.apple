package saker.apple.api;

public class SakerAppleUtils {

	public static final String SDK_NAME_XCODE = "Xcode";

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

	private SakerAppleUtils() {
		throw new UnsupportedOperationException();
	}
}
