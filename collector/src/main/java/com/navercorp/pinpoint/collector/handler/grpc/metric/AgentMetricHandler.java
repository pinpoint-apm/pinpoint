package com.navercorp.pinpoint.collector.handler.grpc.metric;

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.handler.grpc.GrpcAgentEventService;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentStatMapper;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.io.ServerHeader;
import com.navercorp.pinpoint.common.server.io.ServerRequest;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AgentMetricHandler implements SimpleHandler<PAgentStat> {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final GrpcAgentStatMapper agentStatMapper;
    private final AgentStatGroupService agentStatGroupService;

    private final GrpcAgentEventService grpcAgentEventService;

    public AgentMetricHandler(GrpcAgentStatMapper agentStatMapper,
                              AgentStatGroupService agentStatServiceList,
                              GrpcAgentEventService grpcAgentEventService) {
        this.agentStatMapper = Objects.requireNonNull(agentStatMapper, "agentStatMapper");
        this.agentStatGroupService = Objects.requireNonNull(agentStatServiceList, "agentStatServiceList");

        this.grpcAgentEventService = Objects.requireNonNull(grpcAgentEventService, "grpcAgentEventService");
    }

    @Override
    public void handleSimple(ServerRequest<PAgentStat> request) {
        if (logger.isInfoEnabled()) {
            logger.debug("Handle PAgentStat {}", request.getHeader());
        } else if (logger.isDebugEnabled()) {
            logger.debug("Handle PAgentStat {} {}", request.getHeader(), MessageFormatUtils.debugLog(request.getData()));
        }
        final PAgentStat agentStat = request.getData();
        final ServerHeader header = request.getHeader();
        final AgentStatBo agentStatBo = this.agentStatMapper.map(header, agentStat);
        if (agentStatBo == null) {
            return;
        }

        this.agentStatGroupService.handleAgentStat(agentStatBo);

        this.grpcAgentEventService.handleAgentStat(header, agentStat);
    }

}
