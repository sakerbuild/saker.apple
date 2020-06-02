package saker.apple.main.sdk;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import saker.apple.impl.sdk.VersionsApplePlatformSDKDescription;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.trace.BuildTrace;
import saker.nest.utils.FrontendTaskFactory;
import saker.sdk.support.api.SDKDescription;

public class PlatformSDKTaskFactory extends FrontendTaskFactory<SDKDescription> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.apple.sdk.platform";

	public static final Set<String> KNOWN_PLATFORMS = ImmutableUtils.makeImmutableNavigableSet(new String[] {
			"iphoneos", "iphonesimulator", "macosx", "appletvos", "appletvsimulator", "watchos", "watchsimulator", });

	@Override
	public ParameterizableTask<? extends SDKDescription> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<SDKDescription>() {

			@SakerInput(value = { "", "Platform" }, required = true)
			public String platformOption;

			@SakerInput(value = { "Version", "Versions" })
			public Collection<String> versionsOption;

			@Override
			public SDKDescription run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_CONFIGURATION);
				}

				String platform = platformOption.toLowerCase(Locale.ENGLISH);
				if (!KNOWN_PLATFORMS.contains(platform)) {
					SakerLog.warning().taskScriptPosition(taskcontext).println("Unrecognized platform name: " + platform
							+ " expected one of: " + StringUtils.toStringJoin(", ", KNOWN_PLATFORMS));
				}

				Set<String> versions = ImmutableUtils.makeImmutableNavigableSet(versionsOption);
				SDKDescription result = VersionsApplePlatformSDKDescription.create(platform, versions);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

}
