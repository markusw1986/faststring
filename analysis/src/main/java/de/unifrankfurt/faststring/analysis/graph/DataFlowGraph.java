package de.unifrankfurt.faststring.analysis.graph;

import java.util.Collection;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import de.unifrankfurt.faststring.analysis.label.TypeLabel;
import de.unifrankfurt.faststring.analysis.util.StringUtil;

public class DataFlowGraph {
	
	private final Map<Integer, Reference> nodes;
	private final TypeLabel label;
	private final Predicate<Reference> matchedLabel = new Predicate<Reference>() {
		@Override
		public boolean apply(Reference ref) {
			return label == ref.getLabel();
		}
	};
	
	public DataFlowGraph(TypeLabel label, Map<Integer, Reference> nodeMap) {
		this.nodes = nodeMap;
		this.label = label;
	}

	public boolean contains(Integer key) {
		return nodes.containsKey(key);
	}

	public Reference get(int ref) {
		return nodes.get(ref);
	}

	public int size() {
		return nodes.size();
	}
	

	public Collection<Reference> getAllLabelMatchingReferences() {
		return Maps.filterValues(nodes, matchedLabel).values();
	}

	
	@Override
	public String toString() {
		return StringUtil.toStringWithLineBreak(nodes.values());
	}



}