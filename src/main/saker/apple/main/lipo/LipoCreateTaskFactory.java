package saker.apple.main.lipo;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;

import saker.apple.api.SakerAppleUtils;
import saker.apple.impl.lipo.LipoCreateWorkerTaskFactory;
import saker.apple.impl.lipo.LipoCreateWorkerTaskIdentifier;
import saker.apple.impl.sdk.VersionsXcodeSDKDescription;
import saker.apple.main.TaskDocs;
import saker.apple.main.TaskDocs.DocLipoCreateWorkerTaskOutput;
import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.trace.BuildTrace;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.main.SDKSupportFrontendUtils;
import saker.sdk.support.main.option.SDKDescriptionTaskOption;
import saker.std.api.file.location.FileLocation;
import saker.std.api.util.SakerStandardUtils;
import saker.std.main.file.option.MultiFileLocationTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;

@NestTypeInformation(relatedTypes = @NestTypeUsage(DocLipoCreateWorkerTaskOutput.class))
@NestInformation("Create an universal file from the inputs using the lipo tool.\n"
		+ "The task takes multiple input files and packs them into an universal file. "
		+ "It is generally used to create an universal executable that contains code for multiple architectures.")

@NestParameterInformation(value = "Input",
		aliases = { "" },
		required = true,
		type = @NestTypeUsage(value = Collection.class, elementTypes = { MultiFileLocationTaskOption.class }),
		info = @NestInformation("Specifies the input files which should be included in the output universal file."))
@NestParameterInformation(value = "Output",
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("A forward relative output path that specifies the output location of the universal file.\n"
				+ "It can be used to have a better output location than the automatically generated one."))
@NestParameterInformation(value = "SDKs",
		type = @NestTypeUsage(value = Map.class,
				elementTypes = { saker.sdk.support.main.TaskDocs.DocSdkNameOption.class,
						SDKDescriptionTaskOption.class }),
		info = @NestInformation(TaskDocs.SDKS))
public class LipoCreateTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.apple.lipo.create";

	private static final SakerPath DEFAULT_OUTPUT_PATH = SakerPath.valueOf("default");

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {
			@SakerInput(value = { "", "Input" }, required = true)
			public Collection<MultiFileLocationTaskOption> inputOption;

			@SakerInput(value = "Output")
			public SakerPath outputOption;

			@SakerInput(value = { "SDKs" })
			public Map<String, SDKDescriptionTaskOption> sdksOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}
				Collection<FileLocation> inputfiles = TaskOptionUtils.toFileLocations(inputOption, taskcontext, null);
				if (ObjectUtils.isNullOrEmpty(inputfiles)) {
					taskcontext.abortExecution(new IllegalArgumentException("No input files specified."));
					return null;
				}

				NavigableMap<String, SDKDescription> sdkdescriptions = SDKSupportFrontendUtils
						.toSDKDescriptionMap(sdksOption);

				sdkdescriptions.putIfAbsent(SakerAppleUtils.SDK_NAME_LIPO,
						VersionsXcodeSDKDescription.create(null).getLipoSDK());

				SakerPath outputpath;
				if (outputOption != null) {
					TaskOptionUtils.requireForwardRelativePathWithFileName(outputOption, "Output");
					outputpath = SakerPath.valueOf(TASK_NAME).resolve(outputOption);
				} else {
					outputpath = SakerPath.valueOf(TASK_NAME).resolve(inferDefaultOutputPathFromInputPaths(inputfiles));
				}

				LipoCreateWorkerTaskIdentifier workertaskid = new LipoCreateWorkerTaskIdentifier(outputpath);
				LipoCreateWorkerTaskFactory workertask = new LipoCreateWorkerTaskFactory(inputfiles);
				workertask.setSDKDescriptions(sdkdescriptions);

				taskcontext.startTask(workertaskid, workertask, null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

	protected static SakerPath inferDefaultOutputPathFromInputPaths(Iterable<? extends FileLocation> inputs) {
		if (inputs == null) {
			return DEFAULT_OUTPUT_PATH;
		}
		Iterator<? extends FileLocation> it = inputs.iterator();
		if (!it.hasNext()) {
			return DEFAULT_OUTPUT_PATH;
		}
		String s = SakerStandardUtils.getFileLocationFileName(it.next());
		while (it.hasNext()) {
			String n = SakerStandardUtils.getFileLocationFileName(it.next());
			int idx = mismatchIndex(s, n);
			if (idx < 0) {
				continue;
			}
			if (idx == 0) {
				//no common sequence
				return DEFAULT_OUTPUT_PATH;
			}
			s = s.substring(0, idx);
		}
		if (s.isEmpty()) {
			//check just in case
			return DEFAULT_OUTPUT_PATH;
		}
		return SakerPath.valueOf(s);
	}

	private static int mismatchIndex(CharSequence first, CharSequence second) {
		int flen = first.length();
		int slen = second.length();
		int minlen = Math.min(flen, slen);
		for (int i = 0; i < minlen; i++) {
			if (first.charAt(i) != second.charAt(i)) {
				return i;
			}
		}
		if (flen == slen) {
			return -1;
		}
		return minlen;
	}

}
