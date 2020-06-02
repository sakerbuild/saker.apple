package saker.apple.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;
import java.util.function.Predicate;

import saker.apple.impl.SakerAppleImplUtils;
import saker.apple.impl.xcode.XcodeSDKVersions;
import saker.apple.impl.xcode.XcodeSDKVersionsEnvironmentProperty;
import saker.apple.impl.xcode.XcodeVersionInformation;
import saker.build.runtime.environment.EnvironmentProperty;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.exc.SDKNotFoundException;

public class VersionsXcodeSDKReferenceEnvironmentProperty implements EnvironmentProperty<SDKReference>, Externalizable {
	private static final long serialVersionUID = 1L;

	private Set<String> versions;

	/**
	 * For {@link Externalizable}.
	 */
	public VersionsXcodeSDKReferenceEnvironmentProperty() {
	}

	public VersionsXcodeSDKReferenceEnvironmentProperty(Set<String> versions) {
		this.versions = versions;
	}

	@Override
	public SDKReference getCurrentValue(SakerEnvironment environment) throws Exception {
		Predicate<? super String> predicate = SakerAppleImplUtils.getSDKVersionPredicate(versions);
		XcodeSDKVersions xcsdkversions = environment
				.getEnvironmentPropertyCurrentValue(XcodeSDKVersionsEnvironmentProperty.INSTANCE);
		if (xcsdkversions == null) {
			throw new SDKNotFoundException("Xcode installation not found.");
		}
		XcodeVersionInformation xcversioninfo = xcsdkversions.getXcodeVersionInformation();
		if (predicate.test(xcversioninfo.getVersion())) {
			return new XcodeSDKReference(xcversioninfo);
		}
		throw new SDKNotFoundException("No suitable Xcode installation found for versions: "
				+ (versions == null ? "any" : StringUtils.toStringJoin(", ", versions)));
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, versions);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		versions = SerialUtils.readExternalImmutableNavigableSet(in);
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
		VersionsXcodeSDKReferenceEnvironmentProperty other = (VersionsXcodeSDKReferenceEnvironmentProperty) obj;
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
