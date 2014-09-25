package com.nhn.pinpoint.web.applicationmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nhn.pinpoint.web.view.ServerInstanceListSerializer;

/**
 * @author emeroad
 * @author netspider
 */
@JsonSerialize(using = ServerInstanceListSerializer.class)
public class ServerInstanceList {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Map<String, List<ServerInstance>> serverInstanceList = new TreeMap<String, List<ServerInstance>>();

	public ServerInstanceList() {
	}

	public Map<String, List<ServerInstance>> getServerInstanceList() {
		// list의 소트가 안되 있는 문제가 있음.
		return serverInstanceList;
	}

	public int getInstanceCount() {
		int count = 0;
		for (List<ServerInstance> entry : serverInstanceList.values()) {
			count += entry.size();
		}
		return count;
	}

	private void addServerInstance(List<ServerInstance> nodeList, ServerInstance serverInstance) {
		for (ServerInstance node : nodeList) {
			boolean equalsNode = node.equals(serverInstance);
			if (equalsNode) {
				return;
			}
		}
		nodeList.add(serverInstance);
	}

	private List<ServerInstance> getServerInstanceList(String hostName) {
		List<ServerInstance> find = serverInstanceList.get(hostName);
		if (find == null) {
			find = new ArrayList<ServerInstance>();
			serverInstanceList.put(hostName, find);
		}
		return find;
	}

	void addServerInstance(ServerInstance serverInstance) {
		List<ServerInstance> find = getServerInstanceList(serverInstance.getHostName());
		addServerInstance(find, serverInstance);
	}
}
