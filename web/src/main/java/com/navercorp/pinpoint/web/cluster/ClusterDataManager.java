package com.navercorp.pinpoint.web.cluster;

import com.navercorp.pinpoint.web.vo.AgentInfo;

import java.util.List;

/**
 * @author Taejin Koo
 */
public interface ClusterDataManager {

    void start();

    void stop();

    boolean registerWebCluster(String zNodeName, byte[] contents);

    List<ClusterId> getRegisteredAgentList(AgentInfo agentInfo);

    List<ClusterId> getRegisteredAgentList(String applicationName, String agentId, long startTimeStamp);

}
