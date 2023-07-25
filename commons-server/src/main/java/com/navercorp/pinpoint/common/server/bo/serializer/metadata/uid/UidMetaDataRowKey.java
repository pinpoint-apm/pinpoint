package com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid;

public interface UidMetaDataRowKey {
    String getAgentId();

    long getAgentStartTime();

    byte[] getUid();
}
