package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

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

    Supplier<ServiceUid> getServiceUid();

    // ----------------------

    long getAgentStartTime();

    long getSocketId();

    int getServiceType();

    boolean isGrpcBuiltInRetry();

}
