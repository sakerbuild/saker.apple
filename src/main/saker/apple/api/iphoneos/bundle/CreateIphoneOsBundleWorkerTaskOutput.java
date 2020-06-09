package saker.apple.api.iphoneos.bundle;

import java.util.NavigableMap;

import saker.build.file.path.SakerPath;

public interface CreateIphoneOsBundleWorkerTaskOutput {
	public SakerPath getAppDirectory();

	//relative bundle paths to absolute execution paths
	//files only
	public NavigableMap<SakerPath, SakerPath> getMappings();
}
