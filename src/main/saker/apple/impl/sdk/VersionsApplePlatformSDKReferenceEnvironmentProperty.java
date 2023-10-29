package saker.apple.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import saker.apple.impl.SakerAppleImplUtils;
import saker.apple.impl.xcode.ApplePlatformSDKInformation;
import saker.apple.impl.xcode.XcodeSDKVersions;
import saker.apple.impl.xcode.XcodeSDKVersionsEnvironmentProperty;
import saker.build.runtime.environment.EnvironmentProperty;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.exc.SDKNotFoundException;

public class VersionsApplePlatformSDKReferenceEnvironmentProperty
		implements EnvironmentProperty<SDKReference>, Externalizable {
	private static final long serialVersionUID = 1L;

	private String platformSimpleName;
	private Set<String> versions;

	/**
	 * For {@link Externalizable}.
	 */
	public VersionsApplePlatformSDKReferenceEnvironmentProperty() {
	}

	public VersionsApplePlatformSDKReferenceEnvironmentProperty(String platformSimpleName, Set<String> versions) {
		Objects.requireNonNull(platformSimpleName, "platform simple name");
		this.platformSimpleName = platformSimpleName;
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
		Collection<? extends ApplePlatformSDKInformation> sdkinfos = xcsdkversions.getSDKInformations();
		for (ApplePlatformSDKInformation sdkinfo : sdkinfos) {
			if (!platformSimpleName.equals(sdkinfo.getSimpleName())) {
				continue;
			}
			if (predicate.test(sdkinfo.getSDKVersion())) {
				return new ApplePlatformSDKReference(sdkinfo);
			}
		}
		StringBuilder sb = new StringBuilder("Apple platform SDK not found for: ");
		sb.append(platformSimpleName);
		sb.append(" in [");
		Iterator<? extends ApplePlatformSDKInformation> it = sdkinfos.iterator();
		if (it.hasNext()) {
			while (true) {
				ApplePlatformSDKInformation sdkinfo = it.next();
				sb.append(sdkinfo.getName());
				sb.append(" (");
				sb.append(sdkinfo.getSDKVersion());
				if (it.hasNext()) {
					sb.append("), ");
				} else {
					sb.append(')');
					break;
				}
			}
		}
		sb.append("] for versions: ");
		if (versions == null) {
			sb.append("any");
		} else {
			sb.append(StringUtils.toStringJoin(", ", versions));
		}
		throw new SDKNotFoundException(sb.toString());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(platformSimpleName);
		SerialUtils.writeExternalCollection(out, versions);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		platformSimpleName = SerialUtils.readExternalObject(in);
		versions = SerialUtils.readExternalImmutableNavigableSet(in);
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
		VersionsApplePlatformSDKReferenceEnvironmentProperty other = (VersionsApplePlatformSDKReferenceEnvironmentProperty) obj;
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
