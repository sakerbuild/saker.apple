package saker.apple.main.iphoneos.sign;

import java.util.NavigableMap;

import saker.apple.api.iphoneos.bundle.CreateIphoneOsBundleWorkerTaskOutput;
import saker.apple.impl.iphoneos.sign.SignIphoneOsWorkerTaskFactory;
import saker.apple.impl.iphoneos.sign.SignIphoneOsWorkerTaskIdentifier;
import saker.apple.main.TaskDocs.DocSignIphoneOsWorkerTaskOutput;
import saker.apple.main.iphoneos.bundle.CreateIphoneOsBundleTaskFactory;
import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.exception.MissingRequiredParameterException;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.trace.BuildTrace;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.FileLocation;
import saker.std.main.file.option.FileLocationTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;

@NestTaskInformation(returnType = @NestTypeUsage(DocSignIphoneOsWorkerTaskOutput.class))
@NestInformation("Signs an iPhone application bundle.\n"
		+ "The task takes a previously created iPhone application bundle as input, and signs it with the "
		+ "specified provisioning profile and signing identity.\n"
		+ "The build task will NOT sign the application in-place, but rather copy its contents to a different "
		+ "output location and perform the signing there.")

@NestParameterInformation(value = "Application",
		aliases = "",
		required = true,
		type = @NestTypeUsage(SignIphoneOsTaskFactory.IphoneOsApplicationTaskOption.class),
		info = @NestInformation("Specifies the application that should be signed."))
@NestParameterInformation(value = "SigningIdentity",
		type = @NestTypeUsage(String.class),
		required = true,
		info = @NestInformation("The signing identity to be used when performing the operation.\n"
				+ "The signing identity is used to determine the certificate for the signature.\n"
				+ "This parameter is passed as the --sign argument for the codesign tool.\n"
				+ "It is recommended that the value of this parameter is exactly 40 hexadecimal digits "
				+ "that corresponds to a valid signing identity. You can list the signing identities using the\n"
				+ "security find-identity -v -p codesigning\n"
				+ "command. Look for the hash value at the start of the listed identities."))
@NestParameterInformation(value = "ProvisioningProfile",
		type = @NestTypeUsage(FileLocationTaskOption.class),
		required = true,
		info = @NestInformation("Specifies the provisioning profile that should be embedded in the application.\n"
				+ "The provisioning profile specifies the entitlements of the application.\n"
				+ "The specified provisioning profile will be part of the application "
				+ "contents with the embedded.mobileprovision file name.\n"
				+ "The entitlements of the profile will be extracted and passed as the "
				+ "--entitlements option for the codesign tool.\n"
				+ "The provisioning profiles managed by Xcode usually reside in the "
				+ "/Users/<YOUR_USERNAME>/Library/MobileDevice/Provisioning Profiles/ directory."))
@NestParameterInformation(value = "Output",
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("A forward relative output path that specifies the output location of the signed application contents.\n"
				+ "It can be used to have a better output location than the automatically generated one."))
public class SignIphoneOsTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.iphoneos.sign";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = { "", "Application" }, required = true)
			public IphoneOsApplicationTaskOption applicationOption;

			@SakerInput(value = "SigningIdentity")
			public String signingIdentityOption;

			@SakerInput(value = "ProvisioningProfile")
			public FileLocationTaskOption provisioningProfileOption;

			@SakerInput(value = "Output")
			public SakerPath outputOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}

				SakerPath appdir = applicationOption.getAppDirectory();
				NavigableMap<SakerPath, SakerPath> mappings = applicationOption.getMappings();
				FileLocation provisioningprofilefl = TaskOptionUtils.toFileLocation(provisioningProfileOption,
						taskcontext);
				String signingidentity = signingIdentityOption;
				if (provisioningprofilefl == null) {
					//TODO search for a provisioning profile in the 
					//     ~/Library/MobileDevice/Provisioning Profiles/ directory
					//and select one based on the bundle name in Info.plist and application-identifier in the xcent
					throw new MissingRequiredParameterException("ProvisioningProfile parameter is missing.",
							taskcontext.getTaskId());
				}
				if (signingidentity == null) {
					//TODO select one by listing using
					// security find-identity -v -p codesigning
					throw new MissingRequiredParameterException("SigningIdentity parameter is missing.",
							taskcontext.getTaskId());
				}

				SakerPath outputpath;
				if (outputOption != null) {
					TaskOptionUtils.requireForwardRelativePathWithFileName(outputOption, "Output");
					outputpath = SakerPath.valueOf(TASK_NAME).resolve(outputOption);
				} else {
					outputpath = SakerPath.valueOf(TASK_NAME).resolve(inferDefaultOutputPath(taskcontext, appdir));
				}
				String outputpathfilename = outputpath.getFileName();
				if (StringUtils.endsWithIgnoreCase(outputpathfilename, ".xcent")
						|| StringUtils.endsWithIgnoreCase(outputpathfilename, ".file-list")) {
					//so we can safely write the xcent file in the build directory next to the signed app
					throw new IllegalArgumentException("Invalid app directory name: " + outputpath.getFileName());
				}

				SignIphoneOsWorkerTaskIdentifier workertaskid = new SignIphoneOsWorkerTaskIdentifier(outputpath);
				SignIphoneOsWorkerTaskFactory workertask = new SignIphoneOsWorkerTaskFactory(appdir, mappings,
						signingidentity, provisioningprofilefl);
				taskcontext.startTask(workertaskid, workertask, null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

	protected static SakerPath inferDefaultOutputPath(TaskContext taskcontext, SakerPath appdir) {
		SakerPath builddirpath = taskcontext.getTaskBuildDirectoryPath();
		if (appdir.startsWith(builddirpath)) {
			SakerPath relative = builddirpath.relativize(appdir);
			if (relative.getNameCount() >= 2) {
				if (CreateIphoneOsBundleTaskFactory.TASK_NAME.equals(relative.getName(0))) {
					return relative.subPath(1);
				}
			}
			return relative;
		}
		if (appdir.getFileName() != null) {
			return SakerPath.valueOf(appdir.getFileName());
		}
		return SakerPath.valueOf("default.app");
	}

	@NestInformation("An iPhone application package.\n" + "The option accepts the output of the "
			+ CreateIphoneOsBundleTaskFactory.TASK_NAME + "() task.")
	public static class IphoneOsApplicationTaskOption {
		private SakerPath appDirectory;
		private NavigableMap<SakerPath, SakerPath> mappings;

		public IphoneOsApplicationTaskOption(SakerPath appDirectory, NavigableMap<SakerPath, SakerPath> mappings) {
			this.appDirectory = appDirectory;
			this.mappings = mappings;
		}

		public SakerPath getAppDirectory() {
			return appDirectory;
		}

		public NavigableMap<SakerPath, SakerPath> getMappings() {
			return mappings;
		}

		public static IphoneOsApplicationTaskOption valueOf(CreateIphoneOsBundleWorkerTaskOutput input) {
			return new IphoneOsApplicationTaskOption(input.getAppDirectory(), input.getMappings());
		}
	}
}
