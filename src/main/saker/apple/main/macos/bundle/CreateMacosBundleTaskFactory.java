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

import saker.apple.impl.macos.bundle.CrateMacosBundleWorkerTaskIdentifier;
import saker.apple.impl.macos.bundle.CreateMacosBundleWorkerTaskFactory;
import saker.apple.impl.plist.lib.Plist;
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
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.api.util.SakerStandardUtils;
import saker.std.main.dir.prepare.RelativeContentsTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;

public class CreateMacosBundleTaskFactory extends FrontendTaskFactory<Object> {
	private static final SakerPath PATH_CONTENTS_PKGINFO = SakerPath.valueOf("Contents/PkgInfo");
	private static final SakerPath PATH_CONTENTS_INFOPLIST = SakerPath.valueOf("Contents/Info.plist");
	private static final SakerPath PATH_CONTENTS = SakerPath.valueOf("Contents");
	private static final SakerPath PATH_CONTENTS_RESOURCES = SakerPath.valueOf("Contents/Resources");
	private static final SakerPath PATH_CONTENTS_MACOS = SakerPath.valueOf("Contents/MacOS");
	private static final SakerPath PATH_CONTENTS_FRAMEWORKS = SakerPath.valueOf("Contents/Frameworks");
	private static final SakerPath PATH_CONTENTS_PLUGINS = SakerPath.valueOf("Contents/PlugIns");
	private static final SakerPath PATH_CONTENTS_SHAREDSUPPORT = SakerPath.valueOf("Contents/SharedSupport");

	private static final Object DEP_TAG_INFOPLIST = PATH_CONTENTS_INFOPLIST;

	private static final long serialVersionUID = 1L;

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
							plist.accept(new FileLocationVisitor() {
								@Override
								public void visit(LocalFileLocation loc) {
									SakerPath path = loc.getLocalPath();
									ContentDescriptor cd = taskcontext.getTaskUtilities().getReportExecutionDependency(
											SakerStandardUtils.createLocalFileContentDescriptorExecutionProperty(path,
													UUID.randomUUID()));
									if (cd == null || cd instanceof DirectoryContentDescriptor) {
										throw ObjectUtils.sneakyThrow(
												new NoSuchFileException("Specified Info.plist is not a file: " + path));
									}
									try (InputStream is = LocalFileProvider.getInstance().openInputStream(path)) {
										inputmappings.put(PATH_CONTENTS_PKGINFO,
												getPkgInfoFileLocationBasedOnPlist(taskcontext, is));
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
										throw ObjectUtils.sneakyThrow(
												new NoSuchFileException("Specified Info.plist is not a file: " + path));
									}
									taskcontext.reportInputFileDependency(DEP_TAG_INFOPLIST, path,
											f.getContentDescriptor());
									try (InputStream is = f.openInputStream()) {
										inputmappings.put(PATH_CONTENTS_PKGINFO,
												getPkgInfoFileLocationBasedOnPlist(taskcontext, is));
									} catch (Exception e) {
										throw ObjectUtils.sneakyThrow(e);
									}
								}
							});
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

				CrateMacosBundleWorkerTaskIdentifier workertaskid = new CrateMacosBundleWorkerTaskIdentifier(
						outputpath);
				CreateMacosBundleWorkerTaskFactory workertask = new CreateMacosBundleWorkerTaskFactory(inputmappings);
				taskcontext.startTask(workertaskid, workertask, null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

	protected static FileLocation getPkgInfoFileLocationBasedOnPlist(TaskContext taskcontext, InputStream in)
			throws IOException {
		NestBundleClassLoader cl = (NestBundleClassLoader) CreateMacosBundleTaskFactory.class.getClassLoader();
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
