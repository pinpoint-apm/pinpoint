package com.navercorp.pinpoint.collector.applicationmap.service;

import com.navercorp.pinpoint.common.trace.ServiceType;
import jakarta.validation.constraints.NotBlank;

public interface StatisticsService {

    void updateCaller(
            long requestTime,
            @NotBlank String callerApplicationName,
            ServiceType callerServiceType,
            @NotBlank String callerAgentId,
            @NotBlank String calleeApplicationName,
            ServiceType calleeServiceType,
            String calleeHost,
            int elapsed, boolean isError
    );

    void updateCallee(
            long requestTime,
            @NotBlank String calleeApplicationName,
            ServiceType calleeServiceType,
            @NotBlank String callerApplicationName,
            ServiceType callerServiceType,
            String callerHost,
            int elapsed, boolean isError
    );

    void updateResponseTime(
            long requestTime,
            @NotBlank String applicationName,
            ServiceType serviceType,
            String agentId,
            int elapsed, boolean isError
    );

    void updateAgentState(
            long requestTime,
            @NotBlank String callerApplicationName,
            ServiceType callerServiceType,
            @NotBlank String callerAgentId
    );
}
