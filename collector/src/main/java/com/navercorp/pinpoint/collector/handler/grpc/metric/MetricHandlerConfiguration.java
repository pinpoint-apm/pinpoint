package com.navercorp.pinpoint.collector.handler.grpc.metric;


import com.navercorp.pinpoint.collector.config.CollectorProperties;
import com.navercorp.pinpoint.collector.handler.grpc.GrpcMetricHandler;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentStatBatchMapper;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentStatMapper;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentUriStatMapper;
import com.navercorp.pinpoint.collector.service.AgentStatService;
import com.navercorp.pinpoint.collector.service.AgentUriStatService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MetricHandlerConfiguration {
    private final Logger logger = LogManager.getLogger(MetricHandlerConfiguration.class);


    public MetricHandlerConfiguration() {
        logger.info("Install {}", MetricHandlerConfiguration.class.getSimpleName());
    }

    @Bean
    public GrpcMetricHandler agentUriMetricHandler(CollectorProperties collectorProperties,
                                                   GrpcAgentUriStatMapper agentUriStatMapper,
                                                   AgentUriStatService agentUriStatService) {
        if (collectorProperties.isUriStatEnable()) {
            logger.info("Install AgentUriMetricHandler");
            return new AgentUriMetricHandler(agentUriStatMapper, agentUriStatService);
        }
        logger.info("Disable AgentUriMetricHandler");
        return new DisableAgentUriGrpcMetricHandler();
    }


    @Bean
    public GrpcMetricHandler agentMetricBatchHandler(GrpcAgentStatBatchMapper agentStatBatchMapper,
                                                     AgentMetricHandler agentMetricHandler) {
        return new AgentMetricBatchHandler(agentStatBatchMapper, agentMetricHandler);
    }

    @Bean
    public AgentMetricHandler agentMetricHandler(GrpcAgentStatMapper agentStatMapper,
                                                List<AgentStatService> agentStatServiceList) {
        return new AgentMetricHandler(agentStatMapper, agentStatServiceList);
    }
}
