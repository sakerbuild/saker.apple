package saker.apple.impl.plist;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import saker.apple.api.plist.ConvertPlistWorkerTaskOutput;
import saker.apple.impl.plist.lib.Plist;
import saker.apple.main.plist.ConvertPlistTaskFactory;
import saker.build.file.ByteArraySakerFile;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.content.DirectoryContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskExecutionUtilities;
import saker.build.task.TaskFactory;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.thirdparty.saker.util.io.function.IOSupplier;
import saker.build.trace.BuildTrace;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.api.util.SakerStandardUtils;

public class ConvertPlistWorkerTaskFactory
		implements TaskFactory<ConvertPlistWorkerTaskOutput>, Task<ConvertPlistWorkerTaskOutput>, Externalizable {
	private static final long serialVersionUID = 1L;

	private FileLocation input;
	private String format;

	/**
	 * For {@link Externalizable}.
	 */
	public ConvertPlistWorkerTaskFactory() {
	}

	public ConvertPlistWorkerTaskFactory(FileLocation input, String format) {
		this.input = input;
		this.format = format;
	}

	@Override
	public int getRequestedComputationTokenCount() {
		return 1;
	}

	private int getPlistFormat() {
		switch (format) {
			case "binary1": {
				return Plist.FORMAT_BINARY;
			}
			case "xml1": {
				return Plist.FORMAT_XML;
			}
			default: {
				throw new IllegalArgumentException("Unsupported format: " + format);
			}
		}
	}

	@Override
	public ConvertPlistWorkerTaskOutput run(TaskContext taskcontext) throws Exception {
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
		}
		taskcontext.setStandardOutDisplayIdentifier(ConvertPlistTaskFactory.TASK_NAME);

		ConvertPlistWorkerTaskIdentifier taskid = (ConvertPlistWorkerTaskIdentifier) taskcontext.getTaskId();

		SakerPath relativeoutputpath = taskid.getRelativeOutput();
		TaskExecutionUtilities taskutils = taskcontext.getTaskUtilities();
		SakerDirectory outputdir = taskutils.resolveDirectoryAtRelativePathCreate(
				SakerPathFiles.requireBuildDirectory(taskcontext), relativeoutputpath.getParent());

		@SuppressWarnings({ "unchecked", "rawtypes" })
		IOSupplier<? extends InputStream>[] streamsupplier = new IOSupplier[] { null };
		input.accept(new FileLocationVisitor() {
			@Override
			public void visit(LocalFileLocation loc) {
				SakerPath path = loc.getLocalPath();
				ContentDescriptor cd = taskutils.getReportExecutionDependency(
						SakerStandardUtils.createLocalFileContentDescriptorExecutionProperty(path, UUID.randomUUID()));
				if (cd == null || cd instanceof DirectoryContentDescriptor) {
					throw ObjectUtils.sneakyThrow(new NoSuchFieldError("Not a file: " + path));
				}
				streamsupplier[0] = () -> LocalFileProvider.getInstance().openInputStream(path);
			}

			@Override
			public void visit(ExecutionFileLocation loc) {
				SakerPath path = loc.getPath();
				SakerFile f = taskutils.resolveFileAtPath(path);
				if (f == null) {
					throw ObjectUtils.sneakyThrow(new NoSuchFieldError("Not a file: " + path));
				}
				taskcontext.reportInputFileDependency(null, path, f.getContentDescriptor());
				streamsupplier[0] = f::openInputStream;
			}
		});
		String outputfilename = relativeoutputpath.getFileName();

		ByteArraySakerFile outfile;
		try (InputStream is = streamsupplier[0].get()) {
			try (Plist plist = Plist.readFrom(is)) {
				byte[] serialized = plist.serialize(getPlistFormat());
				outfile = new ByteArraySakerFile(outputfilename, serialized);
			}
		}
		outputdir.add(outfile);
		outfile.synchronize();

		SakerPath outputsakerpath = outfile.getSakerPath();
		taskcontext.reportOutputFileDependency(null, outputsakerpath, outfile.getContentDescriptor());

		ConvertPlistWorkerTaskOutputImpl result = new ConvertPlistWorkerTaskOutputImpl(outputsakerpath, format);
		taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
		return result;
	}

	@Override
	public Task<? extends ConvertPlistWorkerTaskOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(input);
		out.writeObject(format);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		input = SerialUtils.readExternalObject(in);
		format = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((format == null) ? 0 : format.hashCode());
		result = prime * result + ((input == null) ? 0 : input.hashCode());
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
		ConvertPlistWorkerTaskFactory other = (ConvertPlistWorkerTaskFactory) obj;
		if (format == null) {
			if (other.format != null)
				return false;
		} else if (!format.equals(other.format))
			return false;
		if (input == null) {
			if (other.input != null)
				return false;
		} else if (!input.equals(other.input))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ConvertPlistWorkerTaskFactory[" + (input != null ? "input=" + input + ", " : "")
				+ (format != null ? "format=" + format : "") + "]";
	}

}
