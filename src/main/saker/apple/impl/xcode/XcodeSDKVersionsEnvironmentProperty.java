package saker.apple.impl.xcode;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.runtime.environment.EnvironmentProperty;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.thirdparty.saker.util.ObjectUtils;

public class XcodeSDKVersionsEnvironmentProperty implements EnvironmentProperty<XcodeSDKVersions>, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final XcodeSDKVersionsEnvironmentProperty INSTANCE = new XcodeSDKVersionsEnvironmentProperty();

	/**
	 * For {@link Externalizable}.
	 */
	public XcodeSDKVersionsEnvironmentProperty() {
	}

	@Override
	public XcodeSDKVersions getCurrentValue(SakerEnvironment environment) throws Exception {
		return XcodeSDKVersions.fromXcodebuildVersionSdkProcess();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
	}

	@Override
	public int hashCode() {
		return getClass().getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return ObjectUtils.isSameClass(this, obj);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[]";
	}

}
