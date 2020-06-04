package saker.apple.impl.strip;

import java.io.Externalizable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.Path;
import java.util.NavigableMap;
import java.util.UUID;

import saker.apple.api.SakerAppleUtils;
import saker.apple.api.strip.StripWorkerTaskOutput;
import saker.apple.main.strip.StripTaskFactory;
import saker.build.file.DirectoryVisitPredicate;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
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

public class StripWorkerTaskFactory
		implements TaskFactory<StripWorkerTaskOutput>, Task<StripWorkerTaskOutput>, Externalizable {
	private static final long serialVersionUID = 1L;

	//TODO the code in this class is very similar to the Android NDK strip worker task. common code could be exported to the standard package

	private FileLocation inputFile;

	private NavigableMap<String, ? extends SDKDescription> sdkDescriptions;

	/**
	 * For {@link Externalizable}.
	 */
	public StripWorkerTaskFactory() {
	}

	public void setInputFile(FileLocation inputFile) {
		this.inputFile = inputFile;
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
	public StripWorkerTaskOutput run(TaskContext taskcontext) throws Exception {
		StripWorkerTaskIdentifier taskid = (StripWorkerTaskIdentifier) taskcontext.getTaskId();
		SakerPath outputpath = taskid.getOutputPath();
		String fname = outputpath.getFileName();
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
			BuildTrace.setDisplayInformation("strip:" + fname, StripTaskFactory.TASK_NAME + ":" + fname);
		}
		taskcontext.setStandardOutDisplayIdentifier("strip:" + fname);

		Path[] inputfilelocalpath = { null };
		ContentDescriptor[] inputfilecd = { null };
		inputFile.accept(new FileLocationVisitor() {
			@Override
			public void visit(ExecutionFileLocation loc) {
				SakerPath inputpath = loc.getPath();
				MirroredFileContents mirroredinputfile;
				try {
					mirroredinputfile = taskcontext.getTaskUtilities().mirrorFileAtPathContents(inputpath);
				} catch (IOException e) {
					taskcontext.reportInputFileDependency(null, inputpath, CommonTaskContentDescriptors.IS_NOT_FILE);
					FileNotFoundException fnfe = new FileNotFoundException(loc.toString());
					fnfe.initCause(e);
					ObjectUtils.sneakyThrow(fnfe);
					return;
				}
				inputfilecd[0] = mirroredinputfile.getContents();
				taskcontext.reportInputFileDependency(null, inputpath, mirroredinputfile.getContents());
				inputfilelocalpath[0] = mirroredinputfile.getPath();
			}

			@Override
			public void visit(LocalFileLocation loc) {
				SakerPath inputpath = loc.getLocalPath();
				ExecutionProperty<? extends ContentDescriptor> envprop = SakerStandardUtils
						.createLocalFileContentDescriptorExecutionProperty(inputpath, UUID.randomUUID());
				inputfilelocalpath[0] = LocalFileProvider.toRealPath(inputpath);
				inputfilecd[0] = taskcontext.getTaskUtilities().getReportExecutionDependency(envprop);
			}
		});

		SakerDirectory outputdir = taskcontext.getTaskUtilities().resolveDirectoryAtRelativePathCreate(
				SakerPathFiles.requireBuildDirectory(taskcontext).getDirectoryCreate(StripTaskFactory.TASK_NAME),
				outputpath.getParent());

		NavigableMap<String, SDKReference> sdkrefs = SDKSupportUtils.resolveSDKReferences(taskcontext,
				this.sdkDescriptions);
		SDKReference stripsdk = SDKSupportUtils.requireSDK(sdkrefs, SakerAppleUtils.SDK_NAME_STRIP);
		SakerPath exepath = stripsdk.getPath(SakerAppleUtils.SDK_XCODE_EXECUTABLE_PATH_EXECUTABLE);
		if (exepath == null) {
			throw new SDKPathNotFoundException("strip executable SDK path not found in: " + stripsdk);
		}

		Path outputfilelocalpath = taskcontext.mirror(outputdir, DirectoryVisitPredicate.synchronizeNothing())
				.resolve(fname);

		SakerProcessBuilder pb = SakerProcessBuilder.create();
		//TODO other flags
		pb.setCommand(ImmutableUtils.asUnmodifiableArrayList(exepath.toString(), "-o", outputfilelocalpath.toString(),
				inputfilelocalpath[0].toString()));
		pb.setStandardErrorMerge(true);
		CollectingProcessIOConsumer outconsumer = new CollectingProcessIOConsumer();
		pb.setStandardOutputConsumer(outconsumer);
		try (SakerProcess proc = pb.start()) {
			proc.processIO();
			int ec = proc.waitFor();
			if (ec != 0) {
				throw new IOException("strip failed: " + ec);
			}
		} finally {
			taskcontext.getStandardOut().write(outconsumer.getByteArrayRegion());
		}

		ProviderHolderPathKey outputfilepathkey = LocalFileProvider.getInstance().getPathKey(outputfilelocalpath);
		taskcontext.invalidate(outputfilepathkey);
		ContentDescriptor outputfilecd = new StrippedBinaryContentDescriptor(inputfilecd[0]);
		SakerFile outputfile = taskcontext.getTaskUtilities().createProviderPathFile(fname, outputfilepathkey,
				outputfilecd);
		outputdir.add(outputfile);

		outputfile.synchronize();

		SakerPath outputabsolutepath = outputfile.getSakerPath();
		taskcontext.reportOutputFileDependency(null, outputabsolutepath, outputfilecd);

		return new StripTaskOutputImpl(outputabsolutepath);
	}

	@Override
	public Task<? extends StripWorkerTaskOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(inputFile);

		SerialUtils.writeExternalMap(out, sdkDescriptions);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		inputFile = (FileLocation) in.readObject();

		sdkDescriptions = SerialUtils.readExternalSortedImmutableNavigableMap(in,
				SDKSupportUtils.getSDKNameComparator());
	}
}
