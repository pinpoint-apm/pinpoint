package com.navercorp.pinpoint.common.server.bo.serializer.metadata;


public interface MetaDataRowKey {

    String getAgentId();

    long getAgentStartTime();

    int getId();
}
