package saker.apple.main.plist;

import java.util.Locale;

public class PlistFormatTaskOption {
	public static final PlistFormatTaskOption INSTANCE_BINARY1 = new PlistFormatTaskOption("binary1");

	private String format;

	public PlistFormatTaskOption(String format) {
		this.format = format;
	}

	public String getFormat() {
		return format;
	}

	public static PlistFormatTaskOption valueOf(String input) {
		switch (input.toLowerCase(Locale.ENGLISH)) {
			case "binary":
			case "binary1": {
				return new PlistFormatTaskOption("binary1");
			}
			case "xml":
			case "xml1": {
				return new PlistFormatTaskOption("xml1");
			}
			default: {
				throw new IllegalArgumentException("Unrecognized plist format: " + input);
			}
		}
	}
}
