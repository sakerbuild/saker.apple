package saker.apple.main.macos.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;

import saker.apple.impl.macos.bundle.CrateMacOsBundleWorkerTaskIdentifier;
import saker.apple.impl.macos.bundle.CreateMacOsBundleWorkerTaskFactory;
import saker.apple.impl.plist.lib.Plist;
import saker.apple.main.TaskDocs.DocCreateMacOsBundleWorkerTaskOutput;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.content.DirectoryContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.trace.BuildTrace;
import saker.nest.bundle.NestBundleClassLoader;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.api.util.SakerStandardUtils;
import saker.std.main.dir.prepare.RelativeContentsTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;

@NestTaskInformation(returnType = @NestTypeUsage(DocCreateMacOsBundleWorkerTaskOutput.class))
@NestInformation("Creates a macOS application bundle with the specified contents.\n"
		+ "The task can be used to create the .app application bundle for a macOS app. It will "
		+ "fill a directory with the contents of the application in the specified manner.\n"
		+ "Please refer to https://developer.apple.com/library/archive/documentation/CoreFoundation/Conceptual/CFBundles/BundleTypes/BundleTypes.html "
		+ "for the structure of a macOS application.")

@NestParameterInformation(value = "Contents",
		aliases = { "" },
		type = @NestTypeUsage(value = Collection.class, elementTypes = { RelativeContentsTaskOption.class }),
		info = @NestInformation("Specifies the files of the application for the Contents directory.\n"
				+ "All files specified in the parameter are placed in the Contents directory of the application bundle."))
@NestParameterInformation(value = "Resources",
		aliases = { "" },
		type = @NestTypeUsage(value = Collection.class, elementTypes = { RelativeContentsTaskOption.class }),
		info = @NestInformation("Specifies the files of the application for the Contents/Resources directory.\n"
				+ "All files specified in the parameter are placed in the Contents/Resources directory of the application bundle."))
@NestParameterInformation(value = "MacOS",
		aliases = { "" },
		type = @NestTypeUsage(value = Collection.class, elementTypes = { RelativeContentsTaskOption.class }),
		info = @NestInformation("Specifies the files of the application for the Contents/MacOS directory.\n"
				+ "All files specified in the parameter are placed in the Contents/MacOS directory of the application bundle.\n"
				+ "Typically, this directory contains only one binary file with your application�s main entry point and statically linked code. "
				+ "However, you may put other standalone executables (such as command-line tools) in this directory as well."))
@NestParameterInformation(value = "Frameworks",
		aliases = { "" },
		type = @NestTypeUsage(value = Collection.class, elementTypes = { RelativeContentsTaskOption.class }),
		info = @NestInformation("Specifies the files of the application for the Contents/Frameworks directory.\n"
				+ "All files specified in the parameter are placed in the Contents/Frameworks directory of the application bundle."))
@NestParameterInformation(value = "PlugIns",
		aliases = { "" },
		type = @NestTypeUsage(value = Collection.class, elementTypes = { RelativeContentsTaskOption.class }),
		info = @NestInformation("Specifies the files of the application for the Contents/PlugIns directory.\n"
				+ "All files specified in the parameter are placed in the Contents/PlugIns directory of the application bundle."))
@NestParameterInformation(value = "SharedSupport",
		aliases = { "" },
		type = @NestTypeUsage(value = Collection.class, elementTypes = { RelativeContentsTaskOption.class }),
		info = @NestInformation("Specifies the files of the application for the Contents/SharedSupport directory.\n"
				+ "All files specified in the parameter are placed in the Contents/SharedSupport directory of the application bundle."))
@NestParameterInformation(value = "GeneratePkgInfo",
		type = @NestTypeUsage(boolean.class),
		info = @NestInformation("Specifies whether or not the PkgInfo file should be automatically generated for the application.\n"
				+ "If set to true, the PkgInfo file will be generated with appropriate contents for the application. The contents "
				+ "are determined based on the Info.plist file entries.\n" + "The default is true.\n"
				+ "If the PkgInfo file is already specified as content or no Info.plist file is given, then "
				+ "it won't be generated."))
@NestParameterInformation(value = "Output",
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("A forward relative output path that specifies the output location of the application contents.\n"
				+ "It can be used to have a better output location than the automatically generated one."))
