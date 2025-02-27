package com.navercorp.pinpoint.collector.config;


import com.navercorp.pinpoint.common.server.util.IdGenerator;
import com.navercorp.pinpoint.common.server.util.RandomApplicationUidGenerator;
import com.navercorp.pinpoint.common.server.vo.ApplicationUid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableCaching
@Import({
        CollectorApplicationIdCacheConfig.class,
        CollectorV4CacheConfig.class,
})
public class CollectorPinpointIdConfiguration {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public CollectorPinpointIdConfiguration() {
        logger.info("Install {}", CollectorPinpointIdConfiguration.class.getSimpleName());
    }

    @Bean
    @ConditionalOnProperty(value = "pinpoint.collector.application.uid.enable", havingValue = "true")
    public IdGenerator<ApplicationUid> applicationIdGenerator() {
        return new RandomApplicationUidGenerator();
    }
}
