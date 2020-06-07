package saker.apple.api.plist;

import saker.build.file.path.SakerPath;

public interface InsertPlistWorkerTaskOutput {
	public SakerPath getPath();

	public String getFormat();
}
