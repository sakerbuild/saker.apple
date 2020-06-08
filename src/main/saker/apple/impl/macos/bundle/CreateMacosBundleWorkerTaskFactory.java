package saker.apple.impl.macos.bundle;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableMap;
import java.util.NavigableSet;

import saker.apple.api.macos.bundle.CreateMacosBundleWorkerTaskOutput;
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

public class CreateMacosBundleWorkerTaskFactory implements TaskFactory<CreateMacosBundleWorkerTaskOutput>,
		Task<CreateMacosBundleWorkerTaskOutput>, Externalizable {
	private static final long serialVersionUID = 1L;

	private TaskFactory<? extends PrepareDirectoryWorkerTaskOutput> prepareTaskFactory;

	/**
	 * For {@link Externalizable}.
	 */
	public CreateMacosBundleWorkerTaskFactory() {
	}

	public CreateMacosBundleWorkerTaskFactory(NavigableMap<SakerPath, FileLocation> resources) {
		this(SakerStandardTaskUtils.createPrepareDirectoryTaskFactory(resources));
	}

	public CreateMacosBundleWorkerTaskFactory(
			TaskFactory<? extends PrepareDirectoryWorkerTaskOutput> prepareTaskFactory) {
		this.prepareTaskFactory = prepareTaskFactory;
	}

	@Override
	public Task<? extends CreateMacosBundleWorkerTaskOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public CreateMacosBundleWorkerTaskOutput run(TaskContext taskcontext) throws Exception {
		CrateMacosBundleWorkerTaskIdentifier taskid = (CrateMacosBundleWorkerTaskIdentifier) taskcontext.getTaskId();

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

		CreateMacosBundleWorkerTaskOutputImpl result = new CreateMacosBundleWorkerTaskOutputImpl(diroutpath,
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
		CreateMacosBundleWorkerTaskFactory other = (CreateMacosBundleWorkerTaskFactory) obj;
		if (prepareTaskFactory == null) {
			if (other.prepareTaskFactory != null)
				return false;
		} else if (!prepareTaskFactory.equals(other.prepareTaskFactory))
			return false;
		return true;
	}

}
