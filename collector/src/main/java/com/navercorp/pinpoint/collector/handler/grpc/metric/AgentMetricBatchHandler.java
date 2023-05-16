package com.navercorp.pinpoint.collector.handler.grpc.metric;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.handler.grpc.GrpcMetricHandler;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentStatBatchMapper;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PAgentStatBatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AgentMetricBatchHandler implements GrpcMetricHandler {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final GrpcAgentStatBatchMapper agentStatBatchMapper;

    private final AgentMetricHandler agentMetricHandler;


    public AgentMetricBatchHandler(GrpcAgentStatBatchMapper agentStatBatchMapper,
                                   AgentMetricHandler agentMetricHandler) {
        this.agentStatBatchMapper = Objects.requireNonNull(agentStatBatchMapper, "agentStatBatchMapper");
        this.agentMetricHandler = Objects.requireNonNull(agentMetricHandler, "agentStatHandler");
    }

    @Override
    public boolean accept(GeneratedMessageV3 message) {
        return message instanceof PAgentStatBatch;
    }

    @Override
    public void handle(GeneratedMessageV3 message) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handle PAgentStatBatch={}", MessageFormatUtils.debugLog(message));
        }
        final PAgentStatBatch agentStatBatch = (PAgentStatBatch) message;

        final Header header = ServerContext.getAgentInfo();
        final AgentStatBo agentStatBo = this.agentStatBatchMapper.map(agentStatBatch, header);
        if (agentStatBo == null) {
            return;
        }

        this.agentMetricHandler.handleAgentStat(agentStatBo);
    }
}
