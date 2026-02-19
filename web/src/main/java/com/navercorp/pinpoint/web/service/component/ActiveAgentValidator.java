package com.navercorp.pinpoint.web.service.component;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.vo.Application;

public interface ActiveAgentValidator {
    boolean isActiveAgent(String agentId, Range range);

    boolean isActiveAgent(Application agent, Range range);

    boolean isActiveAgent(Application agent, String version, Range range);

    boolean isActiveAgentByEvent(String agentId, Range range);
}
