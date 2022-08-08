package com.navercorp.pinpoint.web.cluster;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;

import java.util.Objects;

public final class ClusterKeyUtils {

    public static ClusterKey from(AgentInfo agentInfo) {
        Objects.requireNonNull(agentInfo, "agentInfo");

        return new ClusterKey(agentInfo.getApplicationName(), agentInfo.getAgentId(), agentInfo.getStartTimestamp());
    }

    public static ClusterKeyAndStatus withStatusFrom(AgentAndStatus agentInfoAndStatus) {
        Objects.requireNonNull(agentInfoAndStatus, "agentInfoAndStatus");
        ClusterKey clusterKey = from(agentInfoAndStatus.getAgentInfo());
        return new ClusterKeyAndStatus(clusterKey, agentInfoAndStatus.getStatus());
    }
}
