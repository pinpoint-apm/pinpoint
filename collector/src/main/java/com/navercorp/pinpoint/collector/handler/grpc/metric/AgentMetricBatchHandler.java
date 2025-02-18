package com.navercorp.pinpoint.collector.handler.grpc.metric;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.handler.grpc.GrpcMetricHandler;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentStatBatchMapper;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.trace.PAgentStatBatch;
import com.navercorp.pinpoint.io.request.ServerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

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
    public boolean accept(ServerRequest<GeneratedMessageV3> request) {
        GeneratedMessageV3 message = request.getData();
        return message instanceof PAgentStatBatch;
    }

    @Override
    public void handle(ServerRequest<GeneratedMessageV3> request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handle PAgentStatBatch={}", MessageFormatUtils.debugLog(request.getData()));
        }
        final PAgentStatBatch agentStatBatch = (PAgentStatBatch) request.getData();

        final Header header = request.getHeader();
        final AgentStatBo agentStatBo = this.agentStatBatchMapper.map(agentStatBatch, header);
        if (agentStatBo == null) {
            return;
        }

        this.agentMetricHandler.handleAgentStat(agentStatBo);
    }
}
