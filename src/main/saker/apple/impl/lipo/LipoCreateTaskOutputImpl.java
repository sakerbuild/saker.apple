package saker.apple.impl.lipo;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.apple.api.lipo.LipoCreateWorkerTaskOutput;
import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;

final class LipoCreateTaskOutputImpl implements LipoCreateWorkerTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath path;

	/**
	 * For {@link Externalizable}.
	 */
	public LipoCreateTaskOutputImpl() {
	}

	public LipoCreateTaskOutputImpl(SakerPath path) {
		this.path = path;
	}

	@Override
	public SakerPath getPath() {
		return path;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(path);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		path = SerialUtils.readExternalObject(in);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + path + "]";
	}

}