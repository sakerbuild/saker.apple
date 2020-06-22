package saker.apple.main;

import java.util.Map;
import java.util.Set;

import saker.apple.main.plist.InsertPlistTaskFactory;
import saker.apple.main.plist.PlistFormatTaskOption;
import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.sdk.support.main.option.SDKDescriptionTaskOption;

public class TaskDocs {
	public static final String SDKS = "Specifies the SDKs (Software Development Kits) used by the task.\n"
			+ "SDKs represent development kits that are available in the build environment and to the task.\n"
			+ "The SDK names are compared in a case-insensitive way.";

	@NestTypeInformation(qualifiedName = "InsertPlistWorkerTaskOutput")
	@NestInformation("Output of the plist value insertion task.")
	@NestFieldInformation(value = "Path",
			type = @NestTypeUsage(SakerPath.class),
			info = @NestInformation("The output path of the modified plist."))
	@NestFieldInformation(value = "Format",
			type = @NestTypeUsage(PlistFormatTaskOption.class),
			info = @NestInformation("The output format of the plist."))
	public static class DocInsertPlistWorkerTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "ConvertPlistWorkerTaskOutput")
	@NestInformation("Output of the plist converting task.")
	@NestFieldInformation(value = "Path",
			type = @NestTypeUsage(SakerPath.class),
			info = @NestInformation("The output path of the converted plist."))
	@NestFieldInformation(value = "Format",
			type = @NestTypeUsage(PlistFormatTaskOption.class),
			info = @NestInformation("The output format of the plist."))
	public static class DocConvertPlistWorkerTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "LipoCreateWorkerTaskOutput")
	@NestInformation("Output of the creation operation using the lipo tool.")
	@NestFieldInformation(value = "Path",
			type = @NestTypeUsage(SakerPath.class),
			info = @NestInformation("The output path of the universal file."))
	public static class DocLipoCreateWorkerTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "AppleOptionsPreset")
	@NestInformation("Output configuration for creating applications for Apple platforms.")
	@NestFieldInformation(value = "InfoPlistValues",
			type = @NestTypeUsage(value = Map.class, elementTypes = { String.class, Object.class }),
			info = @NestInformation("Map of values that are to be inserted in the application Info.plist file.\n"
					+ "The SDKs should also be passed to the " + InsertPlistTaskFactory.TASK_NAME + "() task if used."))
	@NestFieldInformation(value = "SDKs",
			type = @NestTypeUsage(value = Map.class,
					elementTypes = { saker.sdk.support.main.TaskDocs.DocSdkNameOption.class,
							SDKDescriptionTaskOption.class }),
			info = @NestInformation("Map of SDKs that are part of the configuration.\n"
					+ "These SDKs are part of the clang options and need not to be passed explicitly. However, "
					+ "they should be passed to the " + InsertPlistTaskFactory.TASK_NAME + "() task if used."))
	public static class DocAppleOptionsPreset {
	}

	@NestInformation("Name of an Apple development target platform.")
	@NestTypeInformation(qualifiedName = "ApplePlatformOption",
			enumValues = {

					@NestFieldInformation(value = "iphoneos", info = @NestInformation("The iPhone OS platform.")),
					@NestFieldInformation(value = "iphonesimulator",
							info = @NestInformation("The iPhone simulator platform.")),
					@NestFieldInformation(value = "macosx", info = @NestInformation("The macOS platform.")),
					@NestFieldInformation(value = "appletvos", info = @NestInformation("The Apple TV OS platform.")),
					@NestFieldInformation(value = "appletvsimulator",
							info = @NestInformation("The Apple TV simulator platform.")),
					@NestFieldInformation(value = "watchos", info = @NestInformation("The watchOS platform.")),
					@NestFieldInformation(value = "watchsimulator",
							info = @NestInformation("The watchOS simulator platform.")),

			})
	public static class DocApplePlatformOption {
		public static final Set<String> KNOWN_PLATFORMS = ImmutableUtils.makeImmutableNavigableSet(new String[] {
				"iphoneos", "iphonesimulator", "macosx", "appletvos", "appletvsimulator", "watchos", "watchsimulator",
				//macos is also recognized by us, but converted to macosx internally
				"macos", });
	}

	@NestInformation("Name of an Apple development target architecture.")
	@NestTypeInformation(qualifiedName = "AppleArchitecture",
			enumValues = {

					@NestFieldInformation(value = "x86_64",
							info = @NestInformation("The architecture x86_64. It is generally used by macOS, and simulator platforms.")),
					@NestFieldInformation(value = "armv7",
							info = @NestInformation("The architecture armv7. It is generally used by iPhone OS.")),
					@NestFieldInformation(value = "arm64",
							info = @NestInformation("The architecture arm64. It is generally used by Apple TV OS, and iPhone OS.")),
					@NestFieldInformation(value = "i386",
							info = @NestInformation("The architecture i386. It is generally used by macOS, and simulator platforms.")),
					@NestFieldInformation(value = "armv7k",
							info = @NestInformation("The architecture armv7k. It is generally used by watchOS.")),
					@NestFieldInformation(value = "armv7s",
							info = @NestInformation("The architecture armv7s. It is generally used by iPhone OS.")),
					@NestFieldInformation(value = "arm64_32",
							info = @NestInformation("The architecture arm64_32. It is generally used by watchOS.")),
					@NestFieldInformation(value = "arm64e",
							info = @NestInformation("The architecture arm64e. It is generally used by iPhone OS.")),

			})
	public static class DocAppleArchitecture {
	}

	@NestTypeInformation(qualifiedName = "StripWorkerTaskOutput")
	@NestInformation("Worker task output of the strip tool invocation.")
	@NestFieldInformation(value = "Path",
			type = @NestTypeUsage(SakerPath.class),
			info = @NestInformation("The output path of the stripped binary."))
	public static class DocStripWorkerTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "CreateIphoneOsBundleWorkerTaskOutput")
	@NestInformation("Output of the iPhone application bundle creation task.")
	@NestFieldInformation(value = "AppDirectory",
			type = @NestTypeUsage(SakerPath.class),
			info = @NestInformation("The path of the output application directory."))
	@NestFieldInformation(value = "Mappings",
			type = @NestTypeUsage(value = Map.class, elementTypes = { SakerPath.class, SakerPath.class }),
			info = @NestInformation("The mappings of the application contents.\n"
					+ "The field contains relative keys which represent the path of a file in the application bundle. The associated "
					+ "values are the absolute execution paths where the files reside in the build system."))
	public static class DocCreateIphoneOsBundleWorkerTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "CreateMacOsBundleWorkerTaskOutput")
	@NestInformation("Output of the macOS application bundle creation task.")
	@NestFieldInformation(value = "AppDirectory",
			type = @NestTypeUsage(SakerPath.class),
			info = @NestInformation("The path of the output application directory."))
	@NestFieldInformation(value = "Mappings",
			type = @NestTypeUsage(value = Map.class, elementTypes = { SakerPath.class, SakerPath.class }),
			info = @NestInformation("The mappings of the application contents.\n"
					+ "The field contains relative keys which represent the path of a file in the application bundle. The associated "
					+ "values are the absolute execution paths where the files reside in the build system."))
	public static class DocCreateMacOsBundleWorkerTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "SignIphoneOsWorkerTaskOutput")
	@NestInformation("Output of the iPhone application signing task.")
	@NestFieldInformation(value = "AppDirectory",
			type = @NestTypeUsage(SakerPath.class),
			info = @NestInformation("The path of the signed application directory."))
	public static class DocSignIphoneOsWorkerTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "DeviceFamily",
			enumValues = {

					@NestFieldInformation(value = "iPhone", info = @NestInformation("The iPhone device family.")),
					@NestFieldInformation(value = "iPad", info = @NestInformation("The iPad device family.")),
					@NestFieldInformation(value = "tvOS", info = @NestInformation("The tvOS device family.")),
					@NestFieldInformation(value = "watchOS", info = @NestInformation("The watchOS device family.")),

			})
	@NestInformation("An Apple device family enumeration.")
	public static class DocDeviceFamilyOption {
	}
}
