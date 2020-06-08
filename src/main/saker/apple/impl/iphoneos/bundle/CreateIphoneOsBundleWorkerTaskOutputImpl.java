package saker.apple.impl.iphoneos.bundle;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableMap;

import saker.apple.api.iphoneos.bundle.CreateIphoneOsBundleWorkerTaskOutput;
import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;

final class CreateIphoneOsBundleWorkerTaskOutputImpl implements CreateIphoneOsBundleWorkerTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath appDirectory;
	private NavigableMap<SakerPath, SakerPath> mappings;

	/**
	 * For {@link Externalizable}.
	 */
	public CreateIphoneOsBundleWorkerTaskOutputImpl() {
	}

	public CreateIphoneOsBundleWorkerTaskOutputImpl(SakerPath diroutpath,
			NavigableMap<SakerPath, SakerPath> mappingpaths) {
		this.appDirectory = diroutpath;
		this.mappings = mappingpaths;
	}

	@Override
	public SakerPath getAppDirectory() {
		return appDirectory;
	}

	@Override
	public NavigableMap<SakerPath, SakerPath> getMappings() {
		return mappings;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(appDirectory);
		SerialUtils.writeExternalMap(out, mappings);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		appDirectory = SerialUtils.readExternalObject(in);
		mappings = SerialUtils.readExternalSortedImmutableNavigableMap(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((appDirectory == null) ? 0 : appDirectory.hashCode());
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
		CreateIphoneOsBundleWorkerTaskOutputImpl other = (CreateIphoneOsBundleWorkerTaskOutputImpl) obj;
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
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + appDirectory + "]";
	}
}