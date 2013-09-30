package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.web.util.JsonSerializable;
import com.nhn.pinpoint.web.util.MergeableMap;
import com.nhn.pinpoint.web.util.MergeableTreeMap;

/**
 * 
 * @author netspider
 * 
 */
public class ServerInfo implements JsonSerializable {

	private MergeableMap<String, PhysicalMachines> infoMap = new MergeableTreeMap<String, PhysicalMachines>();

	public void add(PhysicalMachines machine) {
		infoMap.putOrMerge(machine.getId(), machine);
	}

	@Override
	public String getJson() {
		return null;
	}

	@Override
	public String toString() {
		return "ServerInfo [infoMap=" + infoMap + "]";
	}
}
