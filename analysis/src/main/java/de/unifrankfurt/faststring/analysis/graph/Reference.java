package de.unifrankfurt.faststring.analysis.graph;

import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import de.unifrankfurt.faststring.analysis.label.TypeLabel;


public class Reference implements Labelable {

	private int v;

	private InstructionNode definition = null;

	private List<InstructionNode> uses = Lists.newLinkedList();

	private TypeLabel label = null;

	public Reference(int ref) {
		Preconditions.checkArgument(ref > 0, "valueNumber must be greater than 0");
		this.v = ref;

	}

	public Reference(int receiver, TypeLabel label) {
		this(receiver);
		this.label = label;
	}

	public void setDefinition(InstructionNode instructionNode) {
		this.definition = instructionNode;
	}

	public InstructionNode getDefinition() {
		return definition;
	}
	
	@Override
	public void setLabel(TypeLabel label) {
		this.label = label;

	}

	@Override
	public TypeLabel getLabel() {
		return label;
	}

	public List<InstructionNode> getUses() {
		return uses;
	}

	public Integer valueNumber() {
		return v;
	}

	void setUses(List<InstructionNode> uses) {
		this.uses = ImmutableList.copyOf(uses);
	}

	void setUsesMutable(LinkedList<InstructionNode> uses) {
		this.uses = uses;
	}


	public List<Integer> getConnectedRefs() {
		List<Integer> refs = Lists.newLinkedList(definition.getConnectedRefs(v));
		
		Iterables.<Integer>addAll(refs, 
				Iterables.concat(Iterables.transform(uses, new Function<InstructionNode, List<Integer>>() {
						@Override
						public List<Integer> apply(InstructionNode input) {
							return input.getConnectedRefs(v);
						}
					}
				))
			);
		
		return refs;
	}
	
	@Override
	public String toString() {
		return "Reference [v=" + v + ", label=" + label + ", def=" + definition
				+ ", uses=" + uses + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + v;
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Reference other = (Reference) obj;
		if (v != other.v)
			return false;
		return true;
	}


	@Override
	public boolean isLabel(TypeLabel label) {
		return this.label == label;
	}


	@Override
	public boolean isSameLabel(Labelable other) {
		return this.isLabel(other.getLabel());
	}




}