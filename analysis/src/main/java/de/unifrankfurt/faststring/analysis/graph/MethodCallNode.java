package de.unifrankfurt.faststring.analysis.graph;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.types.MethodReference;

import de.unifrankfurt.faststring.analysis.label.TypeLabel;
import de.unifrankfurt.faststring.analysis.util.IRUtil;


public class MethodCallNode extends LabelableNode {

	private MethodReference target;
	private boolean isStatic;

	private Map<Integer, TypeLabel> labels = Maps.newHashMap();
	private TypeLabel defLabel;

	public MethodCallNode(SSAInvokeInstruction instruction) {
		this(
			(instruction.hasDef()) ? instruction.getDef() : -1,
			IRUtil.getUses(instruction),
			instruction.getDeclaredTarget(),
			instruction.isStatic()
		);
	}

	public MethodCallNode(int def, List<Integer> uses,
			MethodReference target, boolean isStatic) {
		super();
		setDef(def);
		setUses(uses);
		this.target = target;
		this.isStatic = isStatic;
	}

	public MethodReference getTarget() {
		return target;
	}

	public void addLabelToUse(Integer valueNumber, TypeLabel label) {
		labels.put(valueNumber, label);
	}

	@Override
	public boolean canUseAt(TypeLabel label, int i) {
		if (i == 0 && !isStatic) {
			return label.canBeUsedAsReceiverFor(target);
		} else {
			return label.canBeUsedAsParamFor(target, i);
		}

	}

	@Override
	public boolean canProduce(TypeLabel label) {
		return label.canBeDefinedAsResultOf(target);
	}

	@Override
	public List<Integer> getLabelableRefs(TypeLabel label) {
		List<Integer> refs = Lists.newLinkedList();

		if (label.canBeDefinedAsResultOf(target)) {
			refs.add(def);
		}

		if (!isStatic && label.canBeUsedAsReceiverFor(target)) {
			refs.add(uses.get(0));
		}

		for (int i = (isStatic) ? 0 : 1; i < uses.size(); i++) {
			if (label.canBeUsedAsParamFor(target, i)) {
				refs.add(uses.get(i));
			}
		}

		return refs;
	}



	private MethodCallType determineMethodCallType(int inV) {
		if (inV == def) {
			return new Defintion();
		} else {

			int index = IRUtil.findUseIndex(inV, uses);

			if (index == 0 && !isStatic) {
				return new Receiver();
			} else {
				return new Parameter(index);
			}

		}
	}

	interface MethodCallType {
		boolean isCompatibleWithActual(TypeLabel label);

	}

	private class Defintion implements MethodCallType {
		@Override
		public boolean isCompatibleWithActual(TypeLabel label) {
			return label.canBeDefinedAsResultOf(target);
		}

	}

	private class Receiver implements MethodCallType {
		@Override
		public boolean isCompatibleWithActual(TypeLabel label) {
			return label.canBeUsedAsReceiverFor(target);
		}

	}

	private class Parameter implements MethodCallType {

		private int index;

		public Parameter(int index) {
			this.index = index;
		}

		@Override
		public boolean isCompatibleWithActual(TypeLabel label) {
			return label.canBeUsedAsParamFor(target, index);
		}

	}

	@Override
	public String toString() {
		return "MethodCallNode [def=" + def + ", uses=" + uses
				+ ", target=" + target + ", isStatic=" + isStatic + ", bcIndex=" +
				getByteCodeIndex() + " localVarIndex=" + localMap + ", loadMap=" +
				loadMap + ", store=" + storeIndex + " ]";
	}

	@Override
	public void visit(Visitor visitor) {
		visitor.visitMethodCall(this);
	}

	public boolean isReceiver(int v) {
		return determineMethodCallType(v) instanceof Receiver;
	}

	public Collection<Integer> getDefLocals() {
		return getLocals(def);
	}

	public List<Integer> getParams() {
		return uses.subList((isStatic) ? 0 : 1, uses.size());
	}

	public TypeLabel getLabelForUse(int v) {
		return labels.get(v);
	}

	public TypeLabel getDefLabel() {
		return defLabel;
	}

	public void setDefLabel(TypeLabel label) {
		defLabel = label;

	}

	public int getUse(int i) {
		return uses.get(i);
	}

	public boolean isStatic() {
		return isStatic;
	}
}
