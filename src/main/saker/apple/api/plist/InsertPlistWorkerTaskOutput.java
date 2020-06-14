package saker.apple.api.plist;

import saker.build.file.path.SakerPath;

/**
 * Output of the plist value insertion task.
 */
public interface InsertPlistWorkerTaskOutput {
	/**
	 * Gets the path to the output plist.
	 * 
	 * @return The absolute execution path.
	 */
	public SakerPath getPath();

	/**
	 * Gets the format identifier of the output plist.
	 * 
	 * @return The format.
	 */
	public String getFormat();
}
