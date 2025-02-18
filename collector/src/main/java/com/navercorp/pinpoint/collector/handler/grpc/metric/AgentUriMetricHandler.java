package com.navercorp.pinpoint.collector.handler.grpc.metric;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.handler.grpc.GrpcMetricHandler;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentUriStatMapper;
import com.navercorp.pinpoint.collector.service.AgentUriStatService;
import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.trace.PAgentUriStat;
import com.navercorp.pinpoint.io.request.ServerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class AgentUriMetricHandler implements GrpcMetricHandler {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final GrpcAgentUriStatMapper agentUriStatMapper;
    private final AgentUriStatService agentUriStatService;

    public AgentUriMetricHandler(GrpcAgentUriStatMapper agentUriStatMapper,
                                 AgentUriStatService agentUriStatService) {
        this.agentUriStatMapper = Objects.requireNonNull(agentUriStatMapper, "agentUriStatMapper");
        this.agentUriStatService = Objects.requireNonNull(agentUriStatService, "agentUriStatService");
    }

    @Override
    public boolean accept(ServerRequest<GeneratedMessageV3> request) {
        GeneratedMessageV3 message = request.getData();
        return message instanceof PAgentUriStat;

    }

    @Override
    public void handle(ServerRequest<GeneratedMessageV3> request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handle PAgentUriStat={}", MessageFormatUtils.debugLog(request.getData()));
        }
        final Header header = request.getHeader();
        final PAgentUriStat agentUriStat = (PAgentUriStat) request.getData();
        final AgentUriStatBo agentUriStatBo = agentUriStatMapper.map(header, agentUriStat);
        agentUriStatService.save(agentUriStatBo);
    }

    @Override
    public String toString() {
        return "AgentUriStatHandler";
    }
}
