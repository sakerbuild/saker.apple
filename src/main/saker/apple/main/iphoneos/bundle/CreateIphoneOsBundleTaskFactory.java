package saker.apple.main.iphoneos.bundle;

import java.util.Collection;
import java.util.NavigableMap;

import saker.apple.impl.iphoneos.bundle.CreateIphoneOsBundleWorkerTaskIdentifier;
import saker.apple.impl.iphoneos.bundle.CreateIphoneOsBundleWorkerTaskFactory;
import saker.apple.main.macos.bundle.CreateMacOsBundleTaskFactory;
import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.trace.BuildTrace;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.FileLocation;
import saker.std.main.dir.prepare.RelativeContentsTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;

public class CreateIphoneOsBundleTaskFactory extends FrontendTaskFactory<Object> {
	private static final SakerPath PATH_PKGINFO = SakerPath.valueOf("PkgInfo");
	private static final SakerPath PATH_INFOPLIST = SakerPath.valueOf("Info.plist");

	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.iphoneos.bundle.create";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = { "", "Contents" }, required = true)
			public Collection<RelativeContentsTaskOption> contentsOption;

			@SakerInput(value = "GeneratePkgInfo")
			public boolean generatePkgInfoOption = true;

			@SakerInput(value = "Output")
			public SakerPath outputOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}

				contentsOption = ObjectUtils.cloneArrayList(contentsOption, RelativeContentsTaskOption::clone);
				NavigableMap<SakerPath, FileLocation> inputmappings = RelativeContentsTaskOption.toInputMap(taskcontext,
						contentsOption, null);

				if (generatePkgInfoOption) {
					if (!inputmappings.containsKey(PATH_PKGINFO)) {
						FileLocation plist = inputmappings.get(PATH_INFOPLIST);
						if (plist != null) {
							inputmappings.put(PATH_PKGINFO, CreateMacOsBundleTaskFactory
									.getPkgInfoFileLocationBasedOnInfoPlits(taskcontext, plist));
						}
						//else don't auto generate the pkginfo as we don't have an info.plist
					}
				}

				SakerPath outputpath;
				if (outputOption != null) {
					TaskOptionUtils.requireForwardRelativePathWithFileName(outputOption, "Output");
					outputpath = SakerPath.valueOf(TASK_NAME).resolve(outputOption);
				} else {
					outputpath = SakerPath.valueOf(TASK_NAME).resolve("default.app");
				}

				TaskIdentifier workertaskid = new CreateIphoneOsBundleWorkerTaskIdentifier(outputpath);
				CreateIphoneOsBundleWorkerTaskFactory workertask = new CreateIphoneOsBundleWorkerTaskFactory(
						inputmappings);
				taskcontext.startTask(workertaskid, workertask, null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}

		};
	}
}
