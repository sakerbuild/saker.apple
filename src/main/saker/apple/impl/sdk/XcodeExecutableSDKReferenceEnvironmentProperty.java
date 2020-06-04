package saker.apple.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.apple.impl.xcode.XcodeExecutablePathEnvironmentProperty;
import saker.build.file.path.SakerPath;
import saker.build.runtime.environment.EnvironmentProperty;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.SDKSupportUtils;

public class XcodeExecutableSDKReferenceEnvironmentProperty
		implements EnvironmentProperty<XcodeExecutableSDKReference>, Externalizable {
	private static final long serialVersionUID = 1L;

	private SDKDescription xcodeSDK;
	private String executableName;

	/**
	 * For {@link Externalizable}.
	 */
	public XcodeExecutableSDKReferenceEnvironmentProperty() {
	}

	public XcodeExecutableSDKReferenceEnvironmentProperty(SDKDescription xcodeSDK, String executableName) {
		this.xcodeSDK = xcodeSDK;
		this.executableName = executableName;
	}

	@Override
	public XcodeExecutableSDKReference getCurrentValue(SakerEnvironment environment) throws Exception {
		SDKReference xcodesdkref = SDKSupportUtils.resolveSDKReference(environment, xcodeSDK);
		SakerPath path = environment
				.getEnvironmentPropertyCurrentValue(new XcodeExecutablePathEnvironmentProperty(executableName));
		return new XcodeExecutableSDKReference(xcodesdkref, path);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(xcodeSDK);
		out.writeObject(executableName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		xcodeSDK = SerialUtils.readExternalObject(in);
		executableName = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((executableName == null) ? 0 : executableName.hashCode());
		result = prime * result + ((xcodeSDK == null) ? 0 : xcodeSDK.hashCode());
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
		XcodeExecutableSDKReferenceEnvironmentProperty other = (XcodeExecutableSDKReferenceEnvironmentProperty) obj;
		if (executableName == null) {
			if (other.executableName != null)
				return false;
		} else if (!executableName.equals(other.executableName))
			return false;
		if (xcodeSDK == null) {
			if (other.xcodeSDK != null)
				return false;
		} else if (!xcodeSDK.equals(other.xcodeSDK))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "XcodeExecutableSDKReferenceEnvironmentProperty["
				+ (xcodeSDK != null ? "xcodeSDK=" + xcodeSDK + ", " : "")
				+ (executableName != null ? "executableName=" + executableName : "") + "]";
	}
}
