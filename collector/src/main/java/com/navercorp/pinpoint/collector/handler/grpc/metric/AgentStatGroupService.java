package com.navercorp.pinpoint.collector.handler.grpc.metric;

import com.navercorp.pinpoint.collector.service.AgentStatService;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

public class AgentStatGroupService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentStatService[] agentStatServiceList;

    public AgentStatGroupService(List<AgentStatService> agentStatServiceList) {
        Objects.requireNonNull(agentStatServiceList, "agentStatServiceList");
        this.agentStatServiceList = agentStatServiceList.toArray(new AgentStatService[0]);
        for (AgentStatService service : this.agentStatServiceList) {
            logger.info("{}:{}", AgentStatService.class.getSimpleName(), service.getClass().getSimpleName());
        }
    }


    public void handleAgentStat(AgentStatBo agentStatBo) {
        for (AgentStatService agentStatService : agentStatServiceList) {
            try {
                agentStatService.save(agentStatBo);
            } catch (Exception e) {
                logger.warn("Failed to handle service={}, AgentStatBo={}", agentStatService, agentStatBo, e);
            }
        }
    }

}
