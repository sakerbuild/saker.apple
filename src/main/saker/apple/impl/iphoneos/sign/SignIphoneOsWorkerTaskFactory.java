package saker.apple.impl.iphoneos.sign;

import java.io.BufferedReader;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;

import saker.apple.api.iphoneos.sign.SignIphoneOsWorkerTaskOutput;
import saker.apple.impl.plist.lib.Plist;
import saker.apple.main.iphoneos.sign.SignIphoneOsTaskFactory;
import saker.build.exception.InvalidPathFormatException;
import saker.build.file.ByteArraySakerFile;
import saker.build.file.DelegateSakerFile;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.content.DirectoryContentDescriptor;
import saker.build.file.path.ProviderHolderPathKey;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskExecutionUtilities;
import saker.build.task.TaskFactory;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import saker.build.trace.BuildTrace;
import saker.process.api.CollectingProcessIOConsumer;
import saker.process.api.SakerProcess;
import saker.process.api.SakerProcessBuilder;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.api.util.SakerStandardUtils;

public class SignIphoneOsWorkerTaskFactory
		implements TaskFactory<SignIphoneOsWorkerTaskOutput>, Task<SignIphoneOsWorkerTaskOutput>, Externalizable {
	private static final String EMBEDDED_MOBILEPROVISION_FILE_NAME = "embedded.mobileprovision";

	private static final long serialVersionUID = 1L;

	private SakerPath appDirectory;
	private NavigableMap<SakerPath, SakerPath> mappings;
	private String signingIdentity;
	private FileLocation provisioningProfile;

	/**
	 * For {@link Externalizable}.
	 */
	public SignIphoneOsWorkerTaskFactory() {
	}

	public SignIphoneOsWorkerTaskFactory(SakerPath appDirectory, NavigableMap<SakerPath, SakerPath> mappings,
			String signingIdentity, FileLocation provisioningProfile) {
		this.appDirectory = appDirectory;
		this.mappings = mappings;
		this.signingIdentity = signingIdentity;
		this.provisioningProfile = provisioningProfile;
	}

	@Override
	public int getRequestedComputationTokenCount() {
		return 1;
	}

	@Override
	public Task<? extends SignIphoneOsWorkerTaskOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public SignIphoneOsWorkerTaskOutput run(TaskContext taskcontext) throws Exception {
		SignIphoneOsWorkerTaskIdentifier taskid = (SignIphoneOsWorkerTaskIdentifier) taskcontext.getTaskId();
		SakerPath outputrelativepath = taskid.getOutputPath();

		SakerPath appdirpath = appDirectory;
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
			BuildTrace.setDisplayInformation("sign.iphoneos",
					SignIphoneOsTaskFactory.TASK_NAME + ":" + appdirpath.getFileName());
		}
		taskcontext.setStandardOutDisplayIdentifier(SignIphoneOsTaskFactory.TASK_NAME + ":" + appdirpath.getFileName());

		TaskExecutionUtilities taskutils = taskcontext.getTaskUtilities();

		SakerDirectory appdir = taskutils.resolveDirectoryAtPath(appdirpath);
		if (appdir == null) {
			throw new NotDirectoryException("Iphone application directory not found: " + appdir);
		}

		SakerDirectory outputdir = taskutils
				.resolveDirectoryAtPathCreate(SakerPathFiles.requireBuildDirectory(taskcontext), outputrelativepath);
		SakerPath outputdirpath = outputdir.getSakerPath();

		outputdir.clear();

		NavigableMap<SakerPath, ContentDescriptor> inputdependencies = new TreeMap<>();
		NavigableMap<SakerPath, ContentDescriptor> outputdependencies = new TreeMap<>();

		for (Entry<SakerPath, SakerPath> entry : mappings.entrySet()) {
			SakerPath relentrypath = entry.getKey();
			SakerFile f = taskutils.resolveFileAtRelativePath(appdir, relentrypath);
			if (f == null) {
				throw new NoSuchFileException(
						"File not found in iPhone app directory: " + relentrypath + " in " + appdirpath);
			}
			SakerDirectory entryoutdir = taskutils.resolveDirectoryAtRelativePathCreate(outputdir,
					relentrypath.getParent());
			DelegateSakerFile delegatef = new DelegateSakerFile(f);
			entryoutdir.add(delegatef);

			ContentDescriptor cd = f.getContentDescriptor();
			inputdependencies.put(entry.getValue(), cd);
			outputdependencies.put(delegatef.getSakerPath(), cd);
		}

		SakerFile[] embedprovisionfile = { null };
		SakerPath[] provisioningprofilepath = { null };
		provisioningProfile.accept(new FileLocationVisitor() {
			@Override
			public void visit(LocalFileLocation loc) {
				SakerPath path = loc.getLocalPath();
				ContentDescriptor cd = taskutils.getReportExecutionDependency(
						SakerStandardUtils.createLocalFileContentDescriptorExecutionProperty(path, UUID.randomUUID()));
				if (cd == null || cd instanceof DirectoryContentDescriptor) {
					throw ObjectUtils
							.sneakyThrow(new NoSuchFileException("Provisioning profile is not a file: " + path));
				}
				provisioningprofilepath[0] = path;
				try {
					embedprovisionfile[0] = taskutils.createProviderPathFile(EMBEDDED_MOBILEPROVISION_FILE_NAME,
							LocalFileProvider.getInstance().getPathKey(path));
				} catch (Exception e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}

			@Override
			public void visit(ExecutionFileLocation loc) {
				SakerPath path = loc.getPath();
				SakerFile f = taskutils.resolveFileAtPath(path);
				if (f == null) {
					throw ObjectUtils
							.sneakyThrow(new NoSuchFileException("Provisioning profile is not a file: " + path));
				}
				try {
					provisioningprofilepath[0] = SakerPath.valueOf(taskcontext.mirror(f));
					taskcontext.reportInputFileDependency(null, path, f.getContentDescriptor());
					embedprovisionfile[0] = new DelegateSakerFile(EMBEDDED_MOBILEPROVISION_FILE_NAME, f);
				} catch (Exception e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}
		});
		outputdir.add(embedprovisionfile[0]);

		CollectingProcessIOConsumer securityoutconsumer = new CollectingProcessIOConsumer();
		{
			SakerProcessBuilder pb = SakerProcessBuilder.create();
			pb.setCommand(ImmutableUtils.asUnmodifiableArrayList("security", "cms", "-D"));
			//TODO use -i <file> instead of redirected input
			pb.setStandardInputFile(provisioningprofilepath[0]);
			pb.setStandardOutputConsumer(securityoutconsumer);
			pb.setStandardErrorMerge(true);
			try (SakerProcess proc = pb.start()) {
				proc.processIO();
				int ec = proc.waitFor();
				if (ec != 0) {
					throw new IOException(
							"Failed to extract entitlements from provisioning profile. Command exited with: " + ec);
				}
			}
		}

		Object entitlementsobj;
		try (UnsyncByteArrayInputStream is = new UnsyncByteArrayInputStream(securityoutconsumer.getByteArrayRegion());
				Plist plist = Plist.readFrom(is)) {
			entitlementsobj = plist.get("Entitlements");
			if (!(entitlementsobj instanceof Map)) {
				throw new IllegalArgumentException(
						"Entitlements entry is not a dictionary in provisioning profile information: "
								+ entitlementsobj);
			}
		}
		byte[] xcentbytes;
		try (@SuppressWarnings("unchecked")
		Plist xcentplist = Plist.createWithContents((Map<String, ?>) entitlementsobj)) {
			xcentbytes = xcentplist.serialize(Plist.FORMAT_XML);
		}

		ByteArraySakerFile xcentfile = new ByteArraySakerFile(outputdir.getName() + ".xcent", xcentbytes);
		outputdir.getParent().add(xcentfile);

		Path xcentlocalpath = taskcontext.mirror(xcentfile);
		Path outputdirlocalpath = taskcontext.mirror(outputdir);
		Path filelistpath;
		LocalFileProvider localfp = LocalFileProvider.getInstance();
		{
			filelistpath = xcentlocalpath.resolveSibling(outputdir.getName() + ".file-list");
			//delete as the --file-list argument appends the file paths rather than clear the file contents
			localfp.delete(filelistpath);
			SakerProcessBuilder pb = SakerProcessBuilder.create();
			pb.setCommand(ImmutableUtils.asUnmodifiableArrayList("codesign", "--file-list", filelistpath.toString(),
					"--force", "-vvvv", "--sign", signingIdentity, "--entitlements", xcentlocalpath.toString(),
					outputdirlocalpath.toString()));
			pb.setStandardErrorMerge(true);
			CollectingProcessIOConsumer outconsumer = new CollectingProcessIOConsumer();
			pb.setStandardOutputConsumer(outconsumer);
			try (SakerProcess proc = pb.start()) {
				proc.processIO();
				int ec = proc.waitFor();
				if (ec != 0) {
					throw new IOException("Failed to sign application. Exit code: " + ec + " Path: " + appdirpath);
				}
			} finally {
				taskcontext.getStandardOut().write(outconsumer.getByteArrayRegion());
			}
		}
		taskcontext.invalidate(localfp.getPathKey(filelistpath));
		try (InputStream flin = localfp.openInputStream(filelistpath);
				InputStreamReader reader = new InputStreamReader(flin, StandardCharsets.UTF_8);
				BufferedReader br = new BufferedReader(reader)) {
			for (String line; (line = br.readLine()) != null;) {
				if (line.isEmpty()) {
					continue;
				}
				SakerPath unmirrorpath;
				ProviderHolderPathKey pathkey;
				try {
					SakerPath touchedfilepath = SakerPath.valueOf(line);
					Path realpath = LocalFileProvider.toRealPath(touchedfilepath);

					pathkey = localfp.getPathKey(realpath);
					taskcontext.invalidate(pathkey);
					unmirrorpath = taskcontext.getExecutionContext().toUnmirrorPath(realpath);
				} catch (InvalidPathFormatException | InvalidPathException e) {
					taskutils.reportIgnoredException(e);
					continue;
				}
				if (unmirrorpath == null) {
					//the touched file has no corresponding execution path
					//shouldn't happen, but check anyway
					continue;
				}
				if (unmirrorpath.startsWith(outputdirpath)) {
					SakerDirectory touchparentdir = taskutils.resolveDirectoryAtPathCreate(unmirrorpath.getParent());
					SakerFile providerfile = taskutils.createProviderPathFile(unmirrorpath.getFileName(), pathkey);
					touchparentdir.add(providerfile);
					
					outputdependencies.put(unmirrorpath, providerfile.getContentDescriptor());
				}
			}
		}
		outputdir.synchronize();

		taskutils.reportInputFileDependency(null, inputdependencies);
		taskutils.reportOutputFileDependency(null, outputdependencies);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(appDirectory);
		SerialUtils.writeExternalMap(out, mappings);
		out.writeObject(signingIdentity);
		out.writeObject(provisioningProfile);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		appDirectory = SerialUtils.readExternalObject(in);
		mappings = SerialUtils.readExternalSortedImmutableNavigableMap(in);
		signingIdentity = SerialUtils.readExternalObject(in);
		provisioningProfile = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((appDirectory == null) ? 0 : appDirectory.hashCode());
		result = prime * result + ((provisioningProfile == null) ? 0 : provisioningProfile.hashCode());
		result = prime * result + ((signingIdentity == null) ? 0 : signingIdentity.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SignIphoneOsWorkerTaskFactory other = (SignIphoneOsWorkerTaskFactory) obj;
		if (appDirectory == null) {
			if (other.appDirectory != null)
				return false;
		} else if (!appDirectory.equals(other.appDirectory))
			return false;
		if (mappings == null) {
			if (other.mappings != null)
				return false;
		} else if (!mappings.equals(other.mappings))
			return false;
		if (provisioningProfile == null) {
			if (other.provisioningProfile != null)
				return false;
		} else if (!provisioningProfile.equals(other.provisioningProfile))
			return false;
		if (signingIdentity == null) {
			if (other.signingIdentity != null)
				return false;
		} else if (!signingIdentity.equals(other.signingIdentity))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SignIphoneOsWorkerTaskFactory["
				+ (signingIdentity != null ? "signingIdentity=" + signingIdentity + ", " : "")
				+ (provisioningProfile != null ? "provisioningProfile=" + provisioningProfile : "") + "]";
	}

}
