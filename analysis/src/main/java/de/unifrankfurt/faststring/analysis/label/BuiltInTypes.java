package de.unifrankfurt.faststring.analysis.label;

import static de.unifrankfurt.faststring.analysis.util.IRUtil.METHOD_STRING_VALUE_OF;
import static de.unifrankfurt.faststring.analysis.util.IRUtil.METHOD_SUBSTRING;
import static de.unifrankfurt.faststring.analysis.util.IRUtil.METHOD_SUBSTRING_DEFAULT_START;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.MethodReference;

import de.unifrankfurt.faststring.analysis.graph.StringReference;

public enum BuiltInTypes implements StringTypeLabel {

	
	
	SUBSTRING(METHOD_SUBSTRING, METHOD_SUBSTRING_DEFAULT_START) {
		
		@Override
		public boolean canBeUsedAsParamFor(MethodReference method, int index) {
			// TODO check if its possible for stringbuilder
			return false;
		}

		@Override
		public boolean canBeUsedAsReceiverFor(MethodReference calledMethod) {
			return compatibleMethods().contains(calledMethod);
		}
		
		private Set<MethodReference> compatibleMethods() {
			Set<MethodReference> set = Sets.newHashSet(methods());
			// TODO maybe wrong only if it is substituted with a toString() call
			set.add(METHOD_STRING_VALUE_OF);
			return set;
		}

		@Override
		public boolean canBeDefinedAsResultOf(MethodReference method) {
			return methods().contains(method);
		}
	};
	
	private List<MethodReference> methods;

	private BuiltInTypes(MethodReference...ms) {
		methods = Arrays.asList(ms);
	}
	
	/* (non-Javadoc)
	 * @see de.unifrankfurt.faststring.analysis.label.StringTypeLabel#methods()
	 */
	@Override
	public List<MethodReference> methods() {
		return methods;
	}
	@Override
	public int check(SSAInstruction ins) {
		
		if (ins != null) {
			if (ins instanceof SSAAbstractInvokeInstruction) {
				SSAAbstractInvokeInstruction invokeIns = (SSAAbstractInvokeInstruction) ins;
				MethodReference target = invokeIns.getDeclaredTarget();
				if (methods().contains(target) && 
						!invokeIns.isStatic() ) {
					return invokeIns.getReceiver();
				}
			}
		
		}
		return -1;
	}
	
	public List<StringReference> findStringUses(IR ir) {
		List<StringReference> stringReference = Lists.newLinkedList();
		
		for (int i = 0; i < ir.getInstructions().length; i++) {
			
			SSAInstruction ins = ir.getInstructions()[i];
			
			int receiver = check(ins);
			
			if (receiver > -1) {
				stringReference.add(new StringReference(receiver, this));
				
			}
							
		}
		return stringReference;
	}
}