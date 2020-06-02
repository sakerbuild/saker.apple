package saker.apple.impl.macos;

import java.io.BufferedReader;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.charset.StandardCharsets;
import java.util.List;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import saker.process.api.CollectingProcessIOConsumer;
import saker.process.api.SakerProcess;
import saker.process.api.SakerProcessBuilder;

public class MacOsSwVersInformation implements Externalizable {
	private static final long serialVersionUID = 1L;

	private String productName;
	private String productVersion;
	private String buildVersion;

	/**
	 * For {@link Externalizable}.
	 */
	public MacOsSwVersInformation() {
	}

	public static MacOsSwVersInformation fromSwVersProcess() throws IOException {
		SakerProcessBuilder pb = SakerProcessBuilder.create();
		List<String> command = ImmutableUtils.asUnmodifiableArrayList("sw_vers");
		pb.setCommand(command);
		pb.setStandardErrorMerge(true);
		CollectingProcessIOConsumer outconsumer = new CollectingProcessIOConsumer();
		pb.setStandardOutputConsumer(outconsumer);

		try (SakerProcess proc = pb.start()) {
			proc.processIO();
			int ec = proc.waitFor();
			if (ec != 0) {
				throw new IOException(
						"Failed to execute " + StringUtils.toStringJoin(" ", command) + ". Exit code: " + ec);
			}
		} catch (InterruptedException e) {
			throw new IOException("Failed to wait for process.", e);
		} catch (Throwable e) {
			try {
				outconsumer.getByteArrayRegion().writeTo(System.out);
			} catch (Exception e2) {
				e.addSuppressed(e2);
			}
			throw e;
		}
		MacOsSwVersInformation result = new MacOsSwVersInformation();
		try (UnsyncByteArrayInputStream bais = new UnsyncByteArrayInputStream(outconsumer.getByteArrayRegion());
				BufferedReader reader = new BufferedReader(new InputStreamReader(bais, StandardCharsets.UTF_8))) {
			for (String line; (line = reader.readLine()) != null;) {
				if (line.isEmpty()) {
					continue;
				}
				if (line.startsWith("ProductName: ")) {
					result.productName = line.substring(13).trim();
				} else if (line.startsWith("ProductVersion: ")) {
					result.productName = line.substring(16).trim();
				} else if (line.startsWith("BuildVersion: ")) {
					result.productName = line.substring(14).trim();
				}
			}
		}
		return result;
	}

	public String getProductName() {
		return productName;
	}

	public String getProductVersion() {
		return productVersion;
	}

	public String getBuildVersion() {
		return buildVersion;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(productName);
		out.writeObject(productVersion);
		out.writeObject(buildVersion);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		productName = SerialUtils.readExternalObject(in);
		productVersion = SerialUtils.readExternalObject(in);
		buildVersion = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((buildVersion == null) ? 0 : buildVersion.hashCode());
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
		MacOsSwVersInformation other = (MacOsSwVersInformation) obj;
		if (buildVersion == null) {
			if (other.buildVersion != null)
				return false;
		} else if (!buildVersion.equals(other.buildVersion))
			return false;
		if (productName == null) {
			if (other.productName != null)
				return false;
		} else if (!productName.equals(other.productName))
			return false;
		if (productVersion == null) {
			if (other.productVersion != null)
				return false;
		} else if (!productVersion.equals(other.productVersion))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (productName != null ? "productName=" + productName + ", " : "")
				+ (productVersion != null ? "productVersion=" + productVersion + ", " : "")
				+ (buildVersion != null ? "buildVersion=" + buildVersion : "") + "]";
	}
}
