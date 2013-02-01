package com.nhn.hippo.web.servermap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author netspider
 * 
 */
public class Links {
	private final Map<String, Link> links = new HashMap<String, Link>();

	public void add(Link link) {
		links.put(link.getId(), link);
	}

	public void add(Link link, int elapsed) {
		if (links.containsKey(link.getId())) {
			links.get(link.getId()).addElapsedTime(elapsed);
		} else {
			links.put(link.getId(), link);
		}
	}

	public Collection<Link> values() {
		return links.values();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Entry<String, Link> entry : links.entrySet()) {
			sb.append("\t").append(entry.getValue()).append("\n");
		}

		return sb.toString();
	}
}
