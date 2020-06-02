package saker.apple.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableSet;
import java.util.Set;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.sdk.support.api.EnvironmentSDKDescription;
import saker.sdk.support.api.IndeterminateSDKDescription;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKReference;

public class VersionsApplePlatformSDKDescription implements IndeterminateSDKDescription, Externalizable {
	private static final long serialVersionUID = 1L;

	private String platformSimpleName;
	private NavigableSet<String> versions;

	/**
	 * For {@link Externalizable}.
	 */
	public VersionsApplePlatformSDKDescription() {
	}

	private VersionsApplePlatformSDKDescription(String platformSimpleName, Set<String> versions) {
		this.platformSimpleName = platformSimpleName;
		this.versions = ImmutableUtils.makeImmutableNavigableSet(versions);
	}

	public static SDKDescription create(String platformsimplename, Set<String> versions) {
		return new VersionsApplePlatformSDKDescription(platformsimplename, versions);
	}

	@Override
	public SDKDescription getBaseSDKDescription() {
		return EnvironmentSDKDescription
				.create(new VersionsApplePlatformSDKReferenceEnvironmentProperty(platformSimpleName, versions));
	}

	@Override
	public SDKDescription pinSDKDescription(SDKReference sdkreference) {
		if (sdkreference instanceof ApplePlatformSDKReference) {
			return EnvironmentSDKDescription.create(new SpecificApplePlatformSDKReferenceEnvironmentProperty(
					((ApplePlatformSDKReference) sdkreference).getSDKInformation()));
		}
		//shouldn't happen, but handle just in case
		return getBaseSDKDescription();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(platformSimpleName);
		SerialUtils.writeExternalCollection(out, versions);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		platformSimpleName = SerialUtils.readExternalObject(in);
		versions = SerialUtils.readExternalSortedImmutableNavigableSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((platformSimpleName == null) ? 0 : platformSimpleName.hashCode());
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
		VersionsApplePlatformSDKDescription other = (VersionsApplePlatformSDKDescription) obj;
		if (platformSimpleName == null) {
			if (other.platformSimpleName != null)
				return false;
		} else if (!platformSimpleName.equals(other.platformSimpleName))
			return false;
		if (versions == null) {
			if (other.versions != null)
				return false;
		} else if (!versions.equals(other.versions))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + platformSimpleName + " : " + versions + "]";
	}

}
