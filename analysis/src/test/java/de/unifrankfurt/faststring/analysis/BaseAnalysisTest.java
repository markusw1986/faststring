package de.unifrankfurt.faststring.analysis;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Map;

import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap.Builder;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ssa.IR;

import de.unifrankfurt.faststring.analysis.util.TestUtilities;

/**
 * base test class for all analysis test cases. Takes care creating the {@link ClassHierarchy} and
 * provides methods for easily retrieving {@link IR}s and {@link IClass}es
 * 
 * @author markus
 *
 */
public abstract class BaseAnalysisTest {

	private static final Logger LOG = LoggerFactory.getLogger(BaseAnalysisTest.class);
	
	private static TargetApplication targetApplication;

	private static Map<String, IClass> testClassMap = null;
	
	@BeforeClass
	public static void loadClassHierachie() throws IOException, ClassHierarchyException {
		if (targetApplication == null) {
			targetApplication = TestUtilities.loadTestClasses();
		}
		
	}
	
	private static void initTestClasses() {
		checkNotNull(targetApplication, "classHierachy is null. This might be "
				+ "caused by not calling loadClassHierachie before");
		
		LOG.info("creating test classes map");

		Builder<String, IClass> builder = new Builder<String, IClass>();
		
		for (IClass cl : targetApplication.getApplicationClasses()) {
				
			String className = cl.getName().getClassName().toString();
			
			LOG.info("test class {} found", className);
			builder.put(className, cl);
				
		}
		
		testClassMap = builder.build();
	}
	
	
	protected static IClass lookUpClass(String name) {
		checkNotNull(name, "name of class must not be null");
		
		if (testClassMap == null) {
			initTestClasses();
		}
		
		IClass cl = testClassMap.get(name);
		
		if (cl != null) {
			return cl;
		} else {
			throw new IllegalArgumentException(
					String.format("no class with name '%s' could be found in "
						+ "the test class hierachie", 
						name));
		}
		
	}
	
	protected static Map<String, IR> getIRsForClass(String name) {
		
		Builder<String, IR> builder = new Builder<String, IR>();
		
		for (IMethod m : lookUpClass(name).getDeclaredMethods()) {
			
			IR ir = targetApplication.findIRForMethod(m);
			builder.put(m.getName().toString(), ir);
		}

		return builder.build();
		
	}
	
	protected static IR getIR(String className, String methodName) {
		IR ir = getIRsForClass(className).get(methodName);
		
		checkNotNull(ir, "no ir found for class %s and method %s", className, methodName);
		
		return ir;
	}
	
	public static TargetApplication getTargetApplication() {
		return targetApplication;
	}
}