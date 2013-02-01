package com.nhn.hippo.web.servermap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 
 * @author netspider
 * 
 */
public class Nodes {

	private final Map<String, Node> nodes = new HashMap<String, Node>();
	private final Map<String, String> spanIdToNodeId = new HashMap<String, String>();

	public void addNode(String spanId, Node node) {
		if (!nodes.containsKey(node.getId())) {
			nodes.put(node.getId(), node);
		} else {
			nodes.get(node.getId()).mergeWith(node);
		}
		spanIdToNodeId.put(spanId, node.getId());
	}

	public Set<Entry<String, Node>> entrySet() {
		return nodes.entrySet();
	}

	public Node get(String key) {
		if (nodes.containsKey(key)) {
			return nodes.get(key);
		} else {
			return nodes.get(spanIdToNodeId.get(key));
		}
	}

	public Collection<Node> values() {
		return nodes.values();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("\t[");
		for (Entry<String, Node> entry : nodes.entrySet()) {
			sb.append(entry.getValue()).append(", ");
		}
		sb.append("]");

		return sb.toString();
	}
}
