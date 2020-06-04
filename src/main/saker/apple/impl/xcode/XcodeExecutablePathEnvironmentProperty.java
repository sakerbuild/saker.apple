package saker.apple.impl.xcode;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.path.SakerPath;
import saker.build.runtime.environment.EnvironmentProperty;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.process.api.CollectingProcessIOConsumer;
import saker.process.api.SakerProcess;
import saker.process.api.SakerProcessBuilder;

public class XcodeExecutablePathEnvironmentProperty implements EnvironmentProperty<SakerPath>, Externalizable {
	private static final long serialVersionUID = 1L;

	private String executableName;

	/**
	 * For {@link Externalizable}.
	 */
	public XcodeExecutablePathEnvironmentProperty() {
	}

	public XcodeExecutablePathEnvironmentProperty(String executableName) {
		this.executableName = executableName;
	}

	@Override
	public SakerPath getCurrentValue(SakerEnvironment environment) throws Exception {
		SakerProcessBuilder pb = SakerProcessBuilder.create();
		List<String> cmd = ImmutableUtils.asUnmodifiableArrayList("xcodebuild", "-find-executable", executableName);
		pb.setCommand(cmd);
		pb.setStandardErrorMerge(true);
		CollectingProcessIOConsumer outconsumer = new CollectingProcessIOConsumer();
		pb.setStandardOutputConsumer(outconsumer);

		try (SakerProcess proc = pb.start()) {
			proc.processIO();
			int ec = proc.waitFor();
			if (ec != 0) {
				throw new IOException("Failed to find " + executableName + ". Exit code: " + ec + ". Output: "
						+ outconsumer.getOutputString());
			}
		}
		String outstr = outconsumer.getOutputString().trim();
		try {
			return SakerPath.valueOf(outstr);
		} catch (InvalidPathFormatException e) {
			throw new RuntimeException("Failed to parse executable path: " + outstr, e);
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(executableName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		executableName = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((executableName == null) ? 0 : executableName.hashCode());
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
		XcodeExecutablePathEnvironmentProperty other = (XcodeExecutablePathEnvironmentProperty) obj;
		if (executableName == null) {
			if (other.executableName != null)
				return false;
		} else if (!executableName.equals(other.executableName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (executableName != null ? "executableName=" + executableName : "")
				+ "]";
	}

}
