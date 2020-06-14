package saker.apple.main.clang;

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
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import saker.apple.api.SakerAppleUtils;
import saker.apple.impl.plist.PlistValueOption;
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
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.trace.BuildTrace;
import saker.clang.main.options.ClangPresetTaskOption;
import saker.compiler.utils.api.CompilationIdentifier;
import saker.compiler.utils.main.CompilationIdentifierTaskOption;
import saker.nest.utils.FrontendTaskFactory;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKPathReference;
import saker.sdk.support.api.SDKPropertyReference;
import saker.sdk.support.api.SDKSupportUtils;
import saker.sdk.support.main.SDKSupportFrontendUtils;
import saker.sdk.support.main.option.SDKDescriptionTaskOption;

public class AppleOptionsPresetTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.apple.preset";

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

			@SakerInput(value = "AddRPath")
			public Boolean addRPathOption = Boolean.TRUE;

			@SakerInput(value = { "Identifier" })
			public CompilationIdentifierTaskOption identifierOption;

			@SakerInput(value = { "SDKs" })
			public Map<String, SDKDescriptionTaskOption> sdksOption;

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
