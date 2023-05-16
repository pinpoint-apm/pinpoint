package com.navercorp.pinpoint.web.cluster;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;

import java.util.List;

/**
 * @author Taejin Koo
 */
public interface ClusterDataManager {

    void start();

    void stop();

    boolean registerWebCluster(String zNodeName, byte[] contents);

    List<ClusterId> getRegisteredAgentList(ClusterKey key);

}
