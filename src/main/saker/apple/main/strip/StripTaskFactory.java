package saker.apple.main.strip;

import java.util.Map;
import java.util.NavigableMap;

import saker.apple.api.SakerAppleUtils;
import saker.apple.impl.sdk.VersionsXcodeSDKDescription;
import saker.apple.impl.strip.StripWorkerTaskFactory;
import saker.apple.impl.strip.StripWorkerTaskIdentifier;
import saker.apple.main.TaskDocs;
import saker.apple.main.TaskDocs.DocStripWorkerTaskOutput;
import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.trace.BuildTrace;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.main.SDKSupportFrontendUtils;
import saker.sdk.support.main.option.SDKDescriptionTaskOption;
import saker.std.api.file.location.FileLocation;
import saker.std.api.util.SakerStandardUtils;
import saker.std.main.file.option.FileLocationTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;

@NestTaskInformation(returnType = @NestTypeUsage(DocStripWorkerTaskOutput.class))
@NestInformation("Invokes the strip tool to remove unused symbols from binaries.\n"
		+ "The task can be used to run strip on the compiled binaries.")

@NestParameterInformation(value = "Input",
		aliases = { "" },
		required = true,
		type = @NestTypeUsage(value = FileLocationTaskOption.class),
		info = @NestInformation("The input file to strip."))
@NestParameterInformation(value = "Output",
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("A forward relative output path that specifies the output location of the stripped binary.\n"
				+ "It can be used to have a better output location than the automatically generated one."))
@NestParameterInformation(value = "SDKs",
		type = @NestTypeUsage(value = Map.class,
				elementTypes = { saker.sdk.support.main.TaskDocs.DocSdkNameOption.class,
						SDKDescriptionTaskOption.class }),
		info = @NestInformation(TaskDocs.SDKS))
public class StripTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.apple.strip";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {
			@SakerInput(value = { "", "Input" }, required = true)
			public FileLocationTaskOption inputOption;

			@SakerInput(value = "Output")
			public SakerPath outputOption;

			@SakerInput(value = { "SDKs" })
			public Map<String, SDKDescriptionTaskOption> sdksOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}
				FileLocation inputfilelocation = TaskOptionUtils.toFileLocation(inputOption, taskcontext);

				NavigableMap<String, SDKDescription> sdkdescriptions = SDKSupportFrontendUtils
						.toSDKDescriptionMap(sdksOption);

				sdkdescriptions.putIfAbsent(SakerAppleUtils.SDK_NAME_STRIP,
						VersionsXcodeSDKDescription.create(null).getStripSDK());

				SakerPath outputpath;
				if (outputOption != null) {
					TaskOptionUtils.requireForwardRelativePathWithFileName(outputOption, "Output");
					outputpath = SakerPath.valueOf(TASK_NAME).resolve(outputOption);
				} else {
					outputpath = SakerPath.valueOf(TASK_NAME)
							.resolve(SakerStandardUtils.getFileLocationFileName(inputfilelocation));
				}

				StripWorkerTaskIdentifier workertaskid = new StripWorkerTaskIdentifier(outputpath);
				StripWorkerTaskFactory workertask = new StripWorkerTaskFactory(inputfilelocation);
				workertask.setSDKDescriptions(sdkdescriptions);

				taskcontext.startTask(workertaskid, workertask, null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

}
