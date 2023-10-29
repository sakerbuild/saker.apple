package testing.saker.apple.internals;

import java.nio.file.Path;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import saker.apple.impl.xcode.ApplePlatformSDKInformation;
import saker.apple.impl.xcode.XcodeSDKVersions;
import saker.build.file.provider.LocalFileProvider;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;
import testing.saker.build.tests.EnvironmentTestCase;

@SakerTest
public class XcodeSDKVersionsOutputTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		Path workingdir = getWorkingDirectory();
		System.out.println("XcodeSDKVersionsOutputTest.runTest() " + workingdir);

		//sanity checks a sample output as it strangely includes macosx13.1 twice:
		//MacOSX13.1.sdk - macOS 13.1 (macosx13.1)
		//MacOSX13.sdk - macOS 13.1 (macosx13.1)
		XcodeSDKVersions test_duplicatemacosx = XcodeSDKVersions.parseXcodebuildProcessOutput(
				LocalFileProvider.getInstance().getAllBytes(workingdir.resolve("test_duplicatemacosx")));
		NavigableMap<String, ApplePlatformSDKInformation> infos = new TreeMap<>();
		for (ApplePlatformSDKInformation sdkinfo : test_duplicatemacosx.getSDKInformations()) {
			infos.put(sdkinfo.getHeaderSDKName(), sdkinfo);
		}
		assertEquals(infos.get("MacOSX13.sdk").getName(), "macosx13.1");
		assertEquals(infos.get("MacOSX13.1.sdk").getName(), "macosx13.1");

		if ("libtest.dylib".equals(System.mapLibraryName("test"))) {
			//if we're running the test on macOS (based on the library name mapping, then test by
			//running the query through the process as well
			XcodeSDKVersions.fromXcodebuildVersionSdkProcess();
		}
	}

	private Path getWorkingDirectory() {
		return EnvironmentTestCase.getTestingBaseWorkingDirectory().resolve(getClass().getName().replace('.', '/'));
	}

}
