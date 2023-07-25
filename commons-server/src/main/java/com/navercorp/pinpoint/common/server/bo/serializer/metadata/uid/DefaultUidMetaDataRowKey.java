package com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid;

import java.util.Objects;

public class DefaultUidMetaDataRowKey implements UidMetaDataRowKey {
    private final String agentId;
    private final long agentStartTime;
    private final byte[] uid;

    public DefaultUidMetaDataRowKey(String agentId, long agentStartTime, byte[] uid) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.agentStartTime = agentStartTime;
        this.uid = Objects.requireNonNull(uid, "uid");
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
    public byte[] getUid() {
        return uid;
    }
}
