package saker.apple.main.sdk;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import saker.apple.impl.sdk.VersionsApplePlatformSDKDescription;
import saker.apple.main.TaskDocs.DocApplePlatformOption;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.trace.BuildTrace;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.main.TaskDocs.DocSDKDescription;

@NestTaskInformation(returnType = @NestTypeUsage(DocSDKDescription.class))
@NestInformation("Gets an SDK description for an Apple development platform.\n"
		+ "The target platform is one of the platforms or operating systems for which Apple provides "
		+ "developer utilities.\n" + "The SDK description can be passed to tasks that support it.")
@NestParameterInformation(value = "Platform",
		aliases = "",
		type = @NestTypeUsage(DocApplePlatformOption.class),
		info = @NestInformation("Specifies the development target platform.\n"
				+ "The platform is used to determine the base paths for tools that are used to target it. "
				+ "E.g. sysroot, framework and include paths, etc..."))
@NestParameterInformation(value = "Version",
		aliases = { "Versions" },
		type = @NestTypeUsage(value = Collection.class, elementTypes = String.class),
		info = @NestInformation("Specifies expected the versions or version ranges of the target platforms."))
public class PlatformSDKTaskFactory extends FrontendTaskFactory<SDKDescription> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.apple.sdk.platform";

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
				if ("macos".equals(platform)) {
					platform = "macosx";
				}
				if (!DocApplePlatformOption.KNOWN_PLATFORMS.contains(platform)) {
					SakerLog.warning().taskScriptPosition(taskcontext)
							.println("Unrecognized platform name: " + platform + " expected one of: "
									+ StringUtils.toStringJoin(", ", DocApplePlatformOption.KNOWN_PLATFORMS));
				}

				Set<String> versions = ImmutableUtils.makeImmutableNavigableSet(versionsOption);
				SDKDescription result = VersionsApplePlatformSDKDescription.create(platform, versions);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

}
