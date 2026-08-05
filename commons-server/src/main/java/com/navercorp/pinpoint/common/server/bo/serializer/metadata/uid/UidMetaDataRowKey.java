package com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public interface UidMetaDataRowKey {
    int UID_LENGTH = 16;

    ServiceUid getServiceUid();

    String getAgentId();

    long getAgentStartTime();

    byte[] getUid();
}
