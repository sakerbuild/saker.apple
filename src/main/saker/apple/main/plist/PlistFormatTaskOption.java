package saker.apple.main.plist;

import java.util.Locale;

public class PlistFormatTaskOption {
	public static final String FORMAT_XML1 = "xml1";
	public static final String FORMAT_BINARY1 = "binary1";

	public static final PlistFormatTaskOption INSTANCE_BINARY1 = new PlistFormatTaskOption(FORMAT_BINARY1);

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
			case FORMAT_BINARY1: {
				return new PlistFormatTaskOption(FORMAT_BINARY1);
			}
			case "xml":
			case FORMAT_XML1: {
				return new PlistFormatTaskOption(FORMAT_XML1);
			}
			default: {
				throw new IllegalArgumentException("Unrecognized plist format: " + input);
			}
		}
	}
}
