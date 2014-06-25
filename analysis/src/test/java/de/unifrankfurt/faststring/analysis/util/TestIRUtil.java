package de.unifrankfurt.faststring.analysis.util;

import static de.unifrankfurt.faststring.analysis.utils.TestUtilities.assertList;

import java.util.Collection;

import org.junit.Test;

import de.unifrankfurt.faststring.analysis.utils.BaseAnalysisTest;

public class TestIRUtil extends BaseAnalysisTest {

	
	@Test
	public void testPhiLoop() {
		assertList(getAllPointersFor("phiLoop", 7), 4, 5, 7, 10, 11);
		assertList(getAllPointersFor("phiLoop", 11), 5, 10, 11);
		assertList(getAllPointersFor("phiLoop", 5), 5);
		assertList(getAllPointersFor("phiLoop", 4), 4);
		
	}

	@Test
	public void testParamDef() {
		assertList(getAllPointersFor("paramDef", 2), 2);
		assertList(getAllPointersFor("paramDef", 6), 6);
		
	}
	
	@Test
	public void testphiLoopAndIf() {
		assertList(getAllPointersFor("phiLoopAndIf", 12), 4, 11, 12);
		assertList(getAllPointersFor("phiLoopAndIf", 7), 4, 5, 7);
		assertList(getAllPointersFor("phiLoopAndIf", 16), 5, 15, 16);
		
	}
	
	@Test
	public void testPhiSimple() throws Exception {
		assertList(getAllPointersFor("phiSimple", 4), 4);
		assertList(getAllPointersFor("phiSimple", 7), 4, 6, 7);
		
	}
	
	private Collection<Integer> getAllPointersFor(String method, int valueNumber) {
		return IRUtil.findAllPointersFor(getDefUse("MethodAnalyzerTestClass", method), valueNumber);
	}
}
