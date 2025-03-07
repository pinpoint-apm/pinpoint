package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public interface ServerHeader {

    String getAgentId();

    String getAgentName();

    // -----------------

    String getApplicationName();

    ApplicationUid getApplicationUid();

    // -----------------

    String getServiceName();

    ServiceUid getServiceUid();

    // ----------------------

    long getAgentStartTime();

    long getSocketId();

    int getServiceType();

    boolean isGrpcBuiltInRetry();

}
