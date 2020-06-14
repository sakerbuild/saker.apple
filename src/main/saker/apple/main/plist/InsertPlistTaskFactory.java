package saker.apple.main.plist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import saker.apple.impl.plist.InsertPlistWorkerTaskFactory;
import saker.apple.impl.plist.InsertPlistWorkerTaskIdentifier;
import saker.apple.impl.plist.PlistValueOption;
import saker.apple.main.TaskDocs;
import saker.apple.main.TaskDocs.DocInsertPlistWorkerTaskOutput;
import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.ReflectTypes;
import saker.build.thirdparty.saker.util.function.Functionals;
import saker.build.trace.BuildTrace;
import saker.build.util.data.DataConverterUtils;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKPathReference;
import saker.sdk.support.api.SDKPropertyReference;
import saker.sdk.support.main.SDKSupportFrontendUtils;
import saker.sdk.support.main.option.SDKDescriptionTaskOption;
import saker.std.api.file.location.FileLocation;
import saker.std.api.util.SakerStandardUtils;
import saker.std.main.file.option.FileLocationTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;

@NestTaskInformation(returnType = @NestTypeUsage(DocInsertPlistWorkerTaskOutput.class))
@NestInformation("Inserts the specified values into a property list (plist).\n"
		+ "The task can be used to insert the specified values into a property list.\n"
		+ "The task can also convert the plist into a different format. " + "(Like the "
		+ ConvertPlistTaskFactory.TASK_NAME + "() task)")

@NestParameterInformation(value = "Input",
		aliases = { "" },
		type = @NestTypeUsage(FileLocationTaskOption.class),
		info = @NestInformation("Specifies the input plist file.\n"
				+ "If no input is specified, an empty plist is used."))
@NestParameterInformation(value = "Format",
		type = @NestTypeUsage(PlistFormatTaskOption.class),
		info = @NestInformation("Specifies the output format of the plist.\n"
				+ "It is the same as the input format by default. (XML if no input is specified)"))
@NestParameterInformation(value = "Values",
		type = @NestTypeUsage(value = Map.class, elementTypes = { String.class, Object.class }),
		info = @NestInformation("Map of values that should be inserted into the plist.\n"
				+ "The parameter expects a map with string keys and various values as input. The values "
				+ "may be lists (array), maps (dictionaries), numbers, booleans and strings.\n"
				+ "The values may also be SDK paths and property references in which case they will "
				+ "be resolved against the specified SDKs."))
@NestParameterInformation(value = "Output",
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("A forward relative output path that specifies the output location of the output plist.\n"
				+ "It can be used to have a better output location than the automatically generated one."))
@NestParameterInformation(value = "SDKs",
		type = @NestTypeUsage(value = Map.class,
				elementTypes = { saker.sdk.support.main.TaskDocs.DocSdkNameOption.class,
						SDKDescriptionTaskOption.class }),
		info = @NestInformation(TaskDocs.SDKS))
public class InsertPlistTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.plist.insert";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {
			@SakerInput(value = { "", "Input" })
			public FileLocationTaskOption inputOption;

			@SakerInput(value = "Format")
			public PlistFormatTaskOption formatOption;

			@SakerInput(value = { "Values" })
			public Map<String, PlistValueTaskOption> valuesOption;

			@SakerInput(value = "Output")
			public SakerPath outputOption;

			@SakerInput(value = { "SDKs" })
			public Map<String, SDKDescriptionTaskOption> sdksOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}

				FileLocation inputfl = TaskOptionUtils.toFileLocation(inputOption, taskcontext);
				NavigableMap<String, PlistValueTaskOption> valuesopt = ObjectUtils.cloneTreeMap(valuesOption,
						Functionals.identityFunction(), PlistValueTaskOption::clone);
				NavigableMap<String, SDKDescription> sdks = SDKSupportFrontendUtils.toSDKDescriptionMap(sdksOption);

				SakerPath outputpath;
				if (outputOption != null) {
					TaskOptionUtils.requireForwardRelativePathWithFileName(outputOption, "Output");
					outputpath = SakerPath.valueOf(TASK_NAME).resolve(outputOption);
				} else {
					if (inputfl == null) {
						outputpath = SakerPath.valueOf(TASK_NAME + "/default.plist");
					} else {
						outputpath = SakerPath.valueOf(TASK_NAME)
								.resolve(SakerStandardUtils.getFileLocationFileName(inputfl));
					}
				}
				NavigableMap<String, PlistValueOption> values = new TreeMap<>();

				PlistValueOption[] val = { null };
				PlistValueTaskOption.Visitor plisttaskoptionvisitor = new PlistValueTaskOption.Visitor() {
					@Override
					public void visit(boolean value) {
						val[0] = PlistValueOption.create(value);
					}

					@Override
					public void visit(String value) {
						val[0] = PlistValueOption.create(value);
					}

					@Override
					public void visit(long value) {
						val[0] = PlistValueOption.create(value);
					}

					@Override
					public void visit(double value) {
						val[0] = PlistValueOption.create(value);
					}

					@Override
					public void visit(SDKPathReference value) {
						val[0] = PlistValueOption.create(value);
					}

					@Override
					public void visit(SDKPropertyReference value) {
						val[0] = PlistValueOption.create(value);
					}

					@Override
					public void visit(Collection<?> value) {
						@SuppressWarnings("unchecked")
						List<? extends PlistValueTaskOption> list = (List<? extends PlistValueTaskOption>) DataConverterUtils
								.convert(taskcontext, InsertPlistTaskFactory.class.getClassLoader(), value,
										ReflectTypes.makeParameterizedType(List.class, PlistValueTaskOption.class),
										Collections.emptyList());
						ArrayList<PlistValueOption> vals = new ArrayList<>();
						for (PlistValueTaskOption to : list) {
							to.accept(this);
							vals.add(val[0]);
						}
						val[0] = PlistValueOption.create(vals);
					}

					@Override
					public void visit(Map<?, ?> value) {
						@SuppressWarnings("unchecked")
						Map<String, ? extends PlistValueTaskOption> map = (Map<String, ? extends PlistValueTaskOption>) DataConverterUtils
								.convert(taskcontext, InsertPlistTaskFactory.class.getClassLoader(), value, ReflectTypes
										.makeParameterizedType(Map.class, String.class, PlistValueTaskOption.class),
										Collections.emptyList());
						TreeMap<String, PlistValueOption> valsmap = new TreeMap<>();
						for (Entry<String, ? extends PlistValueTaskOption> entry : map.entrySet()) {
							entry.getValue().accept(this);
							valsmap.put(entry.getKey(), val[0]);
						}
						val[0] = PlistValueOption.create(valsmap);
					}

					@Override
					public void visit(PlistValueOption value) {
						val[0] = value;
					}
				};
				for (Entry<String, PlistValueTaskOption> entry : valuesopt.entrySet()) {
					String key = entry.getKey();
					PlistValueTaskOption valtaskopt = entry.getValue();
					//keep nulls as they signal removal
					val[0] = null;
					if (valtaskopt != null) {
						valtaskopt.accept(plisttaskoptionvisitor);
					}
					values.put(key, val[0]);
				}

				InsertPlistWorkerTaskIdentifier workertaskid = new InsertPlistWorkerTaskIdentifier(outputpath);
				InsertPlistWorkerTaskFactory workertask = new InsertPlistWorkerTaskFactory(inputfl,
						formatOption == null ? null : formatOption.getFormat(), values);
				workertask.setSdkDescriptions(sdks);
				taskcontext.startTask(workertaskid, workertask, null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

}
