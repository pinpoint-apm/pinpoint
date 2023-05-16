package com.navercorp.pinpoint.common.server.bo.serializer.metadata;

import java.util.Objects;

public class DefaultMetaDataRowKey implements MetaDataRowKey {
    private final String agentId;
    private final long agentStartTime;
    private final int id;

    public DefaultMetaDataRowKey(String agentId, long agentStartTime, int id) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.agentStartTime = agentStartTime;
        this.id = id;
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public long getAgentStartTime() {
        return agentStartTime;
    }

    @Override
    public int getId() {
        return id;
    }
}
