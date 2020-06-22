package saker.apple.main.preset;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import saker.apple.api.SakerAppleUtils;
import saker.apple.impl.plist.PlistValueOption;
import saker.apple.impl.sdk.VersionsXcodeSDKDescription;
import saker.apple.main.TaskDocs.DocAppleArchitecture;
import saker.apple.main.TaskDocs.DocAppleOptionsPreset;
import saker.apple.main.TaskDocs.DocApplePlatformOption;
import saker.apple.main.TaskDocs.DocDeviceFamilyOption;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.trace.BuildTrace;
import saker.clang.main.options.ClangPresetTaskOption;
import saker.compiler.utils.api.CompilationIdentifier;
import saker.compiler.utils.main.CompilationIdentifierTaskOption;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKPathReference;
import saker.sdk.support.api.SDKPropertyReference;
import saker.sdk.support.api.SDKSupportUtils;
import saker.sdk.support.main.SDKSupportFrontendUtils;
import saker.sdk.support.main.option.SDKDescriptionTaskOption;

@NestTaskInformation(returnType = @NestTypeUsage(DocAppleOptionsPreset.class))
@NestInformation("Create a configuration for compiling and creating applications for various Apple platforms.\n"
		+ "The result of this task can be passed to the saker.clang.compile() and saker.clang.link() tasks "
		+ "for their CompilerOptions and LinkerOptions parameters to configure them appropriately to create "
		+ "applications for the specified platforms.\n"
		+ "The InfoPlistValues field of the result can be used to fill the required plist values for your application.")

@NestParameterInformation(value = "Platform",
		aliases = "",
		type = @NestTypeUsage(DocApplePlatformOption.class),
		info = @NestInformation("Specifies the development target platform.\n"
				+ "The target platform determines some of the clang parameters as well as various "
				+ "values that are to be present in the Info.plist file for the application.\n"
				+ "A default SDK will also be added based on the platform (if not already present)."))
@NestParameterInformation(value = "PlatformMinVersion",
		type = @NestTypeUsage(String.class),
		info = @NestInformation("Specifies the minimum platform version that the application requires.\n"
				+ "The minimum version causes the appropriate -m<PLATFORM>-version-min parameter to be added "
				+ "to the clang arguments, as well as adds requirements for the application Info.plist."))
@NestParameterInformation(value = "Architecture",
		type = @NestTypeUsage(DocAppleArchitecture.class),
		info = @NestInformation("Specifies the target architecture for the compilation.\n"
				+ "The value of this parameter will be added as a clang argument for the -arch option.\n"
				+ "If not specified, a default one is inferred based on the Platform. If set to null, no architecture "
				+ "based configuration is added."))
@NestParameterInformation(value = "Release",
		type = @NestTypeUsage(boolean.class),
		info = @NestInformation("Specifies the kind of optimization related configuration that should be used.\n"
				+ "If this parameter is set to true, the configuration will use options for release optimization. "
				+ "Otherwise it is configured for debugging.\n"
				+ "If set to null, no optimization related options are used.\n" + "The default is false."))
@NestParameterInformation(value = "AutomaticReferenceCounting",
		aliases = "ARC",
		type = @NestTypeUsage(boolean.class),
		info = @NestInformation("Sets whether or not automatic reference counting should be used.\n"
				+ "If set to true, -fobjc-arc argument is added to clang.\n" + "The default is true."))
@NestParameterInformation(value = "Libraries",
		type = @NestTypeUsage(value = Collection.class, elementTypes = String.class),
		info = @NestInformation("Specifies libraries that should be linked with the application.\n"
				+ "The given libraries are added to the clang linking phase as the -l<LIB> argument."))
@NestParameterInformation(value = "Frameworks",
		type = @NestTypeUsage(value = Collection.class, elementTypes = String.class),
		info = @NestInformation("Specifies frameworks that the application uses.\n"
				+ "The given frameworks are added to the clang linking phase as the -framework <NAME> argument."))
