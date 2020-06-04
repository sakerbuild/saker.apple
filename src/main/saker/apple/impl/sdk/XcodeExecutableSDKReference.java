package saker.apple.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Locale;

import saker.apple.api.SakerAppleUtils;
import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.sdk.support.api.SDKReference;

public class XcodeExecutableSDKReference implements SDKReference, Externalizable {
	private static final long serialVersionUID = 1L;

	/**
	 * For equality check.
	 */
	private SDKReference xcodeSDK;
	private transient SakerPath exePath;

	/**
	 * For {@link Externalizable}.
	 */
	public XcodeExecutableSDKReference() {
	}

	public XcodeExecutableSDKReference(SDKReference xcodeSDK, SakerPath exePath) {
		this.xcodeSDK = xcodeSDK;
		this.exePath = exePath;
	}

	public SDKReference getXcodeSDK() {
		return xcodeSDK;
	}

	@Override
	public SakerPath getPath(String identifier) throws Exception {
		if (identifier == null) {
			return null;
		}
		switch (identifier.toLowerCase(Locale.ENGLISH)) {
			case SakerAppleUtils.SDK_XCODE_EXECUTABLE_PATH_EXECUTABLE: {
				return exePath;
			}
		}
		return null;
	}

	@Override
	public String getProperty(String identifier) throws Exception {
		return null;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(xcodeSDK);
		out.writeObject(exePath);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		xcodeSDK = SerialUtils.readExternalObject(in);
		exePath = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		XcodeExecutableSDKReference other = (XcodeExecutableSDKReference) obj;
		if (xcodeSDK == null) {
			if (other.xcodeSDK != null)
				return false;
		} else if (!xcodeSDK.equals(other.xcodeSDK))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (xcodeSDK != null ? "xcode=" + xcodeSDK + ", " : "")
				+ (exePath != null ? "exePath=" + exePath : "") + "]";
	}

}
