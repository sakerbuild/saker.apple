package saker.apple.impl.xcode;

import java.io.BufferedReader;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
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
						"Failed to execute '" + StringUtils.toStringJoin(" ", command) + "'. Exit code: " + ec);
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

		ByteArrayRegion outputbytes = outconsumer.getByteArrayRegion();
		return parseXcodebuildProcessOutput(outputbytes);
	}

	public static XcodeSDKVersions parseXcodebuildProcessOutput(ByteArrayRegion outputbytes) throws IOException {
		NavigableMap<String, ApplePlatformSDKInformation> sdkinfos = new TreeMap<>();

		String xcodeversionnum = null;
		String xcodebuildversion = null;
		try (UnsyncByteArrayInputStream bais = new UnsyncByteArrayInputStream(outputbytes);
				BufferedReader reader = new BufferedReader(new InputStreamReader(bais, StandardCharsets.UTF_8))) {
			ApplePlatformSDKInformation sdkinfo = null;
			for (String line; (line = reader.readLine()) != null;) {
				if (line.isEmpty()) {
					if (sdkinfo != null) {
						//use the hader SDK name for the key, as the name might be duplicated in some strange case 
						String key = sdkinfo.getHeaderSDKName();
						ApplePlatformSDKInformation prev = sdkinfos.putIfAbsent(key, sdkinfo);
						if (prev != null) {
							//make the exception contain all data so we're able to investigate the issue if the error happens
							//on a system we don't have direct access to
							throw new IOException("Duplicate SDKs detected with name: " + key + " new: " + sdkinfo
									+ " prev: " + prev + " in:\n" + outputbytes);
						}
						sdkinfo = null;
					}
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
					int parenend = line.indexOf(')', parenstart + 1);
					if (parenend < 0) {
						throw new IOException("Failed to determine SDK type from line: " + line);
					}
					int dashidx = line.indexOf('-');
					if (dashidx < 0) {
						dashidx = parenstart;
					}
					String name = line.substring(parenstart + 1, parenend);
					String headersdkname = line.substring(0, dashidx).trim();
					sdkinfo = new ApplePlatformSDKInformation(headersdkname, name);
					continue;
				}
				if (line.startsWith("SDKVersion:")) {
					sdkinfo.setSDKVersion(line.substring(11).trim());
				} else if (line.startsWith("Path:")) {
					sdkinfo.setPath(SakerPath.valueOf(line.substring(5).trim()));
				} else if (line.startsWith("PlatformVersion:")) {
					sdkinfo.setPlatformVersion(line.substring(16).trim());
				} else if (line.startsWith("PlatformPath:")) {
					sdkinfo.setPlatformPath(SakerPath.valueOf(line.substring(13).trim()));
				} else if (line.startsWith("ProductBuildVersion:")) {
					sdkinfo.setProductBuildVersion(line.substring(20).trim());
				} else if (line.startsWith("ProductCopyright:")) {
					sdkinfo.setProductCopyright(line.substring(17).trim());
				} else if (line.startsWith("ProductName:")) {
					sdkinfo.setProductName(line.substring(12).trim());
				} else if (line.startsWith("ProductVersion:")) {
					sdkinfo.setProductVersion(line.substring(15).trim());
				} else if (line.startsWith("ProductUserVisibleVersion:")) {
					sdkinfo.setProductUserVisibleVersion(line.substring(26).trim());
				}
			}
		}
		if (ObjectUtils.isNullOrEmpty(xcodeversionnum) || ObjectUtils.isNullOrEmpty(xcodebuildversion)) {
			//make the exception contain all data so we're able to investigate the issue if the error happens
			//on a system we don't have direct access to
			throw new IOException(
					"Failed to determine Xcode version. Properties not found in 'xcodebuild -version -sdk' process output:\n"
							+ outputbytes);
		}
		XcodeVersionInformation xcodeversion = new XcodeVersionInformation(xcodeversionnum, xcodebuildversion);
		return new XcodeSDKVersions(xcodeversion, ImmutableUtils.makeImmutableNavigableMap(sdkinfos));
	}

	public Collection<? extends ApplePlatformSDKInformation> getSDKInformations() {
		return sdkInformations.values();
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