@NestParameterInformation(value = "AddRPath",
		type = @NestTypeUsage(boolean.class),
		info = @NestInformation("Sets whether or not the -rpath parameter should be added for the clang linker.\n"
				+ "If set to true, the -rpath argument is passed to the backend linker with an approriate value for the"
				+ "specified platform.\n" + "The default is true."))
@NestParameterInformation(value = "Identifier",
		type = @NestTypeUsage(CompilationIdentifierTaskOption.class),
		info = @NestInformation("Compilation identifier that specifies for which compilations this preset can be applied to."))
@NestParameterInformation(value = "SDKs",
		type = @NestTypeUsage(value = Map.class,
				elementTypes = { saker.sdk.support.main.TaskDocs.DocSdkNameOption.class,
						SDKDescriptionTaskOption.class }),
		info = @NestInformation("Specifies the SDKs (Software Development Kits) that are part of the configuration.\n"
				+ "The SDKs will be passed to the clang task, and also available with the SDKs field of the result.\n"
				+ "Appropriate SDKs for the given Platform, clang, and developer macOS will be added by default."))
@NestParameterInformation(value = "DeviceFamily",
		type = @NestTypeUsage(value = Collection.class, elementTypes = DocDeviceFamilyOption.class),
		info = @NestInformation("Specifies the device families that the app is expected to run on.\n"
				+ "This parameter will cause an appropriate UIDeviceFamily value be inserted in the Info.plist "
				+ "file of your application.\n"
				+ "By default, the value of this is inferred based on the target Platform.\n"
				+ "You can set this parameter to null to disable the default values.\n"
				+ "Generally this parameter is used to add or set iPad as the device family "
				+ "when targetting the iPhoneOS platform."))
