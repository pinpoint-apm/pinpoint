package com.navercorp.pinpoint.collector.handler.grpc.metric;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.handler.grpc.GrpcMetricHandler;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentStatMapper;
import com.navercorp.pinpoint.collector.service.AgentStatService;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.io.request.ServerRequest;
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
    public boolean accept(ServerRequest<GeneratedMessageV3> request) {
        GeneratedMessageV3 message = request.getData();
        return message instanceof PAgentStat;
    }

    @Override
    public void handle(ServerRequest<GeneratedMessageV3> request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handle PAgentStat={}", MessageFormatUtils.debugLog(request.getData()));
        }
        final PAgentStat agentStat = (PAgentStat) request.getData();
        final Header header = request.getHeader();
        final AgentStatBo agentStatBo = this.agentStatMapper.map(header, agentStat);
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
