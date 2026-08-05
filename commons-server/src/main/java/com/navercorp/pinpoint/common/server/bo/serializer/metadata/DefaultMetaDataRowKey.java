package com.navercorp.pinpoint.common.server.bo.serializer.metadata;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.Objects;

public class DefaultMetaDataRowKey implements MetaDataRowKey {
    private final ServiceUid serviceUid;
    private final String agentId;
    private final long agentStartTime;
    private final int id;

    public DefaultMetaDataRowKey(String agentId, long agentStartTime, int id) {
        this(ServiceUid.DEFAULT, agentId, agentStartTime, id);
    }

    public DefaultMetaDataRowKey(ServiceUid serviceUid, String agentId, long agentStartTime, int id) {
        this.serviceUid = Objects.requireNonNull(serviceUid, "serviceUid");
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.agentStartTime = agentStartTime;
        this.id = id;
    }

    @Override
    public ServiceUid getServiceUid() {
        return serviceUid;
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
