package de.unifrankfurt.faststring;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import de.unifrankfurt.faststring.analysis.AnalysisResult;
import de.unifrankfurt.faststring.analysis.MethodAnalyzer;
import de.unifrankfurt.faststring.analysis.TargetApplication;
import de.unifrankfurt.faststring.analysis.label.TypeLabel;
import de.unifrankfurt.faststring.transform.JarManager;

public class Runner {
	private static final Logger LOG = LoggerFactory.getLogger(Runner.class);

	@Parameter(names = "-labels",
			description = "The optimizing labels to be used",
			required=true,
			listConverter=TypeLabelCreator.class)
	private List<TypeLabel> typeList;

	@Parameter(names = "-jar", required=true)
	private String jarFile;

	@Parameter(names = "-exclusion")
	private String exclusionFile;

	public static void main(String[] args) {
		Runner runner = new Runner();

		new JCommander(runner, args);

		runner.run();
	}


	private void run() {
		if (typeList.isEmpty()) {
			throw new IllegalArgumentException("no label was found");
		}

		TargetApplication targetApplication;
		try {
			targetApplication = new TargetApplication(jarFile, exclusionFile);

			Map<String, AnalysisResult> analysisResult = Maps.newHashMap();

			for (IClass clazz : targetApplication.getApplicationClasses()) {

				LOG.info("analyzing class: {}", clazz.getName());
				for (IMethod m : clazz.getDeclaredMethods()) {
					MethodAnalyzer analyzer = new MethodAnalyzer(targetApplication.findIRMethodForMethod(m), typeList);

					AnalysisResult candidates = analyzer.analyze();

					if (!candidates.isEmpty()) {
						analysisResult.put(m.getSignature(), candidates);
					}

				}

			}


			new JarManager(jarFile, analysisResult).process();

		} catch (ClassHierarchyException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}


	}


	public static class TypeLabelCreator implements IStringConverter<List<TypeLabel>> {

		@Override
		public List<TypeLabel> convert(String value) {
			Iterable<String> classNames = Splitter.on(";").split(value);

			List<TypeLabel> list = Lists.newLinkedList();

			for (String string : classNames) {

				try {
					Class<?> c = Class.forName(string);

					if (TypeLabel.class.isAssignableFrom(c)) {

						LOG.info("using typelabel: {}", c.getName());

						TypeLabel label = (TypeLabel) c.newInstance();

						list.add(label);

					} else {
						LOG.error("class is not implementing the TypeLabel interface: {}", c.getName());
					}


				} catch (ClassNotFoundException e) {
					LOG.error("could not load type label", string, e);
				} catch (InstantiationException | IllegalAccessException e) {
					LOG.error("could not initiate type label", string, e);
				}

			}

			return list;
		}

	}

}