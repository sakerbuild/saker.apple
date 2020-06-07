package saker.apple.main.plist;

import saker.apple.impl.plist.ConvertPlistWorkerTaskFactory;
import saker.apple.impl.plist.ConvertPlistWorkerTaskIdentifier;
import saker.build.exception.InvalidPathFormatException;
import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.trace.BuildTrace;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.FileLocation;
import saker.std.api.util.SakerStandardUtils;
import saker.std.main.file.option.FileLocationTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;

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
					if (!outputOption.isForwardRelative()) {
						throw new InvalidPathFormatException("Output" + " must be forward relative: " + outputOption);
					}
					if (outputOption.getFileName() == null) {
						throw new InvalidPathFormatException("Output" + " must have a file name: " + outputOption);
					}
					outputpath = outputOption;
				} else {
					outputpath = SakerPath.valueOf(SakerStandardUtils.getFileLocationFileName(inputfl));
				}

				ConvertPlistWorkerTaskIdentifier workertaskid = new ConvertPlistWorkerTaskIdentifier(outputpath);
				ConvertPlistWorkerTaskFactory workertask = new ConvertPlistWorkerTaskFactory(inputfl, formatOption.getFormat());
				taskcontext.startTask(workertaskid, workertask, null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

}
