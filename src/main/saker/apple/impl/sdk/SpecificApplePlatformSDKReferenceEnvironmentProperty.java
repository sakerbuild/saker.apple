package saker.apple.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableMap;
import java.util.Map.Entry;

import saker.apple.impl.xcode.ApplePlatformSDKInformation;
import saker.apple.impl.xcode.XcodeSDKVersions;
import saker.apple.impl.xcode.XcodeSDKVersionsEnvironmentProperty;
import saker.build.runtime.environment.EnvironmentProperty;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.exc.SDKNotFoundException;

public class SpecificApplePlatformSDKReferenceEnvironmentProperty
		implements EnvironmentProperty<SDKReference>, Externalizable {
	private static final long serialVersionUID = 1L;

	private ApplePlatformSDKInformation sdkInformation;

	/**
	 * For {@link Externalizable}.
	 */
	public SpecificApplePlatformSDKReferenceEnvironmentProperty() {
	}

	public SpecificApplePlatformSDKReferenceEnvironmentProperty(ApplePlatformSDKInformation sdkInformation) {
		this.sdkInformation = sdkInformation;
	}

	@Override
	public SDKReference getCurrentValue(SakerEnvironment environment) throws Exception {
		XcodeSDKVersions xcsdkversions = environment
				.getEnvironmentPropertyCurrentValue(XcodeSDKVersionsEnvironmentProperty.INSTANCE);
		if (xcsdkversions == null) {
			throw new SDKNotFoundException("Xcode installation not found.");
		}
		NavigableMap<String, ApplePlatformSDKInformation> sdkinfos = xcsdkversions.getSDKInformations();
		for (Entry<String, ApplePlatformSDKInformation> entry : sdkinfos.entrySet()) {
			ApplePlatformSDKInformation sdkinfo = entry.getValue();
			if (sdkInformation.equals(sdkinfo)) {
				return new ApplePlatformSDKReference(sdkinfo);
			}
		}
		throw new SDKNotFoundException("Apple platform SDK not found for: " + sdkInformation + " in "
				+ StringUtils.toStringJoin(", ", sdkinfos.navigableKeySet()));
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
		SpecificApplePlatformSDKReferenceEnvironmentProperty other = (SpecificApplePlatformSDKReferenceEnvironmentProperty) obj;
		if (sdkInformation == null) {
			if (other.sdkInformation != null)
				return false;
		} else if (!sdkInformation.equals(other.sdkInformation))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + sdkInformation + "]";
	}

}
