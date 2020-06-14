package saker.apple.api.strip;

import saker.build.file.path.SakerPath;

/**
 * Output of the worker task of invoking the strip tool.
 * <p>
 * The interface provides access to the output path of the stripped binary file.
 * <p>
 * Clients shouldn't implement this interface.
 */
public interface StripWorkerTaskOutput {
	/**
	 * Gets the output path of the stripped binary.
	 * 
	 * @return The absolute execution path.
	 */
	public SakerPath getPath();
}
