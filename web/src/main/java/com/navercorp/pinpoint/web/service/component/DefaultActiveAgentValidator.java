package com.navercorp.pinpoint.web.service.component;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.service.AgentEventService;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class DefaultActiveAgentValidator implements ActiveAgentValidator {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentEventService agentEventService;
    private final LegacyAgentCompatibility agentCompatibility;

    public DefaultActiveAgentValidator(AgentEventService agentEventService, LegacyAgentCompatibility agentCompatibility) {
        this.agentEventService = Objects.requireNonNull(agentEventService, "agentEventService");
        this.agentCompatibility = Objects.requireNonNull(agentCompatibility, "agentCompatibility");
    }

    @Override
    public boolean isActiveAgent(String agentId, Range range) {
        Objects.requireNonNull(agentId, "agentId");

        if (isActiveAgentByEvent(agentId, range)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isActiveAgent(Application agent, Range range) {
        return isActiveAgent(agent, null, range);
    }

    @Override
    public boolean isActiveAgent(Application agent, String version, Range range) {
        Objects.requireNonNull(agent, "agent");
        String agentId = agent.getName();
        if (!agentCompatibility.isLegacyAgent(agent.getServiceTypeCode(), version)) {
            logger.trace("isActiveAgentByPing");

            if (isActiveAgentByEvent(agentId, range)) {
                return true;
            }
        }

        return false;
    }


    @Override
    public boolean isActiveAgentByEvent(String agentId, Range range) {
        AgentEventQuery query = AgentEventQuery.all().withOneRowScan();
        List<AgentEvent> recentAgentEvent = this.agentEventService.getAgentEvents(agentId, range, query);
        return CollectionUtils.hasLength(recentAgentEvent);
    }


}
