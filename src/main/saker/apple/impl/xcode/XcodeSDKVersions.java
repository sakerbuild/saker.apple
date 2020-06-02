package saker.apple.impl.xcode;

import java.io.BufferedReader;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import saker.process.api.CollectingProcessIOConsumer;
import saker.process.api.SakerProcess;
import saker.process.api.SakerProcessBuilder;

public class XcodeSDKVersions implements Externalizable {
	private static final long serialVersionUID = 1L;

	private XcodeVersionInformation xcodeVersionInformation;
	private NavigableMap<String, ApplePlatformSDKInformation> sdkInformations;

	/**
	 * For {@link Externalizable}.
	 */
	public XcodeSDKVersions() {
	}

	private XcodeSDKVersions(XcodeVersionInformation xcodeVersionInformation,
			NavigableMap<String, ApplePlatformSDKInformation> sdksInformations) {
		this.xcodeVersionInformation = xcodeVersionInformation;
		this.sdkInformations = sdksInformations;
	}

	public static XcodeSDKVersions fromXcodebuildVersionSdkProcess() throws IOException {
		SakerProcessBuilder pb = SakerProcessBuilder.create();
		List<String> command = ImmutableUtils.asUnmodifiableArrayList("xcodebuild", "-version", "-sdk");
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

		NavigableMap<String, ApplePlatformSDKInformation> sdkinfos = new TreeMap<>();

		String xcodeversionnum = null;
		String xcodebuildversion = null;
		try (UnsyncByteArrayInputStream bais = new UnsyncByteArrayInputStream(outconsumer.getByteArrayRegion());
				BufferedReader reader = new BufferedReader(new InputStreamReader(bais, StandardCharsets.UTF_8))) {
			ApplePlatformSDKInformation sdkinfo = null;
			for (String line; (line = reader.readLine()) != null;) {
				if (line.isEmpty()) {
					if (sdkinfo != null) {
						ApplePlatformSDKInformation prev = sdkinfos.put(sdkinfo.getName(), sdkinfo);
						if (prev != null) {
							throw new IOException("Duplicate SDKs detected with name: " + sdkinfo.getName());
						}
					}
					sdkinfo = null;
					continue;
				}
				if (sdkinfo == null) {
					if (StringUtils.startsWithIgnoreCase(line, "Xcode ")) {
						//the xcode version number
						xcodeversionnum = line.substring(6).trim();
						continue;
					}
					if (StringUtils.startsWithIgnoreCase(line, "Build version ")) {
						xcodebuildversion = line.substring(14).trim();
						continue;
					}
					if (!line.endsWith(")")) {
						throw new IOException("Failed to determine SDK type from line: " + line);
					}
					int parenstart = line.lastIndexOf('(');
					if (parenstart < 0) {
						throw new IOException("Failed to determine SDK type from line: " + line);
					}
					sdkinfo = new ApplePlatformSDKInformation(line.substring(parenstart + 1, line.length() - 1));
					continue;
				}
				if (line.startsWith("SDKVersion: ")) {
					sdkinfo.setSDKVersion(line.substring(12));
				} else if (line.startsWith("Path: ")) {
					sdkinfo.setPath(SakerPath.valueOf(line.substring(6)));
				} else if (line.startsWith("PlatformVersion: ")) {
					sdkinfo.setPlatformVersion(line.substring(17));
				} else if (line.startsWith("PlatformPath: ")) {
					sdkinfo.setPlatformPath(SakerPath.valueOf(line.substring(14)));
				} else if (line.startsWith("ProductBuildVersion: ")) {
					sdkinfo.setProductBuildVersion(line.substring(21));
				} else if (line.startsWith("ProductCopyright: ")) {
					sdkinfo.setProductCopyright(line.substring(18));
				} else if (line.startsWith("ProductName: ")) {
					sdkinfo.setProductName(line.substring(13));
				} else if (line.startsWith("ProductVersion: ")) {
					sdkinfo.setProductVersion(line.substring(16));
				} else if (line.startsWith("ProductUserVisibleVersion: ")) {
					sdkinfo.setProductUserVisibleVersion(line.substring(27));
				}
			}
		}
		if (ObjectUtils.isNullOrEmpty(xcodeversionnum) || ObjectUtils.isNullOrEmpty(xcodebuildversion)) {
			throw new IOException("Failed to determine Xcode version.");
		}
		XcodeVersionInformation xcodeversion = new XcodeVersionInformation(xcodeversionnum, xcodebuildversion);
		return new XcodeSDKVersions(xcodeversion, ImmutableUtils.makeImmutableNavigableMap(sdkinfos));
	}

	public NavigableMap<String, ApplePlatformSDKInformation> getSDKInformations() {
		return sdkInformations;
	}

	public XcodeVersionInformation getXcodeVersionInformation() {
		return xcodeVersionInformation;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(xcodeVersionInformation);
		SerialUtils.writeExternalMap(out, sdkInformations);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		xcodeVersionInformation = SerialUtils.readExternalObject(in);
		sdkInformations = SerialUtils.readExternalSortedImmutableNavigableMap(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((xcodeVersionInformation == null) ? 0 : xcodeVersionInformation.hashCode());
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
		XcodeSDKVersions other = (XcodeSDKVersions) obj;
		if (sdkInformations == null) {
			if (other.sdkInformations != null)
				return false;
		} else if (!sdkInformations.equals(other.sdkInformations))
			return false;
		if (xcodeVersionInformation == null) {
			if (other.xcodeVersionInformation != null)
				return false;
		} else if (!xcodeVersionInformation.equals(other.xcodeVersionInformation))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "XcodeSDKVersions["
				+ (xcodeVersionInformation != null ? "xcodeVersionInformation=" + xcodeVersionInformation : "") + "]";
	}

	//example output: (cropped)

//	iPhoneOS10.2.sdk - iOS 10.2 (iphoneos10.2)
//	SDKVersion: 10.2
//	Path: /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS10.2.sdk
//	PlatformVersion: 10.2
//	PlatformPath: /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform
//	ProductBuildVersion: 14C89
//	ProductCopyright: 1983-2016 Apple Inc.
//	ProductName: iPhone OS
//	ProductVersion: 10.2
//
//	iPhoneSimulator10.2.sdk - Simulator - iOS 10.2 (iphonesimulator10.2)
//	SDKVersion: 10.2
//	Path: /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator10.2.sdk
//	PlatformVersion: 10.2
//	PlatformPath: /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneSimulator.platform
//	ProductBuildVersion: 14C89
//	ProductCopyright: 1983-2016 Apple Inc.
//	ProductName: iPhone OS
//	ProductVersion: 10.2
//
//	MacOSX10.12.sdk - macOS 10.12 (macosx10.12)
//	SDKVersion: 10.12
//	Path: /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.12.sdk
//	PlatformVersion: 1.1
//	PlatformPath: /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform
//	ProductBuildVersion: 16C58
//	ProductCopyright: 1983-2016 Apple Inc.
//	ProductName: Mac OS X
//	ProductUserVisibleVersion: 10.12.2
//	ProductVersion: 10.12.2
//	
//	Xcode 8.2.1
//	Build version 8C1002 
}