public class AppleOptionsPresetTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.apple.preset";

	public static final int UIDEVICEFAMILY_IPHONE = 1;
	public static final int UIDEVICEFAMILY_IPAD = 2;
	public static final int UIDEVICEFAMILY_TVOS = 3;
	public static final int UIDEVICEFAMILY_WATCHOS = 4;
	private static final PlistValueOption PLIST_VALUE_UIDEVICEFAMILY_IPHONE = PlistValueOption
			.create(ImmutableUtils.asUnmodifiableArrayList(PlistValueOption.create(UIDEVICEFAMILY_IPHONE)));
	private static final PlistValueOption PLIST_VALUE_UIDEVICEFAMILY_WATCHOS = PlistValueOption
			.create(ImmutableUtils.asUnmodifiableArrayList(PlistValueOption.create(UIDEVICEFAMILY_WATCHOS)));
	private static final PlistValueOption PLIST_VALUE_UIDEVICEFAMILY_TVOS = PlistValueOption
			.create(ImmutableUtils.asUnmodifiableArrayList(PlistValueOption.create(UIDEVICEFAMILY_TVOS)));

	public static final Set<String> KNOWN_ARCHITECTURES = ImmutableUtils.makeImmutableNavigableSet(
			new String[] { "x86_64", "armv7", "arm64", "arm64e", "i386", "armv7k", "armv7s", "arm64_32" });

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = { "", "Platform" })
			public String platformOption;
			@SakerInput(value = { "PlatformVersionMin" })
			public String platformVersionMinOption;

			@SakerInput(value = "Architecture")
			public Optional<String> architectureOption;

			@SakerInput(value = "Release")
			public Boolean releaseOption = Boolean.FALSE;

			@SakerInput(value = { "ARC", "AutomaticReferenceCounting" })
			public Boolean automaticReferenceCountingOption = Boolean.TRUE;

			@SakerInput(value = { "Libraries" })
			public Collection<String> librariesOption;

			@SakerInput(value = { "Frameworks" })
			public Collection<String> frameworksOption;

			@SakerInput(value = "AddRPath")
			public Boolean addRPathOption = Boolean.TRUE;

			@SakerInput(value = { "Identifier" })
			public CompilationIdentifierTaskOption identifierOption;

			@SakerInput(value = { "SDKs" })
			public Map<String, SDKDescriptionTaskOption> sdksOption;

			@SakerInput(value = { "DeviceFamily" })
			public Optional<Collection<String>> uiDeviceFamilyOption = null;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_CONFIGURATION);
				}

				//TODO support bitcode settings

				CompilationIdentifier identifier = CompilationIdentifierTaskOption.getIdentifier(identifierOption);

				List<Object> linkerparams = new ArrayList<>();
				List<Object> compilerparams = new ArrayList<>();
				List<Object> cppcompilerparams = new ArrayList<>();
				List<Object> objccppcompilerparams = new ArrayList<>();
				List<Object> ccompilerparams = new ArrayList<>();
				NavigableMap<String, String> macros = new TreeMap<>();
				NavigableMap<String, String> objccppmacros = new TreeMap<>();
				NavigableMap<String, SDKDescription> sdkmap = SDKSupportFrontendUtils.toSDKDescriptionMap(sdksOption);
				NavigableMap<String, PlistValueOption> infoplistvalues = new TreeMap<>();

				String platform = platformOption == null ? null : platformOption.toLowerCase(Locale.ENGLISH);
				if ("macos".equals(platform)) {
					platform = "macosx";
				}

				sdkmap.putIfAbsent("Clang", VersionsXcodeSDKDescription.create(null).getClangSDK());
				sdkmap.putIfAbsent(SakerAppleUtils.SDK_NAME_XCODE, SakerAppleUtils.getDefaultXcodeSDKDescription());
				sdkmap.putIfAbsent(SakerAppleUtils.SDK_NAME_DEVELOPER_MAC_OS,
						SakerAppleUtils.getDefaultDeveloperMacOsSDKDescription());

				final String architecture;
				if (architectureOption == null) {
					architecture = getDefaultArchitecture(platform);
				} else {
					String arch = architectureOption.orElse(null);
					if (arch != null) {
						arch = arch.toLowerCase(Locale.ENGLISH);
					}
					architecture = arch;
				}

				if (platform != null) {
					if (!DocApplePlatformOption.KNOWN_PLATFORMS.contains(platform)) {
						SakerLog.warning().taskScriptPosition(taskcontext)
								.println("Unrecognized platform name: " + platform + " expected one of: "
										+ StringUtils.toStringJoin(", ", DocApplePlatformOption.KNOWN_PLATFORMS));
					}
					if (platformVersionMinOption != null) {
						String param = getMinVersionClangArgument(platform, platformVersionMinOption);
						if (param != null) {
							linkerparams.add(param);
							compilerparams.add(param);
						}
					}
					String sdkname = null;
					SDKPathReference sysroot = null;
					switch (platform) {
						case "iphoneos": {
							sdkname = SakerAppleUtils.SDK_NAME_PLATFORM_IPHONEOS;
							sdkmap.putIfAbsent(sdkname, SakerAppleUtils.getDefaultIPhoneOsSDKDescription());
							sysroot = SDKPathReference.create(sdkname, SakerAppleUtils.SDK_APPLEPLATFORM_PATH_PATH);

							if (Boolean.TRUE.equals(addRPathOption)) {
								linkerparams.add("-Xlinker");
								linkerparams.add("-rpath");
								linkerparams.add("-Xlinker");
								linkerparams.add("@executable_path/Frameworks");
							}
							linkerparams.add("-dead_strip");
							infoplistvalues.put("CFBundleSupportedPlatforms", PlistValueOption
									.create(ImmutableUtils.singletonList(PlistValueOption.create("iPhoneOS"))));
							if (platformVersionMinOption != null) {
								infoplistvalues.put("MinimumOSVersion",
										PlistValueOption.create(platformVersionMinOption));
							}
							addUIDeviceFamilyValues(infoplistvalues, PLIST_VALUE_UIDEVICEFAMILY_IPHONE);
							break;
						}
						case "iphonesimulator": {
							sdkname = SakerAppleUtils.SDK_NAME_PLATFORM_IPHONESIMULATOR;
							sdkmap.putIfAbsent(sdkname, SakerAppleUtils.getDefaultIPhoneOsSDKDescription());
							sysroot = SDKPathReference.create(sdkname, SakerAppleUtils.SDK_APPLEPLATFORM_PATH_PATH);

							if (Boolean.TRUE.equals(addRPathOption)) {
								linkerparams.add("-Xlinker");
								linkerparams.add("-rpath");
								linkerparams.add("-Xlinker");
								linkerparams.add("@executable_path/Frameworks");
							}
							linkerparams.add("-dead_strip");
							infoplistvalues.put("CFBundleSupportedPlatforms", PlistValueOption
									.create(ImmutableUtils.singletonList(PlistValueOption.create("iPhoneSimulator"))));
							if (platformVersionMinOption != null) {
								infoplistvalues.put("MinimumOSVersion",
										PlistValueOption.create(platformVersionMinOption));
							}
							addUIDeviceFamilyValues(infoplistvalues, PLIST_VALUE_UIDEVICEFAMILY_IPHONE);
							break;
						}
						case "macos":
						case "macosx": {
							sdkname = SakerAppleUtils.SDK_NAME_PLATFORM_MACOS;
							sdkmap.putIfAbsent(sdkname, SakerAppleUtils.getDefaultMacOsSDKDescription());
							sysroot = SDKPathReference.create(sdkname, SakerAppleUtils.SDK_APPLEPLATFORM_PATH_PATH);

							//so the frameworks are searched under the Frameworks directory of the .app bundle
							//https://developer.apple.com/library/archive/documentation/CoreFoundation/Conceptual/CFBundles/BundleTypes/BundleTypes.html
							if (Boolean.TRUE.equals(addRPathOption)) {
								linkerparams.add("-Xlinker");
								linkerparams.add("-rpath");
								linkerparams.add("-Xlinker");
								linkerparams.add("@executable_path/../Frameworks");
							}
							infoplistvalues.put("CFBundleSupportedPlatforms", PlistValueOption
									.create(ImmutableUtils.singletonList(PlistValueOption.create("MacOSX"))));
							if (platformVersionMinOption != null) {
								infoplistvalues.put("LSMinimumSystemVersion",
										PlistValueOption.create(platformVersionMinOption));
							}
							break;
						}
						case "appletvos": {
							sdkname = SakerAppleUtils.SDK_NAME_PLATFORM_APPLETVOS;
							sdkmap.putIfAbsent(sdkname, SakerAppleUtils.getDefaultAppleTvOsSDKDescription());
							sysroot = SDKPathReference.create(sdkname, SakerAppleUtils.SDK_APPLEPLATFORM_PATH_PATH);

							if (Boolean.TRUE.equals(addRPathOption)) {
								linkerparams.add("-Xlinker");
								linkerparams.add("-rpath");
								linkerparams.add("-Xlinker");
								linkerparams.add("@executable_path/Frameworks");
							}
							linkerparams.add("-dead_strip");
							infoplistvalues.put("CFBundleSupportedPlatforms", PlistValueOption
									.create(ImmutableUtils.singletonList(PlistValueOption.create("AppleTVOS"))));
							if (platformVersionMinOption != null) {
								infoplistvalues.put("MinimumOSVersion",
										PlistValueOption.create(platformVersionMinOption));
							}
							addUIDeviceFamilyValues(infoplistvalues, PLIST_VALUE_UIDEVICEFAMILY_TVOS);
							break;
						}
						case "appletvsimulator": {
							sdkname = SakerAppleUtils.SDK_NAME_PLATFORM_APPLETVSIMULATOR;
							sdkmap.putIfAbsent(sdkname, SakerAppleUtils.getDefaultAppleTvSimulatorSDKDescription());
							sysroot = SDKPathReference.create(sdkname, SakerAppleUtils.SDK_APPLEPLATFORM_PATH_PATH);

							if (Boolean.TRUE.equals(addRPathOption)) {
								linkerparams.add("-Xlinker");
								linkerparams.add("-rpath");
								linkerparams.add("-Xlinker");
								linkerparams.add("@executable_path/Frameworks");
							}
							linkerparams.add("-dead_strip");
							infoplistvalues.put("CFBundleSupportedPlatforms", PlistValueOption
									.create(ImmutableUtils.singletonList(PlistValueOption.create("AppleTVSimulator"))));
							if (platformVersionMinOption != null) {
								infoplistvalues.put("MinimumOSVersion",
										PlistValueOption.create(platformVersionMinOption));
							}
							addUIDeviceFamilyValues(infoplistvalues, PLIST_VALUE_UIDEVICEFAMILY_TVOS);
							break;
						}
						case "watchos": {
							sdkname = SakerAppleUtils.SDK_NAME_PLATFORM_WATCHOS;
							sdkmap.putIfAbsent(sdkname, SakerAppleUtils.getDefaultWatchOsSDKDescription());
							sysroot = SDKPathReference.create(sdkname, SakerAppleUtils.SDK_APPLEPLATFORM_PATH_PATH);

							if (Boolean.TRUE.equals(addRPathOption)) {
								linkerparams.add("-Xlinker");
								linkerparams.add("-rpath");
								linkerparams.add("-Xlinker");
								linkerparams.add("@executable_path/Frameworks");
							}
							linkerparams.add("-dead_strip");
							infoplistvalues.put("CFBundleSupportedPlatforms", PlistValueOption
									.create(ImmutableUtils.singletonList(PlistValueOption.create("WatchOS"))));
							if (platformVersionMinOption != null) {
								infoplistvalues.put("MinimumOSVersion",
										PlistValueOption.create(platformVersionMinOption));
							}
							addUIDeviceFamilyValues(infoplistvalues, PLIST_VALUE_UIDEVICEFAMILY_WATCHOS);
							break;
						}
						case "watchsimulator": {
							sdkname = SakerAppleUtils.SDK_NAME_PLATFORM_WATCHSIMULATOR;
							sdkmap.putIfAbsent(sdkname, SakerAppleUtils.getDefaultWatchSimulatorSDKDescription());
							sysroot = SDKPathReference.create(sdkname, SakerAppleUtils.SDK_APPLEPLATFORM_PATH_PATH);

							if (Boolean.TRUE.equals(addRPathOption)) {
								linkerparams.add("-Xlinker");
								linkerparams.add("-rpath");
								linkerparams.add("-Xlinker");
								linkerparams.add("@executable_path/Frameworks");
							}
							linkerparams.add("-dead_strip");
							infoplistvalues.put("CFBundleSupportedPlatforms", PlistValueOption
									.create(ImmutableUtils.singletonList(PlistValueOption.create("WatchSimulator"))));
							if (platformVersionMinOption != null) {
								infoplistvalues.put("MinimumOSVersion",
										PlistValueOption.create(platformVersionMinOption));
							}
							addUIDeviceFamilyValues(infoplistvalues, PLIST_VALUE_UIDEVICEFAMILY_WATCHOS);
							break;
						}
						default: {
							break;
						}
					}
					if (sysroot != null) {
						linkerparams.add("-isysroot");
						linkerparams.add(sysroot);
						compilerparams.add("-isysroot");
						compilerparams.add(sysroot);
					}
					if (sdkname != null) {
						infoplistvalues.put("DTPlatformVersion", PlistValueOption.create(SDKPropertyReference
								.create(sdkname, SakerAppleUtils.SDK_APPLEPLATFORM_PROPERTY_SDK_VERSION)));
						infoplistvalues.put("DTPlatformName", PlistValueOption.create(SDKPropertyReference
								.create(sdkname, SakerAppleUtils.SDK_APPLEPLATFORM_PROPERTY_PLATFORM_NAME)));
						infoplistvalues.put("DTSDKBuild", PlistValueOption.create(SDKPropertyReference.create(sdkname,
								SakerAppleUtils.SDK_APPLEPLATFORM_PROPERTY_PRODUCT_BUILD_VERSION)));
						infoplistvalues.put("DTSDKName", PlistValueOption.create(
								SDKPropertyReference.create(sdkname, SakerAppleUtils.SDK_APPLEPLATFORM_PROPERTY_NAME)));
					}
				}
				infoplistvalues.put("DTCompiler", PlistValueOption.create("com.apple.compilers.llvm.clang.1_0"));
				infoplistvalues.put("DTPlatformBuild", PlistValueOption.create(SDKPropertyReference
						.create(SakerAppleUtils.SDK_NAME_XCODE, SakerAppleUtils.SDK_XCODE_PROPERTY_BUILD_VERSION)));
				infoplistvalues.put("DTXcode", PlistValueOption.create(SDKPropertyReference
						.create(SakerAppleUtils.SDK_NAME_XCODE, SakerAppleUtils.SDK_XCODE_PROPERTY_VERSION_DTXCODE)));
				infoplistvalues.put("DTXcodeBuild", PlistValueOption.create(SDKPropertyReference
						.create(SakerAppleUtils.SDK_NAME_XCODE, SakerAppleUtils.SDK_XCODE_PROPERTY_BUILD_VERSION)));
				infoplistvalues.put("BuildMachineOSBuild",
						PlistValueOption.create(SDKPropertyReference.create(SakerAppleUtils.SDK_NAME_DEVELOPER_MAC_OS,
								SakerAppleUtils.SDK_DEVELOPER_MAC_OS_PROPERTY_BUILD_VERSION)));

				if (architecture != null) {
					if (!KNOWN_ARCHITECTURES.contains(architecture)) {
						SakerLog.warning().taskScriptPosition(taskcontext)
								.println("Unrecognized architecture: " + architecture + " expected one of: "
										+ StringUtils.toStringJoin(", ", KNOWN_ARCHITECTURES));
					}
					linkerparams.add("-arch");
					linkerparams.add(architecture);
					compilerparams.add("-arch");
					compilerparams.add(architecture);
				}

				if (Boolean.TRUE.equals(automaticReferenceCountingOption)) {
					linkerparams.add("-fobjc-arc");
					objccppcompilerparams.add("-fobjc-arc");
				}
				linkerparams.add("-fobjc-link-runtime");

				linkerparams.add("-stdlib=libc++");
				cppcompilerparams.add("-stdlib=libc++");

				if (!ObjectUtils.isNullOrEmpty(librariesOption)) {
					for (String lib : librariesOption) {
						if (ObjectUtils.isNullOrEmpty(lib)) {
							continue;
						}
						linkerparams.add("-l" + lib);
					}
				}
				if (!ObjectUtils.isNullOrEmpty(frameworksOption)) {
					for (String fw : frameworksOption) {
						if (ObjectUtils.isNullOrEmpty(fw)) {
							continue;
						}
						linkerparams.add("-framework");
						linkerparams.add(fw);
					}
				}

				objccppmacros.put("OBJC_OLD_DISPATCH_PROTOTYPES", "0");
				if (releaseOption != null) {
					if (!releaseOption) {
						macros.put("DEBUG", "1");
						compilerparams.add("-O0");
					} else {
						objccppmacros.put("NS_BLOCK_ASSERTIONS", "1");
						compilerparams.add("-Os");
						compilerparams.add("-fvisibility=hidden");

					}
				}
				cppcompilerparams.add("-fvisibility-inlines-hidden");

				compilerparams.add("-fno-common");
				compilerparams.add("-fstrict-aliasing");

				ccompilerparams.add("-std=c11");
				cppcompilerparams.add("-std=c++11");

				NavigableMap<String, Object> cppmap = createPresetMap(identifier,
						ImmutableUtils.asUnmodifiableArrayList("C++", "ObjC++"));
				NavigableMap<String, Object> cmap = createPresetMap(identifier,
						ImmutableUtils.asUnmodifiableArrayList("C", "ObjC"));

				NavigableMap<String, Object> objccppmap = createPresetMap(identifier,
						ImmutableUtils.asUnmodifiableArrayList("ObjC", "ObjC++"));
				NavigableMap<String, Object> basepresetmap = createPresetMap(identifier, null);

				basepresetmap.put("SimpleCompilerParameters", ImmutableUtils.makeImmutableList(compilerparams));
				basepresetmap.put("SimpleLinkerParameters", ImmutableUtils.makeImmutableList(linkerparams));
				basepresetmap.put("MacroDefinitions", ImmutableUtils.makeImmutableNavigableMap(macros));
				basepresetmap.put("SDKs", ImmutableUtils.makeImmutableNavigableMap(sdkmap));

				objccppmap.put("MacroDefinitions", ImmutableUtils.makeImmutableNavigableMap(objccppmacros));

				cppmap.put("SimpleCompilerParameters", ImmutableUtils.makeImmutableList(cppcompilerparams));
				cmap.put("SimpleCompilerParameters", ImmutableUtils.makeImmutableList(ccompilerparams));
				objccppmap.put("SimpleCompilerParameters", ImmutableUtils.makeImmutableList(objccppcompilerparams));

				ClangPresetTaskOption clangoptions = ClangPresetTaskOption.create(ImmutableUtils
						.asUnmodifiableArrayList((Object) ImmutableUtils.makeImmutableNavigableMap(basepresetmap),
								ImmutableUtils.makeImmutableNavigableMap(objccppmap),
								ImmutableUtils.makeImmutableNavigableMap(cppmap),
								ImmutableUtils.makeImmutableNavigableMap(cmap)));
				Object result = new TaskOutputImpl(clangoptions, infoplistvalues, sdkmap);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}

			private void addUIDeviceFamilyValues(NavigableMap<String, PlistValueOption> infoplistvalues,
					PlistValueOption defaultuidevidefamily) {
				if (uiDeviceFamilyOption == null) {
					infoplistvalues.put("UIDeviceFamily", defaultuidevidefamily);
				} else {
					Collection<String> familyoptions = uiDeviceFamilyOption.orElse(null);
					if (familyoptions != null) {
						ArrayList<PlistValueOption> family = new ArrayList<>();
						for (String name : familyoptions) {
							family.add(PlistValueOption.create(toDeviceFamilyInteger(name)));
						}
						infoplistvalues.put("UIDeviceFamily", PlistValueOption.create(family));
					}
				}
			}

		};
	}

	private static NavigableMap<String, Object> createPresetMap(CompilationIdentifier identifier,
			List<String> languages) {
		NavigableMap<String, Object> basepresetmap = new TreeMap<>();
		if (identifier != null) {
			basepresetmap.put("Identifier", identifier);
		}
		if (languages != null) {
			basepresetmap.put("Language", languages);
		}
		return basepresetmap;
	}

	protected static String getDefaultArchitecture(String platform) {
		if (platform == null) {
			return null;
		}
		switch (platform) {
			case "iphoneos": {
				return "arm64";
			}
			case "appletvos": {
				return "arm64";
			}
			case "watchos": {
				return "armv7k";
			}
			case "macos":
			case "macosx":
			case "iphonesimulator":
			case "appletvsimulator":
			case "watchsimulator": {
				//TODO this default may needs to be modified when ARM based macbooks come out?
				return "x86_64";
			}
			default: {
				return null;
			}
		}
	}

	protected static String getMinVersionClangArgument(String platform, String minversion) {
		if (minversion == null || platform == null) {
			return null;
		}
		switch (platform) {
			case "iphoneos": {
				return "-miphoneos-version-min=" + minversion;
			}
			case "iphonesimulator": {
				return "-miphonesimulator-version-min=" + minversion;
			}
			case "macos":
			case "macosx": {
				return "-mmacosx-version-min=" + minversion;
			}
			case "appletvos": {
				return "-mappletvos-version-min=" + minversion;
			}
			case "appletvsimulator": {
				return "-mappletvsimulator-version-min=" + minversion;
			}
			case "watchos": {
				return "-mwatchos-version-min=" + minversion;
			}
			case "watchsimulator": {
				return "-mwatchsimulator-version-min=" + minversion;
			}
			default: {
				return null;
			}
		}
	}

	protected static int toDeviceFamilyInteger(String name) {
		Objects.requireNonNull(name, "device family name");
		switch (name.toLowerCase(Locale.ENGLISH)) {
			case "iphone": {
				return UIDEVICEFAMILY_IPHONE;
			}
			case "ipad": {
				return UIDEVICEFAMILY_IPAD;
			}
			case "tvos": {
				return UIDEVICEFAMILY_TVOS;
			}
			case "watchos": {
				return UIDEVICEFAMILY_WATCHOS;
			}
			default: {
				throw new IllegalArgumentException("Unrecognized device family: " + name);
			}
		}
	}

	private static final class TaskOutputImpl implements Externalizable {
		private static final long serialVersionUID = 1L;

		private ClangPresetTaskOption clangPresetTaskOption;
		private NavigableMap<String, PlistValueOption> infoPlistValues;
		private NavigableMap<String, SDKDescription> sdks;

		/**
		 * For {@link Externalizable}.
		 */
		public TaskOutputImpl() {
		}

		public TaskOutputImpl(ClangPresetTaskOption clangPresetTaskOption,
				NavigableMap<String, PlistValueOption> infoplistvalues, NavigableMap<String, SDKDescription> sdks) {
			this.clangPresetTaskOption = clangPresetTaskOption;
			this.infoPlistValues = infoplistvalues;
			this.sdks = sdks;
		}

		//for conversion compatibility
		public ClangPresetTaskOption toClangPresetTaskOption() {
			return clangPresetTaskOption;
		}

		public NavigableMap<String, PlistValueOption> getInfoPlistValues() {
			return infoPlistValues;
		}

		public NavigableMap<String, SDKDescription> getSDKs() {
			return sdks;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(clangPresetTaskOption);
			SerialUtils.writeExternalMap(out, infoPlistValues);
			SerialUtils.writeExternalMap(out, sdks);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			clangPresetTaskOption = SerialUtils.readExternalObject(in);
			infoPlistValues = SerialUtils.readExternalSortedImmutableNavigableMap(in);
			sdks = SerialUtils.readExternalSortedImmutableNavigableMap(in, SDKSupportUtils.getSDKNameComparator());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((clangPresetTaskOption == null) ? 0 : clangPresetTaskOption.hashCode());
			result = prime * result + ((infoPlistValues == null) ? 0 : infoPlistValues.hashCode());
			result = prime * result + ((sdks == null) ? 0 : sdks.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TaskOutputImpl other = (TaskOutputImpl) obj;
			if (clangPresetTaskOption == null) {
				if (other.clangPresetTaskOption != null)
					return false;
			} else if (!clangPresetTaskOption.equals(other.clangPresetTaskOption))
				return false;
			if (infoPlistValues == null) {
				if (other.infoPlistValues != null)
					return false;
			} else if (!infoPlistValues.equals(other.infoPlistValues))
				return false;
			if (sdks == null) {
				if (other.sdks != null)
					return false;
			} else if (!sdks.equals(other.sdks))
				return false;
			return true;
		}

	}
}
