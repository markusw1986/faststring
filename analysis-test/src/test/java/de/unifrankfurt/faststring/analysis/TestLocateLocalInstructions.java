package de.unifrankfurt.faststring.analysis;

import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import org.junit.Test;

import de.unifrankfurt.faststring.analysis.graph.InstructionNode;
import de.unifrankfurt.faststring.analysis.graph.Reference;
import de.unifrankfurt.faststring.utils.BaseTransformerTest;

public class TestLocateLocalInstructions extends BaseTransformerTest {

	@Test
	public void testLoad() throws Exception {
		AnalysisResult result = analyzeToResult("testLoad");

		Reference ref = findRef(result, 6);

		InstructionNode substring = ref.getDefinition();
		InstructionNode startsWith = ref.getUses().get(0);

		assertNotNull(substring.getLoad(1));
		assertNotNull(startsWith.getLoad(1));

		assertEquals(3, (int)substring.getLoad(1));
		assertEquals(2, (int)startsWith.getLoad(1));

	}

	@Test
	public void testStore() throws Exception {
		AnalysisResult result = analyzeToResult("testStore");

		Reference ref = findRef(result, 6);

		InstructionNode substring = ref.getDefinition();

		assertNotNull(substring.getStore(1));

		assertEquals(3, (int)substring.getStore(1));

	}

	
	private Reference findRef(AnalysisResult result, int i) {
		for (Reference ref : result.getRefs()) {
			if (ref.valueNumber() == i) {
				return ref;
			}
		}
		throw new NoSuchElementException("could not find " + i + " in " + result);
	}

	@Override
	public String getTestClass() {
		return "LoadTestClass";
	}
}