package com.navercorp.pinpoint.collector.config;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableCaching
@Import({
        CollectorPinpointIdCacheConfig.class,
})
@ConditionalOnProperty(value = "pinpoint.collector.v4.enable", havingValue = "true")
public class CollectorV4Configuration {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public CollectorV4Configuration() {
        logger.info("Install {}", CollectorV4Configuration.class.getSimpleName());
    }
}
