package com.navercorp.pinpoint.web.service.component;

import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.service.AgentEventService;
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
        if (isActiveAgentByPing(agentId, range)) {
            return true;
        }
        return agentCompatibility.isActiveAgent(agentId, range);
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
            if (isActiveAgentByPing(agentId, range)) {
                return true;
            }
        } else {
            logger.trace("agentCompatibility.isActiveAgent");
            if (agentCompatibility.isActiveAgent(agentId, range)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isActiveAgent(Application agent, String version, List<Range> ranges) {
        for (Range range : ranges) {
            if (isActiveAgent(agent, version, range)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isActiveAgentByPing(String agentId, Range range) {
        return this.agentEventService.getAgentEvents(agentId, range)
                .stream()
                .anyMatch(e -> e.getEventTypeCode() == AgentEventType.AGENT_PING.getCode());
    }


}
