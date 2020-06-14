package saker.apple.impl.plist;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

import saker.apple.api.plist.InsertPlistWorkerTaskOutput;
import saker.apple.impl.plist.lib.Plist;
import saker.apple.main.plist.InsertPlistTaskFactory;
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
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKPathReference;
import saker.sdk.support.api.SDKPropertyReference;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.SDKSupportUtils;
import saker.sdk.support.api.exc.SDKPathNotFoundException;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.api.util.SakerStandardUtils;

public class InsertPlistWorkerTaskFactory
		implements TaskFactory<InsertPlistWorkerTaskOutput>, Task<InsertPlistWorkerTaskOutput>, Externalizable {
	private static final long serialVersionUID = 1L;

	private FileLocation input;
	private String format;
	private NavigableMap<String, PlistValueOption> values;
	private NavigableMap<String, SDKDescription> sdkDescriptions;

	/**
	 * For {@link Externalizable}.
	 */
	public InsertPlistWorkerTaskFactory() {
	}

	public InsertPlistWorkerTaskFactory(FileLocation input, String format,
			NavigableMap<String, PlistValueOption> values) {
		this.input = input;
		this.format = format;
		this.values = values;
	}

	public void setSdkDescriptions(NavigableMap<String, SDKDescription> sdkdescriptions) {
		ObjectUtils.requireComparator(sdkdescriptions, SDKSupportUtils.getSDKNameComparator());
		this.sdkDescriptions = sdkdescriptions;
	}

	//TODO make cluster dispatchable

	@Override
	public int getRequestedComputationTokenCount() {
		return 1;
	}

	private int getPlistFormat() {
		if (format == null) {
			return Plist.FORMAT_SAME_AS_INPUT;
		}
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

	public static Plist getPlistReportDependencyForFileLocation(TaskContext taskcontext, FileLocation input)
			throws IOException {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		IOSupplier<? extends Plist>[] plistsupplier = new IOSupplier[] { null };
		input.accept(new FileLocationVisitor() {
			@Override
			public void visit(LocalFileLocation loc) {
				SakerPath path = loc.getLocalPath();
				ContentDescriptor cd = taskcontext.getTaskUtilities().getReportExecutionDependency(
						SakerStandardUtils.createLocalFileContentDescriptorExecutionProperty(path, UUID.randomUUID()));
				if (cd == null || cd instanceof DirectoryContentDescriptor) {
					throw ObjectUtils.sneakyThrow(new NoSuchFieldError("Not a file: " + path));
				}
				plistsupplier[0] = () -> {
					try (InputStream is = LocalFileProvider.getInstance().openInputStream(path)) {
						return Plist.readFrom(is);
					}
				};
			}

			@Override
			public void visit(ExecutionFileLocation loc) {
				SakerPath path = loc.getPath();
				SakerFile f = taskcontext.getTaskUtilities().resolveFileAtPath(path);
				if (f == null) {
					throw ObjectUtils.sneakyThrow(new NoSuchFieldError("Not a file: " + path));
				}
				taskcontext.reportInputFileDependency(null, path, f.getContentDescriptor());
				plistsupplier[0] = () -> {
					try (InputStream is = f.openInputStream()) {
						return Plist.readFrom(is);
					}
				};
			}
		});
		return plistsupplier[0].get();
	}

	@Override
	public InsertPlistWorkerTaskOutput run(TaskContext taskcontext) throws Exception {
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
		}
		taskcontext.setStandardOutDisplayIdentifier(InsertPlistTaskFactory.TASK_NAME);

		InsertPlistWorkerTaskIdentifier taskid = (InsertPlistWorkerTaskIdentifier) taskcontext.getTaskId();

		SakerPath relativeoutputpath = taskid.getRelativeOutput();
		TaskExecutionUtilities taskutils = taskcontext.getTaskUtilities();
		SakerDirectory outputdir = taskutils.resolveDirectoryAtRelativePathCreate(
				SakerPathFiles.requireBuildDirectory(taskcontext), relativeoutputpath.getParent());

		NavigableMap<String, SDKReference> sdks = SDKSupportUtils.resolveSDKReferences(taskcontext, sdkDescriptions);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		IOSupplier<? extends Plist>[] plistsupplier = new IOSupplier[] { null };
		if (input == null) {
			plistsupplier[0] = Plist::createEmpty;
		} else {
			plistsupplier[0] = () -> getPlistReportDependencyForFileLocation(taskcontext, input);
		}
		String outputfilename = relativeoutputpath.getFileName();

		ByteArraySakerFile outfile;
		int actualformat;
		try (Plist plist = plistsupplier[0].get()) {
			for (Entry<String, PlistValueOption> entry : values.entrySet()) {
				String key = entry.getKey();
				PlistValueOption valoption = entry.getValue();
				if (valoption == null) {
					plist.remove(key);
				} else {
					Object valobj = toObject(valoption, sdks);
					plist.set(key, valobj);
				}
			}
			actualformat = getPlistFormat();
			byte[] serialized = plist.serialize(actualformat);
			outfile = new ByteArraySakerFile(outputfilename, serialized);
			if (actualformat == Plist.FORMAT_SAME_AS_INPUT) {
				actualformat = plist.getFormat();
			}
		}
		outputdir.add(outfile);
		outfile.synchronize();

		SakerPath outputsakerpath = outfile.getSakerPath();
		taskcontext.reportOutputFileDependency(null, outputsakerpath, outfile.getContentDescriptor());

		String strformat;
		switch (actualformat) {
			case Plist.FORMAT_BINARY: {
				strformat = "binary1";
				break;
			}
			case Plist.FORMAT_XML: {
				strformat = "xml1";
				break;
			}
			default: {
				throw new UnsupportedOperationException("Unrecognized plist format: " + actualformat);
			}
		}
		InsertPlistWorkerTaskOutputImpl result = new InsertPlistWorkerTaskOutputImpl(outputsakerpath, strformat);
		taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
		return result;
	}

	private static Object toObject(PlistValueOption opt, Map<String, ? extends SDKReference> sdks) throws Exception {
		Objects.requireNonNull(opt, "plist value");
		Object val = opt.getValue();
		if (val instanceof String) {
			return val;
		} else if (val instanceof Boolean) {
			return val;
		} else if (val instanceof Long) {
			return val;
		} else if (val instanceof Double) {
			return val;
		} else if (val instanceof SDKPathReference) {
			SakerPath path = ((SDKPathReference) val).getValue(sdks);
			if (path == null) {
				throw new SDKPathNotFoundException("SDK path not found: " + val);
			}
			return path.toString();
		} else if (val instanceof SDKPropertyReference) {
			String prop = ((SDKPropertyReference) val).getValue(sdks);
			if (prop == null) {
				throw new SDKPathNotFoundException("SDK property not found: " + val);
			}
			return prop;
		} else if (val instanceof List<?>) {
			@SuppressWarnings("unchecked")
			List<? extends PlistValueOption> list = (List<? extends PlistValueOption>) val;
			Object[] array = list.toArray();
			for (int i = 0; i < array.length; i++) {
				PlistValueOption o = (PlistValueOption) array[i];
				array[i] = toObject(o, sdks);
			}
			return array;
		} else if (val instanceof Map<?, ?>) {
			@SuppressWarnings("unchecked")
			Map<String, ? extends PlistValueOption> map = (Map<String, ? extends PlistValueOption>) val;
			Map<String, Object> valmap = new TreeMap<>();
			for (Entry<String, ? extends PlistValueOption> entry : map.entrySet()) {
				valmap.put(entry.getKey(), toObject(entry.getValue(), sdks));
			}
			return valmap;
		} else {
			throw new IllegalArgumentException("Unsupported plist value: " + val);
		}
	}

	@Override
	public Task<? extends InsertPlistWorkerTaskOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(input);
		out.writeObject(format);
		SerialUtils.writeExternalMap(out, values);
		SerialUtils.writeExternalMap(out, sdkDescriptions);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		input = SerialUtils.readExternalObject(in);
		format = SerialUtils.readExternalObject(in);
		values = SerialUtils.readExternalSortedImmutableNavigableMap(in);
		sdkDescriptions = SerialUtils.readExternalSortedImmutableNavigableMap(in,
				SDKSupportUtils.getSDKNameComparator());
	}

	@Override
	public int hashCode() {
		return input == null ? Objects.hashCode(values) : input.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InsertPlistWorkerTaskFactory other = (InsertPlistWorkerTaskFactory) obj;
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
		if (sdkDescriptions == null) {
			if (other.sdkDescriptions != null)
				return false;
		} else if (!sdkDescriptions.equals(other.sdkDescriptions))
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (input != null ? "input=" + input + ", " : "")
				+ (format != null ? "format=" + format : "") + "]";
	}

}
