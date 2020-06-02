package saker.apple.impl.plist;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.thirdparty.saker.util.io.SerialUtils;

public class ConvertPlistWorkerTaskIdentifier implements TaskIdentifier, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath relativeOutput;

	/**
	 * For {@link Externalizable}.
	 */
	public ConvertPlistWorkerTaskIdentifier() {
	}

	public ConvertPlistWorkerTaskIdentifier(SakerPath relativeOutput) {
		this.relativeOutput = relativeOutput;
	}

	public SakerPath getRelativeOutput() {
		return relativeOutput;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(relativeOutput);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		relativeOutput = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((relativeOutput == null) ? 0 : relativeOutput.hashCode());
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
		ConvertPlistWorkerTaskIdentifier other = (ConvertPlistWorkerTaskIdentifier) obj;
		if (relativeOutput == null) {
			if (other.relativeOutput != null)
				return false;
		} else if (!relativeOutput.equals(other.relativeOutput))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (relativeOutput != null ? "relativeOutput=" + relativeOutput : "")
				+ "]";
	}

}
