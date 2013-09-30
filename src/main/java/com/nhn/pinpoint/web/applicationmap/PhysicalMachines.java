package com.nhn.pinpoint.web.applicationmap;

import java.util.SortedMap;
import java.util.TreeMap;

import com.nhn.pinpoint.web.util.JsonSerializable;
import com.nhn.pinpoint.web.util.Mergeable;

/**
 * 
 * @author netspider
 * 
 */
public class PhysicalMachines implements Mergeable<PhysicalMachines>, JsonSerializable {
	private final String machineName;
	private final SortedMap<String, ServerInstance> instances;

	public PhysicalMachines(String machineName) {
		this.machineName = machineName;
		this.instances = new TreeMap<String, ServerInstance>();
	}

	public void addServerInstance(ServerInstance instance) {

	}

	@Override
	public String getId() {
		return machineName;
	}

	@Override
	public String getJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"name\":\"").append(machineName).append("\"");
		sb.append("}");
		return sb.toString();
	}

	@Override
	public PhysicalMachines mergeWith(PhysicalMachines o) {
		if (!this.machineName.equals(o.machineName)) {
			throw new IllegalArgumentException();
		}
		return null;
	}

}
