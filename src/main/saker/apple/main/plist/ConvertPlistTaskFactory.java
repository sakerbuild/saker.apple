package saker.apple.main.plist;

import saker.apple.impl.plist.ConvertPlistWorkerTaskFactory;
import saker.apple.impl.plist.ConvertPlistWorkerTaskIdentifier;
import saker.apple.main.TaskDocs.DocConvertPlistWorkerTaskOutput;
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
import saker.std.api.file.location.FileLocation;
import saker.std.api.util.SakerStandardUtils;
import saker.std.main.file.option.FileLocationTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;

@NestTaskInformation(returnType = @NestTypeUsage(DocConvertPlistWorkerTaskOutput.class))
@NestInformation("Converts between different representations of a plist file.\n"
		+ "The task can be used to transform a property list file into a different format.")

@NestParameterInformation(value = "Input",
		aliases = { "" },
		required = true,
		type = @NestTypeUsage(FileLocationTaskOption.class),
		info = @NestInformation("Specifies the input plist file which is to be converted."))
@NestParameterInformation(value = "Format",
		type = @NestTypeUsage(PlistFormatTaskOption.class),
		info = @NestInformation("Specifies the output format of the plist.\n" + "It is "
				+ PlistFormatTaskOption.FORMAT_BINARY1 + " by default."))
@NestParameterInformation(value = "Output",
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("A forward relative output path that specifies the output location of the converted plist.\n"
				+ "It can be used to have a better output location than the automatically generated one."))
public class ConvertPlistTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.plist.convert";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = { "", "Input" }, required = true)
			public FileLocationTaskOption inputOption;

			@SakerInput(value = "Format")
			public PlistFormatTaskOption formatOption = PlistFormatTaskOption.INSTANCE_BINARY1;

			@SakerInput(value = "Output")
			public SakerPath outputOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}

				FileLocation inputfl = TaskOptionUtils.toFileLocation(inputOption, taskcontext);

				SakerPath outputpath;
				if (outputOption != null) {
					TaskOptionUtils.requireForwardRelativePathWithFileName(outputOption, "Output");
					outputpath = SakerPath.valueOf(TASK_NAME).resolve(outputOption);
				} else {
					outputpath = SakerPath.valueOf(TASK_NAME)
							.resolve(SakerStandardUtils.getFileLocationFileName(inputfl));
				}

				ConvertPlistWorkerTaskIdentifier workertaskid = new ConvertPlistWorkerTaskIdentifier(outputpath);
				ConvertPlistWorkerTaskFactory workertask = new ConvertPlistWorkerTaskFactory(inputfl,
						formatOption.getFormat());
				taskcontext.startTask(workertaskid, workertask, null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

}
