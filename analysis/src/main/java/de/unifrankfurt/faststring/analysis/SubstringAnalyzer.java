package de.unifrankfurt.faststring.analysis;

import static de.unifrankfurt.faststring.analysis.util.IRUtil.METHOD_SUBSTRING;
import static de.unifrankfurt.faststring.analysis.util.IRUtil.METHOD_SUBSTRING_DEFAULT_START;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;

import de.unifrankfurt.faststring.analysis.graph.DataFlowGraphBuilder;

public class SubstringAnalyzer {

	private static final Logger LOG = LoggerFactory.getLogger(SubstringAnalyzer.class);
	
	private TargetApplication targetApp;
	
	private IMethod method;

	private IR ir;
	private DefUse defUse;

	
	private StringCallIdentifier identifier = new StringCallIdentifier(METHOD_SUBSTRING, METHOD_SUBSTRING_DEFAULT_START);

	
	public SubstringAnalyzer(TargetApplication targetApplication, IMethod m) {
		method = m;
		targetApp = targetApplication;
		
		ir = targetApp.findIRForMethod(method);
		defUse = targetApp.findDefUseForMethod(method);	
	}
	
	
	public Set<Integer> findCandidates() {
		LOG.info("analyzing Method: {}", method.getSignature());
		
		
		new DataFlowGraphBuilder(identifier, ir, defUse).createDataFlowGraph();
		
//		if (!stringUses.isEmpty()) {
//			DataFlowGraph graph = DataFlowGraphBuilder.create(defUse, Queues.newArrayDeque(stringUses));
//			
//			checkDefinition(stringUses, graph);
//			
//			System.out.println("after checkDefinition: " + stringUses);
//			
//			checkUses(stringUses, graph);
//			
//			System.out.println("after checkUses: " + stringUses);
//			
//		} else {
//			LOG.debug("no string uses found");
//		}					
		
		return null;
		
	}
	





//	private UseRegister checkCandidates(Map<Integer, List<Integer>> stringCalls) {
//		
//		UseRegister register = new UseRegister();
//		IRAnalyzer analyzer = new IRAnalyzer(ir);
//		
//		for (Entry<Integer, List<Integer>> call : stringCalls.entrySet()) {
//			
//			Queue<Integer> vs = Queues.newArrayDeque(call.getValue());
//			
//			while (!vs.isEmpty()) {
//				Integer v = vs.remove();
//				Integer insIndex = call.getKey();
//				Iterator<SSAInstruction> uses = defUse.getUses(v);
//				while (uses.hasNext()) {
//					
//					SSAInstruction use = uses.next();
//					if (!(use instanceof SSAPhiInstruction)) {
//						boolean usedLater = analyzer.isConnected(insIndex, use);
//						
//						register.add(v, usedLater);
//					} else {
//						Boolean pointerShadow = register.getCandidate(use.getDef());
//						if (pointerShadow == null) {
//							vs.add(v);
//						} else {
//							register.add(v, pointerShadow);
//						}
//					}
//				}
//			}
//			
//		}
//		
//		return register;
//
//	}
	
}
