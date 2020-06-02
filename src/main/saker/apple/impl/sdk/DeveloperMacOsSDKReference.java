package saker.apple.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Locale;

import saker.apple.api.SakerAppleUtils;
import saker.apple.impl.macos.MacOsSwVersionInformation;
import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.sdk.support.api.SDKReference;

public class DeveloperMacOsSDKReference implements SDKReference, Externalizable {
	private static final long serialVersionUID = 1L;

	private MacOsSwVersionInformation versions;

	/**
	 * For {@link Externalizable}.
	 */
	public DeveloperMacOsSDKReference() {
	}

	public DeveloperMacOsSDKReference(MacOsSwVersionInformation versions) {
		this.versions = versions;
	}

	public MacOsSwVersionInformation getVersionInformation() {
		return versions;
	}

	@Override
	public SakerPath getPath(String identifier) throws Exception {
		if (identifier == null) {
			return null;
		}
		return null;
	}

	@Override
	public String getProperty(String identifier) throws Exception {
		if (identifier == null) {
			return null;
		}
		switch (identifier.toLowerCase(Locale.ENGLISH)) {
			case SakerAppleUtils.SDK_DEVELOPER_MAC_OS_PROPERTY_PRODUCT_NAME: {
				return versions.getProductName();
			}
			case SakerAppleUtils.SDK_DEVELOPER_MAC_OS_PROPERTY_PRODUCT_VERSION: {
				return versions.getProductVersion();
			}
			case SakerAppleUtils.SDK_DEVELOPER_MAC_OS_PROPERTY_BUILD_VERSION: {
				return versions.getBuildVersion();
			}
		}
		return null;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(versions);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		versions = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((versions == null) ? 0 : versions.hashCode());
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
		DeveloperMacOsSDKReference other = (DeveloperMacOsSDKReference) obj;
		if (versions == null) {
			if (other.versions != null)
				return false;
		} else if (!versions.equals(other.versions))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + versions + "]";
	}

}
