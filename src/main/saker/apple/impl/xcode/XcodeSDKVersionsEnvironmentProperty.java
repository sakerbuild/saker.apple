package saker.apple.impl.xcode;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import saker.build.exception.PropertyComputationFailedException;
import saker.build.file.path.SakerPath;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.trace.BuildTrace;
import saker.build.trace.TraceContributorEnvironmentProperty;

public class XcodeSDKVersionsEnvironmentProperty
		implements TraceContributorEnvironmentProperty<XcodeSDKVersions>, Externalizable {
	private static final long serialVersionUID = 1L;

	private static final Map<String, String> TRACE_SDK_SIMPLE_NAME_REPLACEMENTS = new TreeMap<>(
			String::compareToIgnoreCase);
	static {
		TRACE_SDK_SIMPLE_NAME_REPLACEMENTS.put("iphoneos", "iOS");
		TRACE_SDK_SIMPLE_NAME_REPLACEMENTS.put("iphonesimulator", "iOS Simulator");
		TRACE_SDK_SIMPLE_NAME_REPLACEMENTS.put("macosx", "macOS");
		TRACE_SDK_SIMPLE_NAME_REPLACEMENTS.put("macos", "macOS");
		TRACE_SDK_SIMPLE_NAME_REPLACEMENTS.put("appletvos", "tvOS");
		TRACE_SDK_SIMPLE_NAME_REPLACEMENTS.put("appletvsimulator", "tvOS Simulator");
		TRACE_SDK_SIMPLE_NAME_REPLACEMENTS.put("watchos", "watchOS");
		TRACE_SDK_SIMPLE_NAME_REPLACEMENTS.put("watchsimulator", "watchOS Simulator");
	}

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
	public void contributeBuildTraceInformation(XcodeSDKVersions propertyvalue,
			PropertyComputationFailedException thrownexception) {
		if (propertyvalue != null) {
			LinkedHashMap<Object, Object> values = new LinkedHashMap<>();
			LinkedHashMap<Object, Object> xcodeprops = new LinkedHashMap<>();

			values.put("Xcode " + propertyvalue.getXcodeVersionInformation().getVersion(), xcodeprops);
			xcodeprops.put("Build version", propertyvalue.getXcodeVersionInformation().getBuildVersion());

			LinkedHashMap<Object, Object> platformsdks = new LinkedHashMap<>();
			xcodeprops.put("Platform SDKs", platformsdks);
			for (Entry<String, ApplePlatformSDKInformation> entry : propertyvalue.getSDKInformations().entrySet()) {
				ApplePlatformSDKInformation sdkinfo = entry.getValue();
				LinkedHashMap<String, String> sdkmap = new LinkedHashMap<>();

				String simplename = sdkinfo.getSimpleName();
				platformsdks.put(TRACE_SDK_SIMPLE_NAME_REPLACEMENTS.getOrDefault(simplename, simplename) + " "
						+ sdkinfo.getSDKVersion(), sdkmap);
				SakerPath path = sdkinfo.getPath();
				String platformver = sdkinfo.getPlatformVersion();
				SakerPath platformpath = sdkinfo.getPlatformPath();
				String productbuildver = sdkinfo.getProductBuildVersion();
				String productver = sdkinfo.getProductVersion();
				if (path != null) {
					sdkmap.put("Path", path.toString());
				}
				if (platformver != null) {
					sdkmap.put("Platform version", platformver);
				}
				if (platformpath != null) {
					sdkmap.put("Platform path", platformpath.toString());
				}
				if (productbuildver != null) {
					sdkmap.put("Product build version", productbuildver);
				}
				if (productver != null) {
					sdkmap.put("Product version", productver);
				}
			}

			BuildTrace.setValues(values, BuildTrace.VALUE_CATEGORY_ENVIRONMENT);
		} else {
			//exceptions as values supported since 0.8.14
			if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_014) {
				BuildTrace.setValues(ImmutableUtils.singletonMap("Xcode version", thrownexception.getCause()),
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
