package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.server.config.PinpointIdCacheConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        PinpointIdCacheConfiguration.class,
})
@ConditionalOnProperty(name = "pinpoint.collector.v4.enable", havingValue = "true")
public class CollectorPinpointIdCacheConfiguration {

    private final Logger logger = LogManager.getLogger(CollectorPinpointIdCacheConfiguration.class);

    public CollectorPinpointIdCacheConfiguration() {
        logger.info("Install {}", CollectorPinpointIdCacheConfiguration.class.getSimpleName());
    }
}
