package saker.apple.impl.xcode;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.thirdparty.saker.util.io.SerialUtils;

public class XcodeVersionInformation implements Externalizable {
	private static final long serialVersionUID = 1L;

	private String version;
	private String buildVersion;

	/**
	 * For {@link Externalizable}.
	 */
	public XcodeVersionInformation() {
	}

	public XcodeVersionInformation(String version, String buildVersion) {
		this.version = version;
		this.buildVersion = buildVersion;
	}

	public String getVersion() {
		return version;
	}

	public String getBuildVersion() {
		return buildVersion;
	}

	public String getVersionAsDTXcode() {
		//TODO this needs to be tested on versions 10 and above
		String replaced = version.replace(".", "");
		if (replaced.length() < 4) {
			return "0" + replaced;
		}
		return replaced;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(version);
		out.writeObject(buildVersion);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		version = SerialUtils.readExternalObject(in);
		buildVersion = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((buildVersion == null) ? 0 : buildVersion.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		XcodeVersionInformation other = (XcodeVersionInformation) obj;
		if (buildVersion == null) {
			if (other.buildVersion != null)
				return false;
		} else if (!buildVersion.equals(other.buildVersion))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "XcodeVersionInformation[" + (version != null ? "version=" + version + ", " : "")
				+ (buildVersion != null ? "buildVersion=" + buildVersion : "") + "]";
	}

}
