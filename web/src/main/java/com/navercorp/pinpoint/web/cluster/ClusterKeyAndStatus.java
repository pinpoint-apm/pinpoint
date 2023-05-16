package com.navercorp.pinpoint.web.cluster;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;

import java.util.Objects;

public class ClusterKeyAndStatus {
    private final ClusterKey clusterKey;
    private final AgentStatus status;

    public ClusterKeyAndStatus(ClusterKey clusterKey, AgentStatus status) {
        this.clusterKey = Objects.requireNonNull(clusterKey, "clusterKey");
        this.status = Objects.requireNonNull(status, "status");
    }

    public ClusterKey getClusterKey() {
        return clusterKey;
    }

    public AgentStatus getStatus() {
        return status;
    }
}
