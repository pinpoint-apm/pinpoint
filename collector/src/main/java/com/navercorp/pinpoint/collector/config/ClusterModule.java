package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.server.cluster.zookeeper.config.ClusterConfigurationFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Woonduk Kang(emeroad)
 */
@Configuration
@Import({
        ClusterConfigurationFactory.class,
        ClusterConfiguration.class,
        ClusterFilterChainConfiguration.class,
        ClusterServiceConfiguration.class
})
public class ClusterModule {

    private final Logger logger = LogManager.getLogger(ClusterModule.class);

    public ClusterModule() {
        logger.info("Install {}", ClusterModule.class.getSimpleName());
    }
}
