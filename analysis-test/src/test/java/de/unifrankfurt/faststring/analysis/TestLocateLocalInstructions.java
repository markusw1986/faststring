package de.unifrankfurt.faststring.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;

import org.junit.Test;

import de.unifrankfurt.faststring.analysis.graph.InstructionNode;
import de.unifrankfurt.faststring.analysis.graph.Reference;
import de.unifrankfurt.faststring.analysis.label.TypeLabel;
import de.unifrankfurt.faststring.analysis.test.util.TestUtilities;
import de.unifrankfurt.faststring.utils.BaseTransformerTest;

public class TestLocateLocalInstructions extends BaseTransformerTest {

	@Test
	public void testLoad() throws Exception {
		AnalysisResult result = analyzeToResult("testLoad");

		Reference ref = findRef(result, 6);

		InstructionNode substring = ref.getDefinition();
		InstructionNode startsWith = ref.getUses().get(0);

		assertNotNull(substring.getLoad(0));
		assertNotNull(startsWith.getLoad(0));

		assertEquals(3, (int)substring.getLoad(0));
		assertEquals(2, (int)startsWith.getLoad(0));

	}

	@Test
	public void testStore() throws Exception {
		AnalysisResult result = analyzeToResult("testStore");

		Reference ref = findRef(result, 6);

		InstructionNode substring = ref.getDefinition();

		assertTrue(substring.getStore() != -1);

		assertEquals(3, (int)substring.getStore());

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

	@Override
	protected Collection<TypeLabel> getTypeLabel() {
		return Arrays.asList(TestUtilities.loadTestLabel("SubstringString"));
	}
}
