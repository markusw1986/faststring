package de.unifrankfurt.faststring.analysis;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;

import de.unifrankfurt.faststring.analysis.graph.DataFlowGraph;
import de.unifrankfurt.faststring.analysis.graph.DataFlowGraphBuilder;
import de.unifrankfurt.faststring.analysis.graph.GraphUtil;
import de.unifrankfurt.faststring.analysis.graph.StringReference;
import de.unifrankfurt.faststring.analysis.label.BuiltInTypes;
import de.unifrankfurt.faststring.analysis.label.StringTypeLabel;
import de.unifrankfurt.faststring.analysis.model.DataFlowObject;
import de.unifrankfurt.faststring.analysis.model.Definition;
import de.unifrankfurt.faststring.analysis.model.Use;
import de.unifrankfurt.faststring.analysis.util.StringUtil;
import de.unifrankfurt.faststring.analysis.util.UniqueQueue;

public class SubstringAnalyzer {

	private static final Logger LOG = LoggerFactory.getLogger(SubstringAnalyzer.class);
	
	private TargetApplication targetApp;
	
	private IMethod method;

	private IR ir;
	private DefUse defUse;

	private StringTypeLabel label = BuiltInTypes.SUBSTRING;

	private DataFlowGraph graph;

	
	public SubstringAnalyzer(TargetApplication targetApplication, IMethod m) {
		method = m;
		targetApp = targetApplication;
		
		ir = targetApp.findIRForMethod(method);
		defUse = targetApp.findDefUseForMethod(method);	
		
	}
	
	
	public Set<StringReference> findCandidates() {
		LOG.info("analyzing (for {}) Method: {}", label, method.getSignature());
		
		graph = getGraph();
		Collection<StringReference> refs = graph.getAllLabelMatchingReferences();
		
		if (refs.size() > 0) {
			processUntilQueueIsEmpty(refs, definitionCheck);
			processUntilQueueIsEmpty(graph.getAllLabelMatchingReferences(), usageCheck);
			
			
		} else {
			LOG.debug("no string uses found");
		}
		
		Collection<StringReference> finalRefs = graph.getAllLabelMatchingReferences();
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("found possible references: {}", GraphUtil.extractIntsFromStringReferences(finalRefs));
			LOG.debug("definition conversion needed for: {}", GraphUtil.extractDefConversions(finalRefs));
			LOG.debug("usage conversion needed for: {}", StringUtil.toStringMap(GraphUtil.extractUsageConversions(finalRefs)));
		}
		
		return Sets.newHashSet(finalRefs);
		
	}


	private DataFlowGraph getGraph() {
		if (graph == null) {
			graph = new DataFlowGraphBuilder(label).createDataFlowGraph(defUse, ir);
		}
		return graph;
	}


	private void processUntilQueueIsEmpty(Collection<StringReference> contents, CheckingStrategy strategy) {
		Queue<StringReference> refQueue = new UniqueQueue<>(contents);
		
		while(!refQueue.isEmpty()) {
			strategy.checkReference(refQueue.remove(), refQueue);
		}
		
	}

	interface CheckingStrategy {
		void checkReference(StringReference ref, Queue<StringReference> refQueue);
	}
	
	CheckingStrategy definitionCheck = new CheckingStrategy() {
		@Override
		public void checkReference(StringReference ref, Queue<StringReference> refQueue) {
			
			
			Definition def = ref.getDef();
			if (def.isCompatibleWith(label)) {
				check(def, refQueue);
				
			} else {
				ref.setConvertToDefinition();
			}
			
		}
	};
	
	CheckingStrategy usageCheck = new CheckingStrategy() {
		@Override
		public void checkReference(StringReference ref, Queue<StringReference> refQueue) {
			List<Use> uses = ref.getUses();
			for (int useId = 0; useId < uses.size(); useId++) {
				Use use = uses.get(useId);
				if (use.isCompatibleWith(label)) {
					check(use, refQueue);
				} else {
					ref.setConvertToUse(useId);
					
				}
			}
			
		}
	};
	
	

	private void check(DataFlowObject o, Queue<StringReference> refQueue) {		
		System.out.println("inspecting " + o);
		for (Integer connectedRefId : o.getConnectedRefs()) {
			StringReference connectedRef = graph.get(connectedRefId);
			// TODO maybe somewhere else...
			if (connectedRef.getLabel() == null) {
				System.out.println("setting label to " + connectedRef);
				connectedRef.setLabel(label);
				
				refQueue.add(connectedRef);
			}
		}
		
	}
	
}
