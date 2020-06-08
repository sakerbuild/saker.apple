package saker.apple.impl.macos.bundle;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableMap;
import java.util.NavigableSet;

import saker.apple.api.macos.bundle.CreateMacOsBundleWorkerTaskOutput;
import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.SetTransformingNavigableMap;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.std.api.dir.prepare.PrepareDirectoryWorkerTaskOutput;
import saker.std.api.file.location.FileLocation;
import saker.std.api.util.SakerStandardTaskUtils;

public class CreateMacOsBundleWorkerTaskFactory implements TaskFactory<CreateMacOsBundleWorkerTaskOutput>,
		Task<CreateMacOsBundleWorkerTaskOutput>, Externalizable {
	private static final long serialVersionUID = 1L;

	private TaskFactory<? extends PrepareDirectoryWorkerTaskOutput> prepareTaskFactory;

	/**
	 * For {@link Externalizable}.
	 */
	public CreateMacOsBundleWorkerTaskFactory() {
	}

	public CreateMacOsBundleWorkerTaskFactory(NavigableMap<SakerPath, FileLocation> resources) {
		this(SakerStandardTaskUtils.createPrepareDirectoryTaskFactory(resources));
	}

	public CreateMacOsBundleWorkerTaskFactory(
			TaskFactory<? extends PrepareDirectoryWorkerTaskOutput> prepareTaskFactory) {
		this.prepareTaskFactory = prepareTaskFactory;
	}

	@Override
	public Task<? extends CreateMacOsBundleWorkerTaskOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public CreateMacOsBundleWorkerTaskOutput run(TaskContext taskcontext) throws Exception {
		CrateMacOsBundleWorkerTaskIdentifier taskid = (CrateMacOsBundleWorkerTaskIdentifier) taskcontext.getTaskId();

		PrepareDirectoryWorkerTaskOutput prepareout = taskcontext.getTaskUtilities().runTaskResult(
				SakerStandardTaskUtils.createPrepareDirectoryTaskIdentifier(taskid.getOutputPath()),
				prepareTaskFactory);

		SakerPath diroutpath = prepareout.getOutputPath();
		NavigableSet<SakerPath> outfilepaths = prepareout.getFilePaths();

		int diroutnamecount = diroutpath.getNameCount();
		NavigableMap<SakerPath, SakerPath> mappingpaths = ImmutableUtils.makeImmutableNavigableMap(
				new SetTransformingNavigableMap<SakerPath, SakerPath, SakerPath>(outfilepaths) {
					@Override
					protected Entry<SakerPath, SakerPath> transformEntry(SakerPath e) {
						return ImmutableUtils.makeImmutableMapEntry(e.subPath(diroutnamecount), e);
					}
				});

		CreateMacOsBundleWorkerTaskOutputImpl result = new CreateMacOsBundleWorkerTaskOutputImpl(diroutpath,
				mappingpaths);
		taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
		return result;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(prepareTaskFactory);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		prepareTaskFactory = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((prepareTaskFactory == null) ? 0 : prepareTaskFactory.hashCode());
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
		CreateMacOsBundleWorkerTaskFactory other = (CreateMacOsBundleWorkerTaskFactory) obj;
		if (prepareTaskFactory == null) {
			if (other.prepareTaskFactory != null)
				return false;
		} else if (!prepareTaskFactory.equals(other.prepareTaskFactory))
			return false;
		return true;
	}

}
