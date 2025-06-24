package com.navercorp.pinpoint.collector.handler.grpc.metric;


import com.navercorp.pinpoint.collector.config.CollectorProperties;
import com.navercorp.pinpoint.collector.handler.DisableSimpleHandler;
import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentUriStatMapper;
import com.navercorp.pinpoint.collector.service.AgentStatService;
import com.navercorp.pinpoint.collector.service.AgentUriStatService;
import com.navercorp.pinpoint.grpc.trace.PAgentUriStat;
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
    public SimpleHandler<PAgentUriStat> agentUriMetricHandler(CollectorProperties collectorProperties,
                                                              GrpcAgentUriStatMapper agentUriStatMapper,
                                                              AgentUriStatService agentUriStatService) {
        if (collectorProperties.isUriStatEnable()) {
            logger.info("Install AgentUriMetricHandler");
            return new AgentUriMetricHandler(agentUriStatMapper, agentUriStatService);
        }
        logger.info("Disable AgentUriMetricHandler");
        return new DisableSimpleHandler<>();
    }

    @Bean
    public AgentStatGroupService agentStatGroupService(List<AgentStatService> agentStatServiceList) {
        return new AgentStatGroupService(agentStatServiceList);
    }
}
