package saker.apple.impl.plist;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.apple.api.plist.InsertPlistWorkerTaskOutput;
import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;

final class InsertPlistWorkerTaskOutputImpl implements InsertPlistWorkerTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath outputPath;

	private String format;

	/**
	 * For {@link Externalizable}.
	 */
	public InsertPlistWorkerTaskOutputImpl() {
	}

	public InsertPlistWorkerTaskOutputImpl(SakerPath outputPath, String format) {
		this.outputPath = outputPath;
		this.format = format;
	}

	@Override
	public SakerPath getPath() {
		return outputPath;
	}

	@Override
	public String getFormat() {
		return format;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(outputPath);
		out.writeObject(format);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		outputPath = SerialUtils.readExternalObject(in);
		format = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((outputPath == null) ? 0 : outputPath.hashCode());
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
		InsertPlistWorkerTaskOutputImpl other = (InsertPlistWorkerTaskOutputImpl) obj;
		if (format == null) {
			if (other.format != null)
				return false;
		} else if (!format.equals(other.format))
			return false;
		if (outputPath == null) {
			if (other.outputPath != null)
				return false;
		} else if (!outputPath.equals(other.outputPath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ConvertPlistWorkerTaskOutputImpl[" + (outputPath != null ? "outputPath=" + outputPath + ", " : "")
				+ (format != null ? "format=" + format : "") + "]";
	}

}