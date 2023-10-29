package saker.apple.impl.xcode;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;

public class ApplePlatformSDKInformation implements Externalizable {
	private static final long serialVersionUID = 1L;

	private String name;
	private String sdkVersion;
	private SakerPath path;
	private String platformVersion;
	private SakerPath platformPath;
	private String productBuildVersion;
	private String productCopyright;
	private String productName;
	private String productUserVisibleVersion;
	private String productVersion;

	/**
	 * For {@link Externalizable}.
	 */
	public ApplePlatformSDKInformation() {
	}

	public ApplePlatformSDKInformation(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	 * Gets the simple name part of the {@linkplain #getName() SDK name}.
	 * <p>
	 * E.g. the simple name of <code>iphoneos10.2</code> is <code>iphoneos</code>.
	 * 
	 * @return The simple name.
	 */
	public String getSimpleName() {
		StringBuilder sb = new StringBuilder();
		int len = name.length();
		for (int i = 0; i < len; i++) {
			char c = name.charAt(i);
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
				sb.append(c);
			} else {
				break;
			}
		}
		return sb.toString();
	}

	void setName(String name) {
		this.name = name;
	}

	public String getSDKVersion() {
		return sdkVersion;
	}

	void setSDKVersion(String sdkVersion) {
		this.sdkVersion = sdkVersion;
	}

	public SakerPath getPath() {
		return path;
	}

	void setPath(SakerPath path) {
		this.path = path;
	}

	public String getPlatformVersion() {
		return platformVersion;
	}

	void setPlatformVersion(String platformVersion) {
		this.platformVersion = platformVersion;
	}

	public SakerPath getPlatformPath() {
		return platformPath;
	}

	void setPlatformPath(SakerPath platformPath) {
		this.platformPath = platformPath;
	}

	public String getProductBuildVersion() {
		return productBuildVersion;
	}

	void setProductBuildVersion(String productBuildVersion) {
		this.productBuildVersion = productBuildVersion;
	}

	public String getProductCopyright() {
		return productCopyright;
	}

	void setProductCopyright(String productCopyright) {
		this.productCopyright = productCopyright;
	}

	public String getProductName() {
		return productName;
	}

	void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductUserVisibleVersion() {
		return productUserVisibleVersion;
	}

	void setProductUserVisibleVersion(String productUserVisibleVersion) {
		this.productUserVisibleVersion = productUserVisibleVersion;
	}

	public String getProductVersion() {
		return productVersion;
	}

	void setProductVersion(String productVersion) {
		this.productVersion = productVersion;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(name);
		out.writeObject(sdkVersion);
		out.writeObject(path);
		out.writeObject(platformVersion);
		out.writeObject(platformPath);
		out.writeObject(productBuildVersion);
		out.writeObject(productCopyright);
		out.writeObject(productName);
		out.writeObject(productUserVisibleVersion);
		out.writeObject(productVersion);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		name = SerialUtils.readExternalObject(in);
		sdkVersion = SerialUtils.readExternalObject(in);
		path = SerialUtils.readExternalObject(in);
		platformVersion = SerialUtils.readExternalObject(in);
		platformPath = SerialUtils.readExternalObject(in);
		productBuildVersion = SerialUtils.readExternalObject(in);
		productCopyright = SerialUtils.readExternalObject(in);
		productName = SerialUtils.readExternalObject(in);
		productUserVisibleVersion = SerialUtils.readExternalObject(in);
		productVersion = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		return ((name == null) ? 0 : name.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ApplePlatformSDKInformation other = (ApplePlatformSDKInformation) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (platformPath == null) {
			if (other.platformPath != null)
				return false;
		} else if (!platformPath.equals(other.platformPath))
			return false;
		if (platformVersion == null) {
			if (other.platformVersion != null)
				return false;
		} else if (!platformVersion.equals(other.platformVersion))
			return false;
		if (productBuildVersion == null) {
			if (other.productBuildVersion != null)
				return false;
		} else if (!productBuildVersion.equals(other.productBuildVersion))
			return false;
		if (productCopyright == null) {
			if (other.productCopyright != null)
				return false;
		} else if (!productCopyright.equals(other.productCopyright))
			return false;
		if (productName == null) {
			if (other.productName != null)
				return false;
		} else if (!productName.equals(other.productName))
			return false;
		if (productUserVisibleVersion == null) {
			if (other.productUserVisibleVersion != null)
				return false;
		} else if (!productUserVisibleVersion.equals(other.productUserVisibleVersion))
			return false;
		if (productVersion == null) {
			if (other.productVersion != null)
				return false;
		} else if (!productVersion.equals(other.productVersion))
			return false;
		if (sdkVersion == null) {
			if (other.sdkVersion != null)
				return false;
		} else if (!sdkVersion.equals(other.sdkVersion))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[");
		if (name != null) {
			builder.append("name=");
			builder.append(name);
			builder.append(", ");
		}
		if (sdkVersion != null) {
			builder.append("sdkVersion=");
			builder.append(sdkVersion);
			builder.append(", ");
		}
		if (path != null) {
			builder.append("path=");
			builder.append(path);
			builder.append(", ");
		}
		if (platformVersion != null) {
			builder.append("platformVersion=");
			builder.append(platformVersion);
			builder.append(", ");
		}
		if (platformPath != null) {
			builder.append("platformPath=");
			builder.append(platformPath);
			builder.append(", ");
		}
		if (productBuildVersion != null) {
			builder.append("productBuildVersion=");
			builder.append(productBuildVersion);
			builder.append(", ");
		}
		if (productCopyright != null) {
			builder.append("productCopyright=");
			builder.append(productCopyright);
			builder.append(", ");
		}
		if (productName != null) {
			builder.append("productName=");
			builder.append(productName);
			builder.append(", ");
		}
		if (productUserVisibleVersion != null) {
			builder.append("productUserVisibleVersion=");
			builder.append(productUserVisibleVersion);
			builder.append(", ");
		}
		if (productVersion != null) {
			builder.append("productVersion=");
			builder.append(productVersion);
		}
		builder.append("]");
		return builder.toString();
	}
}
