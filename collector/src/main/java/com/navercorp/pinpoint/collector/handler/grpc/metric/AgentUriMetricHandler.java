package com.navercorp.pinpoint.collector.handler.grpc.metric;

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentUriStatMapper;
import com.navercorp.pinpoint.collector.service.AgentUriStatService;
import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;
import com.navercorp.pinpoint.common.server.io.ServerHeader;
import com.navercorp.pinpoint.common.server.io.ServerRequest;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.trace.PAgentUriStat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class AgentUriMetricHandler implements SimpleHandler<PAgentUriStat> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final GrpcAgentUriStatMapper agentUriStatMapper;
    private final AgentUriStatService agentUriStatService;

    public AgentUriMetricHandler(GrpcAgentUriStatMapper agentUriStatMapper,
                                 AgentUriStatService agentUriStatService) {
        this.agentUriStatMapper = Objects.requireNonNull(agentUriStatMapper, "agentUriStatMapper");
        this.agentUriStatService = Objects.requireNonNull(agentUriStatService, "agentUriStatService");
    }

    @Override
    public void handleSimple(ServerRequest<PAgentUriStat> request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handle PAgentUriStat={}", MessageFormatUtils.debugLog(request.getData()));
        }
        final ServerHeader header = request.getHeader();
        final PAgentUriStat agentUriStat = request.getData();
        final AgentUriStatBo agentUriStatBo = agentUriStatMapper.map(header, agentUriStat);
        agentUriStatService.save(agentUriStatBo);
    }

    @Override
    public String toString() {
        return "AgentUriStatHandler";
    }
}
