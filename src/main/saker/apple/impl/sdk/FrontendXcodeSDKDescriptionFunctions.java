package saker.apple.impl.sdk;

import saker.apple.api.SakerAppleUtils;
import saker.sdk.support.api.SDKDescription;

public interface FrontendXcodeSDKDescriptionFunctions extends SDKDescription {
	public default SDKDescription getStripSDK() {
		return XcodeExecutableSDKDescription.create(this, SakerAppleUtils.XCODE_EXECUTABLE_NAME_STRIP);
	}

	public default SDKDescription getClangSDK() {
		return XcodeExecutableSDKDescription.create(this, SakerAppleUtils.XCODE_EXECUTABLE_NAME_CLANG);
	}

	public default SDKDescription getClangXXSDK() {
		return XcodeExecutableSDKDescription.create(this, SakerAppleUtils.XCODE_EXECUTABLE_NAME_STRIP);
	}
}
