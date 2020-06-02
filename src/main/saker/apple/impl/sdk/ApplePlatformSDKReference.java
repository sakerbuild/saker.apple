package saker.apple.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Locale;

import saker.apple.api.SakerAppleUtils;
import saker.apple.impl.xcode.ApplePlatformSDKInformation;
import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.sdk.support.api.SDKReference;

public class ApplePlatformSDKReference implements SDKReference, Externalizable {
	private static final long serialVersionUID = 1L;

	private ApplePlatformSDKInformation sdkInformation;

	/**
	 * For {@link Externalizable}.
	 */
	public ApplePlatformSDKReference() {
	}

	public ApplePlatformSDKReference(ApplePlatformSDKInformation sdkInformation) {
		this.sdkInformation = sdkInformation;
	}

	public ApplePlatformSDKInformation getSDKInformation() {
		return sdkInformation;
	}

	@Override
	public SakerPath getPath(String identifier) throws Exception {
		if (identifier == null) {
			return null;
		}
		switch (identifier.toLowerCase(Locale.ENGLISH)) {
			case SakerAppleUtils.SDK_APPLEPLATFORM_PATH_PATH: {
				return sdkInformation.getPath();
			}
			case SakerAppleUtils.SDK_APPLEPLATFORM_PATH_PLATFORM_PATH: {
				return sdkInformation.getPlatformPath();
			}
		}
		return null;
	}

	@Override
	public String getProperty(String identifier) throws Exception {
		if (identifier == null) {
			return null;
		}
		switch (identifier.toLowerCase(Locale.ENGLISH)) {
			case SakerAppleUtils.SDK_APPLEPLATFORM_PROPERTY_NAME: {
				return sdkInformation.getName();
			}
			case SakerAppleUtils.SDK_APPLEPLATFORM_PROPERTY_SDK_VERSION: {
				return sdkInformation.getSDKVersion();
			}
			case SakerAppleUtils.SDK_APPLEPLATFORM_PROPERTY_PLATFORM_VERSION: {
				return sdkInformation.getPlatformVersion();
			}

			case SakerAppleUtils.SDK_APPLEPLATFORM_PROPERTY_PRODUCT_BUILD_VERSION: {
				return sdkInformation.getProductBuildVersion();
			}
			case SakerAppleUtils.SDK_APPLEPLATFORM_PROPERTY_PRODUCT_COPYRIGHT: {
				return sdkInformation.getProductCopyright();
			}
			case SakerAppleUtils.SDK_APPLEPLATFORM_PROPERTY_PRODUCT_NAME: {
				return sdkInformation.getProductName();
			}
			case SakerAppleUtils.SDK_APPLEPLATFORM_PROPERTY_PRODUCT_VERSION: {
				return sdkInformation.getProductVersion();
			}
			case SakerAppleUtils.SDK_APPLEPLATFORM_PROPERTY_PRODUCT_USER_VISIBLE_VERSION: {
				return sdkInformation.getProductUserVisibleVersion();
			}
		}
		return null;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(sdkInformation);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		sdkInformation = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sdkInformation == null) ? 0 : sdkInformation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ApplePlatformSDKReference other = (ApplePlatformSDKReference) obj;
		if (sdkInformation == null) {
			if (other.sdkInformation != null)
				return false;
		} else if (!sdkInformation.equals(other.sdkInformation))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (sdkInformation != null ? "sdkInformation=" + sdkInformation : "")
				+ "]";
	}

}
