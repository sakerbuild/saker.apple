package saker.apple.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import saker.apple.impl.xcode.ApplePlatformSDKInformation;
import saker.apple.impl.xcode.XcodeSDKVersions;
import saker.apple.impl.xcode.XcodeSDKVersionsEnvironmentProperty;
import saker.build.runtime.environment.EnvironmentProperty;
import saker.build.runtime.environment.SakerEnvironment;
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
		Collection<? extends ApplePlatformSDKInformation> sdkinfos = xcsdkversions.getSDKInformations();
		for (ApplePlatformSDKInformation sdkinfo : sdkinfos) {
			if (sdkInformation.sdkAttributesEqual(sdkinfo)) {
				return new ApplePlatformSDKReference(sdkinfo);
			}
		}
		StringBuilder sb = new StringBuilder("Apple platform SDK not found for: ");
		sb.append(sdkInformation);
		sb.append(" in [");
		Iterator<? extends ApplePlatformSDKInformation> it = sdkinfos.iterator();
		if (it.hasNext()) {
			while (true) {
				ApplePlatformSDKInformation sdkinfo = it.next();
				sb.append(sdkinfo);
				if (it.hasNext()) {
					sb.append(", ");
				} else {
					sb.append(')');
					break;
				}
			}
		}
		sb.append(']');
		throw new SDKNotFoundException(sb.toString());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(sdkInformation);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		sdkInformation = SerialUtils.readExternalObject(in);
	}

	//Note: the hashCode and equality only checks the SDK attributes, as other properties (installation path) are irrelevant

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sdkInformation == null) ? 0 : Objects.hashCode(sdkInformation.getName()));
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
		} else if (!sdkInformation.sdkAttributesEqual(other.sdkInformation))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + sdkInformation + "]";
	}

}
