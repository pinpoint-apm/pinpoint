package com.navercorp.pinpoint.web.cluster;

import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.List;

/**
 * @author Taejin Koo
 */
public interface ClusterDataManager {

    void start() throws InterruptedException, IOException, KeeperException, Exception;

    void stop();

    boolean registerWebCluster(String zNodeName, byte[] contents);

    List<String> getRegisteredAgentList(AgentInfo agentInfo);

    List<String> getRegisteredAgentList(String applicationName, String agentId, long startTimeStamp);

}
