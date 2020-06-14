package saker.apple.api.lipo;

import saker.build.file.path.SakerPath;

/**
 * Output of the creation operation using the lipo tool.
 */
public interface LipoCreateWorkerTaskOutput {
	/**
	 * Gets the path of the universal file.
	 * 
	 * @return The absolute execution path.
	 */
	public SakerPath getPath();
}
