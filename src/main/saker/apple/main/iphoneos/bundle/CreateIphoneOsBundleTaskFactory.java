package saker.apple.main.iphoneos.bundle;

import java.util.Collection;
import java.util.NavigableMap;

import saker.apple.impl.iphoneos.bundle.CreateIphoneOsBundleWorkerTaskFactory;
import saker.apple.impl.iphoneos.bundle.CreateIphoneOsBundleWorkerTaskIdentifier;
import saker.apple.main.TaskDocs.DocCreateIphoneOsBundleWorkerTaskOutput;
import saker.apple.main.iphoneos.sign.SignIphoneOsTaskFactory;
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
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.FileLocation;
import saker.std.main.dir.prepare.RelativeContentsTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;

@NestTaskInformation(returnType = @NestTypeUsage(DocCreateIphoneOsBundleWorkerTaskOutput.class))
@NestInformation("Creates an iPhone application bundle with the specified contents.\n"
		+ "The task can be used to create the .app application bundle for an iPhone OS app. It will "
		+ "fill a directory with the contents of the application in the specified manner.\n"
		+ "The output of the task can be later passed to " + SignIphoneOsTaskFactory.TASK_NAME
		+ "() task to create a digitally signed one.")

@NestParameterInformation(value = "Contents",
		aliases = { "" },
		required = true,
		type = @NestTypeUsage(value = Collection.class, elementTypes = { RelativeContentsTaskOption.class }),
		info = @NestInformation("Specifies the file contents of the application.\n"
				+ "All file contents of the application should be specified for this parameter."))
@NestParameterInformation(value = "GeneratePkgInfo",
		type = @NestTypeUsage(boolean.class),
		info = @NestInformation("Specifies whether or not the PkgInfo file should be automatically generated for the application.\n"
				+ "If set to true, the PkgInfo file will be generated with appropriate contents for the application. The contents "
				+ "are determined based on the Info.plist file entries.\n" + "The default is true.\n"
				+ "If the PkgInfo file is already specified in the Contents parameter or no Info.plist file is given, then "
				+ "it won't be generated."))
@NestParameterInformation(value = "Output",
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("A forward relative output path that specifies the output location of the application contents.\n"
				+ "It can be used to have a better output location than the automatically generated one."))
public class CreateIphoneOsBundleTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	private static final SakerPath PATH_PKGINFO = SakerPath.valueOf("PkgInfo");
	private static final SakerPath PATH_INFOPLIST = SakerPath.valueOf("Info.plist");

	public static final String TASK_NAME = "saker.iphoneos.bundle.create";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = { "", "Contents" }, required = true)
			public Collection<RelativeContentsTaskOption> contentsOption;

			@SakerInput(value = "GeneratePkgInfo")
			public Boolean generatePkgInfoOption = true;

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

				if (Boolean.TRUE.equals(generatePkgInfoOption)) {
					if (!inputmappings.containsKey(PATH_PKGINFO)) {
						FileLocation plist = inputmappings.get(PATH_INFOPLIST);
						if (plist != null) {
							inputmappings.put(PATH_PKGINFO, CreateMacOsBundleTaskFactory
									.getPkgInfoFileLocationBasedOnInfoPlist(taskcontext, plist));
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
