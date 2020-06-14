package saker.apple.impl.lipo;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;
import java.util.UUID;

import saker.apple.api.SakerAppleUtils;
import saker.apple.api.lipo.LipoWorkerTaskOutput;
import saker.apple.main.lipo.LipoTaskFactory;
import saker.build.file.DirectoryVisitPredicate;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.content.DirectoryContentDescriptor;
import saker.build.file.path.ProviderHolderPathKey;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskExecutionUtilities.MirroredFileContents;
import saker.build.task.TaskFactory;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.trace.BuildTrace;
import saker.process.api.CollectingProcessIOConsumer;
import saker.process.api.SakerProcess;
import saker.process.api.SakerProcessBuilder;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.SDKSupportUtils;
import saker.sdk.support.api.exc.SDKPathNotFoundException;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.api.util.SakerStandardUtils;

public class LipoWorkerTaskFactory
		implements TaskFactory<LipoWorkerTaskOutput>, Task<LipoWorkerTaskOutput>, Externalizable {
	private static final long serialVersionUID = 1L;

	private List<FileLocation> inputs;

	private NavigableMap<String, ? extends SDKDescription> sdkDescriptions;

	/**
	 * For {@link Externalizable}.
	 */
	public LipoWorkerTaskFactory() {
	}

	public LipoWorkerTaskFactory(Collection<? extends FileLocation> inputs) {
		this.inputs = ImmutableUtils.makeImmutableList(inputs);
	}

	public void setSDKDescriptions(NavigableMap<String, ? extends SDKDescription> sdkdescriptions) {
		ObjectUtils.requireComparator(sdkdescriptions, SDKSupportUtils.getSDKNameComparator());
		this.sdkDescriptions = sdkdescriptions;
	}

	@Override
	public int getRequestedComputationTokenCount() {
		return 1;
	}

	@Override
	public LipoWorkerTaskOutput run(TaskContext taskcontext) throws Exception {
		LipoWorkerTaskIdentifier taskid = (LipoWorkerTaskIdentifier) taskcontext.getTaskId();
		SakerPath outputpath = taskid.getOutputPath();
		String fname = outputpath.getFileName();
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
			BuildTrace.setDisplayInformation("lipo:" + fname, LipoTaskFactory.TASK_NAME + ":" + fname);
		}
		taskcontext.setStandardOutDisplayIdentifier("lipo:" + fname);

		List<String> lipocommand = new ArrayList<>();

		NavigableMap<String, SDKReference> sdkrefs = SDKSupportUtils.resolveSDKReferences(taskcontext,
				this.sdkDescriptions);
		SDKReference liposdk = SDKSupportUtils.requireSDK(sdkrefs, SakerAppleUtils.SDK_NAME_LIPO);
		SakerPath exepath = liposdk.getPath(SakerAppleUtils.SDK_XCODE_EXECUTABLE_PATH_EXECUTABLE);
		if (exepath == null) {
			throw new SDKPathNotFoundException("lipo executable SDK path not found in: " + liposdk);
		}
		lipocommand.add(exepath.toString());
		lipocommand.add("-create");

		for (FileLocation inputFile : inputs) {
			inputFile.accept(new FileLocationVisitor() {
				@Override
				public void visit(ExecutionFileLocation loc) {
					SakerPath inputpath = loc.getPath();
					MirroredFileContents mirroredinputfile;
					try {
						mirroredinputfile = taskcontext.getTaskUtilities().mirrorFileAtPathContents(inputpath);
					} catch (IOException e) {
						taskcontext.reportInputFileDependency(null, inputpath,
								CommonTaskContentDescriptors.IS_NOT_FILE);
						NoSuchFileException nsfe = new NoSuchFileException(loc.toString());
						nsfe.initCause(e);
						throw ObjectUtils.sneakyThrow(nsfe);
					}
					taskcontext.reportInputFileDependency(null, inputpath, mirroredinputfile.getContents());
					lipocommand.add(mirroredinputfile.getPath().toString());
				}

				@Override
				public void visit(LocalFileLocation loc) {
					SakerPath inputpath = loc.getLocalPath();
					ExecutionProperty<? extends ContentDescriptor> envprop = SakerStandardUtils
							.createLocalFileContentDescriptorExecutionProperty(inputpath, UUID.randomUUID());
					lipocommand.add(inputpath.toString());
					ContentDescriptor cd = taskcontext.getTaskUtilities().getReportExecutionDependency(envprop);
					if (cd == null || cd instanceof DirectoryContentDescriptor) {
						throw ObjectUtils.sneakyThrow(new NoSuchFileException(inputpath + " is not a file."));
					}
				}
			});
		}

		SakerDirectory outputdir = taskcontext.getTaskUtilities().resolveDirectoryAtRelativePathCreate(
				SakerPathFiles.requireBuildDirectory(taskcontext), outputpath.getParent());

		Path outputfilelocalpath = taskcontext.mirror(outputdir, DirectoryVisitPredicate.synchronizeNothing())
				.resolve(fname);
		lipocommand.add("-output");
		lipocommand.add(outputfilelocalpath.toString());

		SakerProcessBuilder pb = SakerProcessBuilder.create();
		pb.setCommand(lipocommand);
		pb.setStandardErrorMerge(true);
		CollectingProcessIOConsumer outconsumer = new CollectingProcessIOConsumer();
		pb.setStandardOutputConsumer(outconsumer);
		try (SakerProcess proc = pb.start()) {
			proc.processIO();
			int ec = proc.waitFor();
			if (ec != 0) {
				throw new IOException("lipo failed: " + ec);
			}
		} finally {
			taskcontext.getStandardOut().write(outconsumer.getByteArrayRegion());
		}

		ProviderHolderPathKey outputfilepathkey = LocalFileProvider.getInstance().getPathKey(outputfilelocalpath);
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_013) {
			taskcontext.getTaskUtilities().invalidateWithPosixFilePermissions(outputfilepathkey);
		} else {
			taskcontext.invalidate(outputfilepathkey);
		}
		SakerFile outputfile = taskcontext.getTaskUtilities().createProviderPathFile(fname, outputfilepathkey);
		outputdir.add(outputfile);

		outputfile.synchronize();

		SakerPath outputabsolutepath = outputfile.getSakerPath();
		taskcontext.reportOutputFileDependency(null, outputabsolutepath, outputfile.getContentDescriptor());

		return new LipoTaskOutputImpl(outputabsolutepath);
	}

	@Override
	public Task<? extends LipoWorkerTaskOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, inputs);

		SerialUtils.writeExternalMap(out, sdkDescriptions);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		inputs = SerialUtils.readExternalImmutableList(in);

		sdkDescriptions = SerialUtils.readExternalSortedImmutableNavigableMap(in,
				SDKSupportUtils.getSDKNameComparator());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
		result = prime * result + ((sdkDescriptions == null) ? 0 : sdkDescriptions.hashCode());
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
		LipoWorkerTaskFactory other = (LipoWorkerTaskFactory) obj;
		if (inputs == null) {
			if (other.inputs != null)
				return false;
		} else if (!inputs.equals(other.inputs))
			return false;
		if (sdkDescriptions == null) {
			if (other.sdkDescriptions != null)
				return false;
		} else if (!sdkDescriptions.equals(other.sdkDescriptions))
			return false;
		return true;
	}
}