public class CreateMacOsBundleTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	private static final SakerPath PATH_CONTENTS_PKGINFO = SakerPath.valueOf("Contents/PkgInfo");
	private static final SakerPath PATH_CONTENTS_INFOPLIST = SakerPath.valueOf("Contents/Info.plist");
	private static final SakerPath PATH_CONTENTS = SakerPath.valueOf("Contents");
	private static final SakerPath PATH_CONTENTS_RESOURCES = SakerPath.valueOf("Contents/Resources");
	private static final SakerPath PATH_CONTENTS_MACOS = SakerPath.valueOf("Contents/MacOS");
	private static final SakerPath PATH_CONTENTS_FRAMEWORKS = SakerPath.valueOf("Contents/Frameworks");
	private static final SakerPath PATH_CONTENTS_PLUGINS = SakerPath.valueOf("Contents/PlugIns");
	private static final SakerPath PATH_CONTENTS_SHAREDSUPPORT = SakerPath.valueOf("Contents/SharedSupport");

	private static final Object DEP_TAG_INFOPLIST = "dep-tag-info-plist";

	public static final String TASK_NAME = "saker.macos.bundle.create";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = "Contents")
			public Collection<RelativeContentsTaskOption> contentsOption;
			@SakerInput(value = "Resources")
			public Collection<RelativeContentsTaskOption> resourcesOption;
			@SakerInput(value = "MacOS")
			public Collection<RelativeContentsTaskOption> macOSOption;
			@SakerInput(value = "Frameworks")
			public Collection<RelativeContentsTaskOption> frameworksOption;
			@SakerInput(value = "PlugIns")
			public Collection<RelativeContentsTaskOption> plugInsOption;
			@SakerInput(value = "SharedSupport")
			public Collection<RelativeContentsTaskOption> sharedSupportOption;

			@SakerInput(value = "GeneratePkgInfo")
			public boolean generatePkgInfoOption = true;

			@SakerInput(value = "Output")
			public SakerPath outputOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}

				NavigableMap<SakerPath, FileLocation> inputmappings = new TreeMap<>();
				contentsOption = ObjectUtils.cloneArrayList(contentsOption, RelativeContentsTaskOption::clone);
				resourcesOption = ObjectUtils.cloneArrayList(resourcesOption, RelativeContentsTaskOption::clone);
				macOSOption = ObjectUtils.cloneArrayList(macOSOption, RelativeContentsTaskOption::clone);
				frameworksOption = ObjectUtils.cloneArrayList(frameworksOption, RelativeContentsTaskOption::clone);
				plugInsOption = ObjectUtils.cloneArrayList(plugInsOption, RelativeContentsTaskOption::clone);
				sharedSupportOption = ObjectUtils.cloneArrayList(sharedSupportOption,
						RelativeContentsTaskOption::clone);
				addToInputMappings(contentsOption, taskcontext, inputmappings, PATH_CONTENTS);
				addToInputMappings(resourcesOption, taskcontext, inputmappings, PATH_CONTENTS_RESOURCES);
				addToInputMappings(macOSOption, taskcontext, inputmappings, PATH_CONTENTS_MACOS);
				addToInputMappings(frameworksOption, taskcontext, inputmappings, PATH_CONTENTS_FRAMEWORKS);
				addToInputMappings(plugInsOption, taskcontext, inputmappings, PATH_CONTENTS_PLUGINS);
				addToInputMappings(sharedSupportOption, taskcontext, inputmappings, PATH_CONTENTS_SHAREDSUPPORT);

				if (generatePkgInfoOption) {
					if (!inputmappings.containsKey(PATH_CONTENTS_PKGINFO)) {
						FileLocation plist = inputmappings.get(PATH_CONTENTS_INFOPLIST);
						if (plist != null) {
							inputmappings.put(PATH_CONTENTS_PKGINFO,
									getPkgInfoFileLocationBasedOnInfoPlist(taskcontext, plist));
						}
						//else don't auto generate the pkginfo as we don't have an info.plist
					}
				}

				SakerPath outputpath;
				if (outputOption != null) {
					TaskOptionUtils.requireForwardRelativePathWithFileName(outputOption, "Output");
					outputpath = SakerPath.valueOf(TASK_NAME).resolve(outputOption);
				} else {
					outputpath = SakerPath.valueOf(TASK_NAME).resolve("default.app");
				}

				CrateMacOsBundleWorkerTaskIdentifier workertaskid = new CrateMacOsBundleWorkerTaskIdentifier(
						outputpath);
				CreateMacOsBundleWorkerTaskFactory workertask = new CreateMacOsBundleWorkerTaskFactory(inputmappings);
				taskcontext.startTask(workertaskid, workertask, null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}

		};
	}

	public static FileLocation getPkgInfoFileLocationBasedOnInfoPlist(TaskContext taskcontext, FileLocation plist) {
		FileLocation[] result = { null };
		plist.accept(new FileLocationVisitor() {
			@Override
			public void visit(LocalFileLocation loc) {
				SakerPath path = loc.getLocalPath();
				ContentDescriptor cd = taskcontext.getTaskUtilities().getReportExecutionDependency(
						SakerStandardUtils.createLocalFileContentDescriptorExecutionProperty(path, UUID.randomUUID()));
				if (cd == null || cd instanceof DirectoryContentDescriptor) {
					throw ObjectUtils
							.sneakyThrow(new NoSuchFileException("Specified Info.plist is not a file: " + path));
				}
				try (InputStream is = LocalFileProvider.getInstance().openInputStream(path)) {
					result[0] = getPkgInfoFileLocationBasedOnPlist(taskcontext, is);
				} catch (Exception e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}

			@Override
			public void visit(ExecutionFileLocation loc) {
				SakerPath path = loc.getPath();
				SakerFile f = taskcontext.getTaskUtilities().resolveFileAtPath(path);
				if (f == null) {
					taskcontext.reportInputFileDependency(DEP_TAG_INFOPLIST, path,
							CommonTaskContentDescriptors.IS_NOT_FILE);
					throw ObjectUtils
							.sneakyThrow(new NoSuchFileException("Specified Info.plist is not a file: " + path));
				}
				taskcontext.reportInputFileDependency(DEP_TAG_INFOPLIST, path, f.getContentDescriptor());
				try (InputStream is = f.openInputStream()) {
					result[0] = getPkgInfoFileLocationBasedOnPlist(taskcontext, is);
				} catch (Exception e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}
		});
		return result[0];
	}

	protected static FileLocation getPkgInfoFileLocationBasedOnPlist(TaskContext taskcontext, InputStream in)
			throws IOException {
		NestBundleClassLoader cl = (NestBundleClassLoader) CreateMacOsBundleTaskFactory.class.getClassLoader();
		Path storagedir = cl.getBundle().getBundleStoragePath().resolve("gen_PkgInfo");
		String pkginfocontents;
		try (Plist plist = Plist.readFrom(in)) {
			//get the CFBundlePackageType and CFBundleSignature attributes from the plist and concatenate them
			String pkgtype = getPlistStringField(plist, "CFBundlePackageType");
			String sig = getPlistStringField(plist, "CFBundleSignature");
			pkginfocontents = pkgtype + sig;
		}
		byte[] pkginfobytes = pkginfocontents.getBytes(StandardCharsets.UTF_8);
		if (pkginfobytes.length != 4 + 4) {
			throw new IllegalArgumentException("Invalid PkgInfo contents. Expected 8 bytes: " + pkginfocontents);
		}
		String filename = Base64.getUrlEncoder().withoutPadding().encodeToString(pkginfobytes);
		Path cachedfile = storagedir.resolve(filename);
		UUID uuid = UUID.randomUUID();

		if (!Files.isRegularFile(cachedfile)) {
			//create parent dirs so we can write the contents
			Files.createDirectories(cachedfile.getParent());
			Path temppath = cachedfile.resolveSibling(filename + ".temp_" + uuid);
			try {
				Files.write(temppath, pkginfobytes);
				try {
					Files.move(temppath, cachedfile);
				} catch (IOException e) {
					//somebody already moved to it concurrently. ignore, continue
				}
			} finally {
				try {
					Files.deleteIfExists(temppath);
				} catch (IOException e) {
					taskcontext.getTaskUtilities().reportIgnoredException(e);
				}
			}
		}
		//report an output dependency so we get reinvoked if the cached PkgInfo gets deleted or something
		SakerPath cachefilesakerpath = SakerPath.valueOf(cachedfile);
		taskcontext.getTaskUtilities().getReportExecutionDependency(
				SakerStandardUtils.createLocalFileContentDescriptorExecutionProperty(cachefilesakerpath, uuid));
		return LocalFileLocation.create(cachefilesakerpath);
	}

	private static String getPlistStringField(Plist plist, String fname) {
		String pkgtype;
		try {
			pkgtype = (String) plist.get(fname);
		} catch (UnsupportedOperationException e) {
			throw new IllegalArgumentException("Info.plist " + fname + " field is not a String.");
		}
		if (pkgtype == null) {
			throw new IllegalArgumentException("Info.plist " + fname + " field is missing.");
		}
		return pkgtype;
	}

	protected static void addToInputMappings(Collection<RelativeContentsTaskOption> contentsOption,
			TaskContext taskcontext, NavigableMap<SakerPath, FileLocation> inputmappings, SakerPath basedir) {
		NavigableMap<SakerPath, FileLocation> contents = RelativeContentsTaskOption.toInputMap(taskcontext,
				contentsOption, null);
		if (contents == null) {
			return;
		}
		for (Entry<SakerPath, FileLocation> entry : contents.entrySet()) {
			FileLocation fl = entry.getValue();
			SakerPath relpath = basedir.resolve(entry.getKey());
			FileLocation prev = inputmappings.putIfAbsent(relpath, fl);
			if (prev != null && !prev.equals(fl)) {
				throw new IllegalArgumentException(
						"Duplicate files for application path: " + relpath + " with " + prev + " and " + fl);
			}
		}
	}

}
