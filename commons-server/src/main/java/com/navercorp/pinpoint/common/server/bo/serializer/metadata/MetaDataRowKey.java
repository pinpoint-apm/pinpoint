package com.navercorp.pinpoint.common.server.bo.serializer.metadata;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public interface MetaDataRowKey {

    ServiceUid getServiceUid();

    String getAgentId();

    long getAgentStartTime();

    int getId();
}
