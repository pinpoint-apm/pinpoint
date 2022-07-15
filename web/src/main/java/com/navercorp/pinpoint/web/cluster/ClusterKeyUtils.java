package com.navercorp.pinpoint.web.cluster;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.web.vo.AgentInfo;

import java.util.Objects;

public final class ClusterKeyUtils {

    public static ClusterKey from(AgentInfo agentInfo) {
        Objects.requireNonNull(agentInfo, "agentInfo");

        return new ClusterKey(agentInfo.getApplicationName(), agentInfo.getAgentId(), agentInfo.getStartTimestamp());
    }

    public static ClusterKeyAndStatus withStatusFrom(AgentInfo agentInfo) {
        Objects.requireNonNull(agentInfo, "agentInfo");
        ClusterKey clusterKey = from(agentInfo);
        return new ClusterKeyAndStatus(clusterKey, agentInfo.getStatus());
    }
}
