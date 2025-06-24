package com.navercorp.pinpoint.collector.handler.grpc.metric;

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.handler.grpc.GrpcAgentEventService;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentStatBatchMapper;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.trace.PAgentStatBatch;
import com.navercorp.pinpoint.io.request.ServerHeader;
import com.navercorp.pinpoint.io.request.ServerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AgentMetricBatchHandler implements SimpleHandler<PAgentStatBatch> {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final GrpcAgentStatBatchMapper agentStatBatchMapper;
    private final AgentStatGroupService agentStatGroupService;

    private final GrpcAgentEventService grpcAgentEventService;


    public AgentMetricBatchHandler(GrpcAgentStatBatchMapper agentStatBatchMapper,
                                   AgentStatGroupService agentStatGroupService,
                                   GrpcAgentEventService grpcAgentEventService) {
        this.agentStatBatchMapper = Objects.requireNonNull(agentStatBatchMapper, "agentStatBatchMapper");
        this.agentStatGroupService = Objects.requireNonNull(agentStatGroupService, "agentStatGroupService");
        this.grpcAgentEventService = Objects.requireNonNull(grpcAgentEventService, "grpcAgentEventService");
    }

    @Override
    public void handleSimple(ServerRequest<PAgentStatBatch> request) {
        if (logger.isInfoEnabled()) {
            logger.info("Handle PAgentStatBatch {}", request.getHeader());
        } else if (logger.isDebugEnabled()) {
            logger.debug("Handle PAgentStatBatch {} {}", request.getHeader(), MessageFormatUtils.debugLog(request.getData()));
        }
        final PAgentStatBatch agentStatBatch = request.getData();

        final ServerHeader header = request.getHeader();
        final AgentStatBo agentStatBo = this.agentStatBatchMapper.map(agentStatBatch, header);
        if (agentStatBo == null) {
            return;
        }

        this.agentStatGroupService.handleAgentStat(agentStatBo);

        this.grpcAgentEventService.handleAgentStatBatch(header, agentStatBatch);
    }
}
