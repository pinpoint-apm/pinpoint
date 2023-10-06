package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.collector.manage.ClusterManager;
import com.navercorp.pinpoint.realtime.collector.receiver.ClusterPointLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Woonduk Kang(emeroad)
 */
@Configuration
public class ClusterConfiguration {

    private final Logger logger = LogManager.getLogger(ClusterConfiguration.class);

    public ClusterConfiguration() {
        logger.info("Install {}", ClusterConfiguration.class.getSimpleName());
    }

    @Bean
    public ClusterManager clusterManager(ClusterPointLocator clusterPointLocator) {
        return new ClusterManager(clusterPointLocator);
    }
    
}
