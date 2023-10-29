package saker.apple.impl.xcode;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedHashMap;
import java.util.Map;
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
		TRACE_SDK_SIMPLE_NAME_REPLACEMENTS.put("driverkit", "DriverKit");
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
			for (ApplePlatformSDKInformation sdkinfo : propertyvalue.getSDKInformations()) {
				LinkedHashMap<String, String> sdkmap = new LinkedHashMap<>();

				platformsdks.put(getSDKTraceDisplayName(sdkinfo), sdkmap);
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

	private static String getSDKTraceDisplayName(ApplePlatformSDKInformation sdkinfo) {
		String simplename = sdkinfo.getSimpleName();
		StringBuilder displaynamesb = new StringBuilder();
		displaynamesb.append(TRACE_SDK_SIMPLE_NAME_REPLACEMENTS.getOrDefault(simplename, simplename));
		displaynamesb.append(" ");
		displaynamesb.append(sdkinfo.getSDKVersion());

		String headersdkname = sdkinfo.getHeaderSDKName();
		if (headersdkname != null && !(sdkinfo.getName() + ".sdk").equalsIgnoreCase(headersdkname)) {
			//The header SDK name and the name is different for some reason, e.g. in
			//    MacOSX13.sdk - macOS 13.1 (macosx13.1)
			//append the header sdk name to the display name to avoid confusion
			displaynamesb.append(" (");
			displaynamesb.append(headersdkname);
			displaynamesb.append(')');
		}
		return displaynamesb.toString();
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
