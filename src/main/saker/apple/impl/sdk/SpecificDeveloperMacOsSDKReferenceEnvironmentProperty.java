package saker.apple.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.apple.impl.macos.MacOsSwVersionInformation;
import saker.apple.impl.macos.MacOsSwVersionInformationEnvironmentProperty;
import saker.build.runtime.environment.EnvironmentProperty;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.exc.SDKNotFoundException;

public class SpecificDeveloperMacOsSDKReferenceEnvironmentProperty
		implements EnvironmentProperty<SDKReference>, Externalizable {
	private static final long serialVersionUID = 1L;

	private MacOsSwVersionInformation versions;

	/**
	 * For {@link Externalizable}.
	 */
	public SpecificDeveloperMacOsSDKReferenceEnvironmentProperty() {
	}

	public SpecificDeveloperMacOsSDKReferenceEnvironmentProperty(MacOsSwVersionInformation versions) {
		this.versions = versions;
	}

	@Override
	public SDKReference getCurrentValue(SakerEnvironment environment) throws Exception {
		MacOsSwVersionInformation swvers = environment
				.getEnvironmentPropertyCurrentValue(MacOsSwVersionInformationEnvironmentProperty.INSTANCE);
		if (swvers == null) {
			throw new SDKNotFoundException("Failed to determine macOS software version.");
		}
		if (versions.equals(swvers)) {
			return new DeveloperMacOsSDKReference(swvers);
		}
		throw new SDKNotFoundException(
				"Host macOS has different version: " + swvers.getProductVersion() + " doesn't match: " + versions);
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
		SpecificDeveloperMacOsSDKReferenceEnvironmentProperty other = (SpecificDeveloperMacOsSDKReferenceEnvironmentProperty) obj;
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
