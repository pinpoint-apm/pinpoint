package com.navercorp.pinpoint.collector.handler.grpc.metric;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.handler.grpc.GrpcMetricHandler;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentStatMapper;
import com.navercorp.pinpoint.collector.service.AgentStatService;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AgentMetricHandler implements GrpcMetricHandler {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final GrpcAgentStatMapper agentStatMapper;

    private final AgentStatService[] agentStatServiceList;

    public AgentMetricHandler(GrpcAgentStatMapper agentStatMapper,
                              AgentStatService[] agentStatServiceList) {
        this.agentStatMapper = Objects.requireNonNull(agentStatMapper, "agentStatMapper");
        this.agentStatServiceList = Objects.requireNonNull(agentStatServiceList, "agentStatServiceList");

        for (AgentStatService service : this.agentStatServiceList) {
            logger.info("{}:{}", AgentStatService.class.getSimpleName(), service.getClass().getSimpleName());
        }
    }

    @Override
    public boolean accept(GeneratedMessageV3 message) {
        return message instanceof PAgentStat;
    }

    @Override
    public void handle(GeneratedMessageV3 message) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handle PAgentStat={}", MessageFormatUtils.debugLog(message));
        }
        final PAgentStat agentStat = (PAgentStat) message;

        final AgentStatBo agentStatBo = this.agentStatMapper.map(agentStat);
        if (agentStatBo == null) {
            return;
        }

        handleAgentStat(agentStatBo);
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
