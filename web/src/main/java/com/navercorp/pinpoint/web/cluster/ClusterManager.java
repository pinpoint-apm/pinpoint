package com.nhn.pinpoint.web.cluster;

import java.util.List;

/**
 * @author koo.taejin <kr14910>
 */
public interface ClusterManager {

	boolean registerWebCluster(String zNodeName, byte[] contents);

	void close();
	
	List<String> getRegisteredAgentList(String applicationName, String agentId, long startTimeStamp);

}
