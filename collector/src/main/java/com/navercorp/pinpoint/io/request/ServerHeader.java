package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.vo.ServiceUid;

public interface ServerHeader {

    String getAgentId();

    String getAgentName();

    // -----------------

    String getApplicationName();

    long getApplicationUid();

    // -----------------

    String getServiceName();

    ServiceUid getServiceUid();

    // ----------------------

    long getAgentStartTime();

    long getSocketId();

    int getServiceType();

    boolean isGrpcBuiltInRetry();

}
