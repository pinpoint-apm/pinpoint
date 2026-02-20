package com.navercorp.pinpoint.web.service.component;

import com.navercorp.pinpoint.common.timeseries.time.Range;

public interface ActiveAgentValidator {
    boolean isActiveAgent(String agentId, Range range);

    boolean isActiveAgent(String agentId, int agentServiceType, Range range);

    boolean isActiveAgent(String agentId, int agentServiceType, String version, Range range);

    boolean isActiveAgentByEvent(String agentId, Range range);
}
