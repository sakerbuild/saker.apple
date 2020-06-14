package saker.apple.main.plist;

import java.util.Locale;

import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;

@NestInformation(value = "Format name of a property list. (plist)")
@NestTypeInformation(qualifiedName = "PlistFormatOption",
		enumValues = {

				@NestFieldInformation(value = "xml1", info = @NestInformation("XML format version 1.0.")),
				@NestFieldInformation(value = "binary1", info = @NestInformation("Binary format version 1.0.")),

		})
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
