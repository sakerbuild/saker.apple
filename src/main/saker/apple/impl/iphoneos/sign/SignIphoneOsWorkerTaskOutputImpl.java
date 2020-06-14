package saker.apple.impl.iphoneos.sign;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.apple.api.iphoneos.sign.SignIphoneOsWorkerTaskOutput;
import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;

final class SignIphoneOsWorkerTaskOutputImpl implements SignIphoneOsWorkerTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath appDirectory;

	/**
	 * For {@link Externalizable}.
	 */
	public SignIphoneOsWorkerTaskOutputImpl() {
	}

	public SignIphoneOsWorkerTaskOutputImpl(SakerPath appDirectory) {
		this.appDirectory = appDirectory;
	}

	@Override
	public SakerPath getAppDirectory() {
		return appDirectory;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(appDirectory);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		appDirectory = SerialUtils.readExternalObject(in);
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
		SignIphoneOsWorkerTaskOutputImpl other = (SignIphoneOsWorkerTaskOutputImpl) obj;
		if (appDirectory == null) {
			if (other.appDirectory != null)
				return false;
		} else if (!appDirectory.equals(other.appDirectory))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (appDirectory != null ? "appDirectory=" + appDirectory : "") + "]";
	}

}