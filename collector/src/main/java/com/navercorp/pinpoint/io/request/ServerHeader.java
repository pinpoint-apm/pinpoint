package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.function.Supplier;

public interface ServerHeader {

    String getAgentId();

    String getAgentName();

    // -----------------

    String getApplicationName();

    Supplier<ApplicationUid> getApplicationUid();

    // -----------------

    String getServiceName();

    Supplier<ServiceUid> getServiceUid();

    // ----------------------

    long getAgentStartTime();

    long getSocketId();

    int getServiceType();

    boolean isGrpcBuiltInRetry();

}
