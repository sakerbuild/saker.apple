package saker.apple.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Locale;

import saker.apple.api.SakerAppleUtils;
import saker.apple.impl.xcode.XcodeVersionInformation;
import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.sdk.support.api.SDKReference;

public class XcodeSDKReference implements SDKReference, Externalizable {
	private static final long serialVersionUID = 1L;

	private XcodeVersionInformation versionInformation;

	/**
	 * For {@link Externalizable}.
	 */
	public XcodeSDKReference() {
	}

	public XcodeSDKReference(XcodeVersionInformation versionInformation) {
		this.versionInformation = versionInformation;
	}

	public XcodeVersionInformation getVersionInformation() {
		return versionInformation;
	}

	@Override
	public SakerPath getPath(String identifier) throws Exception {
//		if (identifier == null) {
//			return null;
//		}
//		switch (identifier.toLowerCase(Locale.ENGLISH)) {
//		}
		return null;
	}

	@Override
	public String getProperty(String identifier) throws Exception {
		if (identifier == null) {
			return null;
		}
		switch (identifier.toLowerCase(Locale.ENGLISH)) {
			case SakerAppleUtils.SDK_XCODE_PROPERTY_VERSION: {
				return versionInformation.getVersion();
			}
			case SakerAppleUtils.SDK_XCODE_PROPERTY_BUILD_VERSION: {
				return versionInformation.getBuildVersion();
			}

			case SakerAppleUtils.SDK_XCODE_PROPERTY_VERSION_DTXCODE: {
				return versionInformation.getVersionAsDTXcode();
			}
		}
		return null;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(versionInformation);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		versionInformation = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((versionInformation == null) ? 0 : versionInformation.hashCode());
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
		XcodeSDKReference other = (XcodeSDKReference) obj;
		if (versionInformation == null) {
			if (other.versionInformation != null)
				return false;
		} else if (!versionInformation.equals(other.versionInformation))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ (versionInformation != null ? "versionInformation=" + versionInformation : "") + "]";
	}
}
