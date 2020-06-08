package saker.apple.api.macos.bundle;

import java.util.NavigableMap;

import saker.build.file.path.SakerPath;

public interface CreateMacOsBundleWorkerTaskOutput {
	public SakerPath getAppDirectory();

	//relative bundle paths to absolute execution paths
	public NavigableMap<SakerPath, SakerPath> getMappings();
}
