package saker.apple.impl.plist.lib;

public class PlatformLib {
	public static final boolean LOADED;
	public static final Throwable loadCause;
	static {
		boolean loaded;
		Throwable cause = null;
		try {
			System.loadLibrary("appleplatform");
			loaded = true;
		} catch (LinkageError | Exception e) {
			System.err.println("Failed to load native platform library of saker.apple-impl: " + e);
			loaded = false;
			cause = e;
		}
		loadCause = cause;
		LOADED = loaded;
	}
}
