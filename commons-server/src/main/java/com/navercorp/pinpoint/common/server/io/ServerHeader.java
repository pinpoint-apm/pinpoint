package com.navercorp.pinpoint.common.server.io;

import com.navercorp.pinpoint.common.server.uid.ServiceUidSupplier;
import org.jspecify.annotations.NonNull;

public interface ServerHeader {

    @NonNull
    String getAgentId();

    @NonNull
    String getAgentName();

    // Application -----------------
    @NonNull
    String getApplicationName();

    // Service -----------------
    String getServiceName();

    ServiceUidSupplier getServiceUid();

    // ----------------------

    long getAgentStartTime();

    int getServiceType();

    boolean isGrpcBuiltInRetry();

}
