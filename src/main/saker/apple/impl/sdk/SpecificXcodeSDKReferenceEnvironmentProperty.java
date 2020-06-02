package saker.apple.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.apple.impl.xcode.XcodeSDKVersions;
import saker.apple.impl.xcode.XcodeSDKVersionsEnvironmentProperty;
import saker.apple.impl.xcode.XcodeVersionInformation;
import saker.build.runtime.environment.EnvironmentProperty;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.exc.SDKNotFoundException;

public class SpecificXcodeSDKReferenceEnvironmentProperty implements EnvironmentProperty<SDKReference>, Externalizable {
	private static final long serialVersionUID = 1L;

	private XcodeVersionInformation versionInformation;

	/**
	 * For {@link Externalizable}.
	 */
	public SpecificXcodeSDKReferenceEnvironmentProperty() {
	}

	public SpecificXcodeSDKReferenceEnvironmentProperty(XcodeVersionInformation versionInformation) {
		this.versionInformation = versionInformation;
	}

	@Override
	public SDKReference getCurrentValue(SakerEnvironment environment) throws Exception {
		XcodeSDKVersions xcsdkversions = environment
				.getEnvironmentPropertyCurrentValue(XcodeSDKVersionsEnvironmentProperty.INSTANCE);
		if (xcsdkversions == null) {
			throw new SDKNotFoundException("Xcode installation not found.");
		}
		XcodeVersionInformation foundversioninfo = xcsdkversions.getXcodeVersionInformation();
		if (versionInformation.equals(foundversioninfo)) {
			return new XcodeSDKReference(foundversioninfo);
		}
		throw new SDKNotFoundException("Xcode installation not found for version: " + versionInformation);
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
		SpecificXcodeSDKReferenceEnvironmentProperty other = (SpecificXcodeSDKReferenceEnvironmentProperty) obj;
		if (versionInformation == null) {
			if (other.versionInformation != null)
				return false;
		} else if (!versionInformation.equals(other.versionInformation))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + versionInformation + "]";
	}

}
