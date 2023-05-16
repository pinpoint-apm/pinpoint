package com.navercorp.pinpoint.collector.handler.grpc.metric;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.config.CollectorProperties;
import com.navercorp.pinpoint.collector.handler.grpc.GrpcMetricHandler;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentUriStatMapper;
import com.navercorp.pinpoint.collector.service.AgentUriStatService;
import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.trace.PAgentUriStat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AgentUriMetricHandler implements GrpcMetricHandler {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final GrpcAgentUriStatMapper agentUriStatMapper;
    private final AgentUriStatService agentUriStatService;

    private final boolean uriStatEnable;

    public AgentUriMetricHandler(CollectorProperties collectorProperties,
                                 GrpcAgentUriStatMapper agentUriStatMapper,
                                 AgentUriStatService agentUriStatService) {
        Objects.requireNonNull(collectorProperties, "collectorProperties");
        this.uriStatEnable = collectorProperties.isUriStatEnable();

        this.agentUriStatMapper = Objects.requireNonNull(agentUriStatMapper, "agentUriStatMapper");
        this.agentUriStatService = Objects.requireNonNull(agentUriStatService, "agentUriStatService");
    }

    @Override
    public boolean accept(GeneratedMessageV3 message) {
        return message instanceof PAgentUriStat;

    }

    @Override
    public void handle(GeneratedMessageV3 message) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handle PAgentUriStat={}", MessageFormatUtils.debugLog(message));
        }
        if (!uriStatEnable) {
            return;
        }
        final PAgentUriStat agentUriStat = (PAgentUriStat) message;
        final AgentUriStatBo agentUriStatBo = agentUriStatMapper.map(agentUriStat);
        agentUriStatService.save(agentUriStatBo);
    }

    @Override
    public String toString() {
        return "AgentUriStatHandler{" +
                "uriStatEnable=" + uriStatEnable +
                '}';
    }
}
