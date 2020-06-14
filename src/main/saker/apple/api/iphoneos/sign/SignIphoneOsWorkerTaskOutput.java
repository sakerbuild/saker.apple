package saker.apple.api.iphoneos.sign;

import saker.build.file.path.SakerPath;

/**
 * Output of the iPhone application signing task.
 */
public interface SignIphoneOsWorkerTaskOutput {
	/**
	 * Gets the application directory path of the signed application.
	 * 
	 * @return The absolute execution path.
	 */
	public SakerPath getAppDirectory();
}
