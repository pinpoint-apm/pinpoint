package com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.Objects;

public class DefaultUidMetaDataRowKey implements UidMetaDataRowKey {
    private final ServiceUid serviceUid;
    private final String agentId;
    private final long agentStartTime;
    private final byte[] uid;

    public DefaultUidMetaDataRowKey(String agentId, long agentStartTime, byte[] uid) {
        this(ServiceUid.DEFAULT, agentId, agentStartTime, uid);
    }

    public DefaultUidMetaDataRowKey(ServiceUid serviceUid, String agentId, long agentStartTime, byte[] uid) {
        this.serviceUid = Objects.requireNonNull(serviceUid, "serviceUid");
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.agentStartTime = agentStartTime;
        this.uid = Objects.requireNonNull(uid, "uid");
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
    public byte[] getUid() {
        return uid;
    }
}
