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

public class VersionsXcodeSDKDescription
		implements IndeterminateSDKDescription, FrontendXcodeSDKDescriptionFunctions, Externalizable {
	private static final long serialVersionUID = 1L;

	private NavigableSet<String> versions;

	/**
	 * For {@link Externalizable}.
	 */
	public VersionsXcodeSDKDescription() {
	}

	private VersionsXcodeSDKDescription(Set<String> versions) {
		this.versions = ImmutableUtils.makeImmutableNavigableSet(versions);
	}

	public static VersionsXcodeSDKDescription create(Set<String> versions) {
		return new VersionsXcodeSDKDescription(versions);
	}

	@Override
	public SDKDescription getBaseSDKDescription() {
		return EnvironmentSDKDescription.create(new VersionsXcodeSDKReferenceEnvironmentProperty(versions));
	}

	@Override
	public SDKDescription pinSDKDescription(SDKReference sdkreference) {
		if (sdkreference instanceof XcodeSDKReference) {
			EnvironmentSDKDescription pinnedsdk = EnvironmentSDKDescription
					.create(new SpecificXcodeSDKReferenceEnvironmentProperty(
							((XcodeSDKReference) sdkreference).getVersionInformation()));
			return new PinnedXcodeSDKDescription(pinnedsdk);
		}
		//shouldn't happen, but handle just in case
		return getBaseSDKDescription();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, versions);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		versions = SerialUtils.readExternalSortedImmutableNavigableSet(in);
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
		VersionsXcodeSDKDescription other = (VersionsXcodeSDKDescription) obj;
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

	/**
	 * This class exists so the <code>get*SDK()</code> functions can be called by external agents via reflection.
	 */
	private static final class PinnedXcodeSDKDescription extends DelegateSDKDescription
			implements FrontendXcodeSDKDescriptionFunctions {
		private static final long serialVersionUID = 1L;

		/**
		 * For {@link Externalizable}.
		 */
		public PinnedXcodeSDKDescription() {
		}

		public PinnedXcodeSDKDescription(SDKDescription description) {
			super(description);
		}

	}
}
