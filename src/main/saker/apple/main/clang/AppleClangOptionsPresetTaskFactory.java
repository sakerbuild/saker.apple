package saker.apple.main.clang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import saker.apple.api.SakerAppleUtils;
import saker.apple.impl.sdk.VersionsApplePlatformSDKDescription;
import saker.apple.impl.sdk.VersionsXcodeSDKDescription;
import saker.apple.main.sdk.PlatformSDKTaskFactory;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.trace.BuildTrace;
import saker.clang.main.options.ClangPresetTaskOption;
import saker.compiler.utils.api.CompilationIdentifier;
import saker.compiler.utils.main.CompilationIdentifierTaskOption;
import saker.nest.utils.FrontendTaskFactory;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKPathReference;
import saker.sdk.support.api.SDKSupportUtils;

public class AppleClangOptionsPresetTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAMe = "saker.apple.clang.preset";

	//TODO check these values, especially arm64_32
	public static final Set<String> KNOWN_ARCHITECTURES = ImmutableUtils.makeImmutableNavigableSet(
			new String[] { "x86_64", "armv7", "arm64", "i386", "armv7k", "armv7s", "arm64_32" });

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

			@SakerInput(value = { "Identifier" })
			public CompilationIdentifierTaskOption identifierOption;

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
				NavigableMap<String, SDKDescription> sdkmap = new TreeMap<>(SDKSupportUtils.getSDKNameComparator());

				String platform = platformOption == null ? null : platformOption.toLowerCase(Locale.ENGLISH);
				if ("macos".equals(platform)) {
					platform = "macosx";
				}

				sdkmap.putIfAbsent("Clang", VersionsXcodeSDKDescription.create(null).getClangSDK());

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
					if (!PlatformSDKTaskFactory.KNOWN_PLATFORMS.contains(platform)) {
						SakerLog.warning().taskScriptPosition(taskcontext)
								.println("Unrecognized platform name: " + platform + " expected one of: "
										+ StringUtils.toStringJoin(", ", PlatformSDKTaskFactory.KNOWN_PLATFORMS));
					}
					if (platformVersionMinOption != null) {
						String param = getMinVersionClangArgument(platform, platformVersionMinOption);
						if (param != null) {
							linkerparams.add(param);
							compilerparams.add(param);
						}
					}
					SDKPathReference sysroot = null;
					switch (platform) {
						case "iphoneos": {
							sdkmap.put(SakerAppleUtils.SDK_NAME_PLATFORM_IPHONEOS,
									VersionsApplePlatformSDKDescription.create(platform, null));
							sysroot = SDKPathReference.create(SakerAppleUtils.SDK_NAME_PLATFORM_IPHONEOS,
									SakerAppleUtils.SDK_APPLEPLATFORM_PATH_PATH);

							linkerparams.add("-Xlinker");
							linkerparams.add("-rpath");
							linkerparams.add("-Xlinker");
							linkerparams.add("@executable_path/Frameworks");
							linkerparams.add("-dead_strip");
							break;
						}
						case "iphonesimulator": {
							sdkmap.put(SakerAppleUtils.SDK_NAME_PLATFORM_IPHONESIMULATOR,
									VersionsApplePlatformSDKDescription.create(platform, null));
							sysroot = SDKPathReference.create(SakerAppleUtils.SDK_NAME_PLATFORM_IPHONESIMULATOR,
									SakerAppleUtils.SDK_APPLEPLATFORM_PATH_PATH);

							linkerparams.add("-Xlinker");
							linkerparams.add("-rpath");
							linkerparams.add("-Xlinker");
							linkerparams.add("@executable_path/Frameworks");
							linkerparams.add("-dead_strip");
							break;
						}
						case "macos":
						case "macosx": {
							sdkmap.put(SakerAppleUtils.SDK_NAME_PLATFORM_MACOS,
									VersionsApplePlatformSDKDescription.create(platform, null));
							sysroot = SDKPathReference.create(SakerAppleUtils.SDK_NAME_PLATFORM_MACOS,
									SakerAppleUtils.SDK_APPLEPLATFORM_PATH_PATH);

							//so the frameworks are searched under the Frameworks directory of the .app bundle
							//https://developer.apple.com/library/archive/documentation/CoreFoundation/Conceptual/CFBundles/BundleTypes/BundleTypes.html
							linkerparams.add("-Xlinker");
							linkerparams.add("-rpath");
							linkerparams.add("-Xlinker");
							linkerparams.add("@executable_path/../Frameworks");
							break;
						}
						case "appletvos": {
							sdkmap.put(SakerAppleUtils.SDK_NAME_PLATFORM_APPLETVOS,
									VersionsApplePlatformSDKDescription.create(platform, null));
							sysroot = SDKPathReference.create(SakerAppleUtils.SDK_NAME_PLATFORM_APPLETVOS,
									SakerAppleUtils.SDK_APPLEPLATFORM_PATH_PATH);

							linkerparams.add("-Xlinker");
							linkerparams.add("-rpath");
							linkerparams.add("-Xlinker");
							linkerparams.add("@executable_path/Frameworks");
							linkerparams.add("-dead_strip");
							break;
						}
						case "appletvsimulator": {
							sdkmap.put(SakerAppleUtils.SDK_NAME_PLATFORM_APPLETVSIMULATOR,
									VersionsApplePlatformSDKDescription.create(platform, null));
							sysroot = SDKPathReference.create(SakerAppleUtils.SDK_NAME_PLATFORM_APPLETVSIMULATOR,
									SakerAppleUtils.SDK_APPLEPLATFORM_PATH_PATH);

							linkerparams.add("-Xlinker");
							linkerparams.add("-rpath");
							linkerparams.add("-Xlinker");
							linkerparams.add("@executable_path/Frameworks");
							linkerparams.add("-dead_strip");
							break;
						}
						case "watchos": {
							sdkmap.put(SakerAppleUtils.SDK_NAME_PLATFORM_WATCHOS,
									VersionsApplePlatformSDKDescription.create(platform, null));
							sysroot = SDKPathReference.create(SakerAppleUtils.SDK_NAME_PLATFORM_WATCHOS,
									SakerAppleUtils.SDK_APPLEPLATFORM_PATH_PATH);

							linkerparams.add("-Xlinker");
							linkerparams.add("-rpath");
							linkerparams.add("-Xlinker");
							linkerparams.add("@executable_path/Frameworks");
							linkerparams.add("-dead_strip");
							break;
						}
						case "watchsimulator": {
							sdkmap.put(SakerAppleUtils.SDK_NAME_PLATFORM_WATCHSIMULATOR,
									VersionsApplePlatformSDKDescription.create(platform, null));
							sysroot = SDKPathReference.create(SakerAppleUtils.SDK_NAME_PLATFORM_WATCHSIMULATOR,
									SakerAppleUtils.SDK_APPLEPLATFORM_PATH_PATH);

							linkerparams.add("-Xlinker");
							linkerparams.add("-rpath");
							linkerparams.add("-Xlinker");
							linkerparams.add("@executable_path/Frameworks");
							linkerparams.add("-dead_strip");
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
				}

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

				ClangPresetTaskOption result = ClangPresetTaskOption.create(ImmutableUtils.asUnmodifiableArrayList(
						(Object) ImmutableUtils.makeImmutableNavigableMap(basepresetmap),
						ImmutableUtils.makeImmutableNavigableMap(objccppmap),
						ImmutableUtils.makeImmutableNavigableMap(cppmap),
						ImmutableUtils.makeImmutableNavigableMap(cmap)));
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
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

}
