package saker.apple.impl.macos;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedHashMap;

import saker.build.exception.PropertyComputationFailedException;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.trace.BuildTrace;
import saker.build.trace.TraceContributorEnvironmentProperty;

public class MacOsSwVersionInformationEnvironmentProperty
		implements TraceContributorEnvironmentProperty<MacOsSwVersionInformation>, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final MacOsSwVersionInformationEnvironmentProperty INSTANCE = new MacOsSwVersionInformationEnvironmentProperty();

	/**
	 * For {@link Externalizable}.
	 */
	public MacOsSwVersionInformationEnvironmentProperty() {
	}

	@Override
	public MacOsSwVersionInformation getCurrentValue(SakerEnvironment environment) throws Exception {
		return MacOsSwVersionInformation.fromSwVersProcess();
	}

	@Override
	public void contributeBuildTraceInformation(MacOsSwVersionInformation propertyvalue,
			PropertyComputationFailedException thrownexception) {
		if (propertyvalue != null) {
			LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
			LinkedHashMap<Object, Object> props = new LinkedHashMap<>();

			values.put(propertyvalue.getProductName() + " " + propertyvalue.getProductVersion(), props);
			props.put("Build version", propertyvalue.getBuildVersion());

			BuildTrace.setValues(values, BuildTrace.VALUE_CATEGORY_ENVIRONMENT);
		} else {
			//exceptions as values supported since 0.8.14
			if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_014) {
				BuildTrace.setValues(ImmutableUtils.singletonMap("macOS version", thrownexception.getCause()),
						BuildTrace.VALUE_CATEGORY_ENVIRONMENT);
			}
		}
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
