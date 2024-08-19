package com.navercorp.pinpoint.web.service.component;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.List;

public interface ActiveAgentValidator {
    boolean isActiveAgent(String agentId, Range range);

    boolean isActiveAgent(Application agent, Range range);

    boolean isActiveAgent(Application agent, String version, Range range);

    boolean isActiveAgent(Application agent, String version, List<Range> ranges);

    boolean isActiveAgentByEvent(String agentId, Range range);
}
