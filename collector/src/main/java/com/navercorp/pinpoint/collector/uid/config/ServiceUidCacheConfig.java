package com.navercorp.pinpoint.collector.uid.config;


import com.navercorp.pinpoint.common.server.uid.cache.UidCaffeineCacheBuilder;
import com.navercorp.pinpoint.common.server.uid.cache.UidCaffeineCacheProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@ConditionalOnProperty(value = "pinpoint.collector.v4.enable", havingValue = "true")
public class ServiceUidCacheConfig {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public ServiceUidCacheConfig() {
        logger.info("Install {}", ServiceUidCacheConfig.class.getSimpleName());
    }

    public static final String SERVICE_UID_CACHE_NAME = "collectorServiceUidCache";

    @Bean
    @ConfigurationProperties(prefix = "pinpoint.service.uid.cache")
    public UidCaffeineCacheProperties serviceUidCacheProperties() {
        return new UidCaffeineCacheProperties();
    }

    @Bean
    public CacheManager collectorServiceUidCache(@Qualifier("serviceUidCacheProperties") UidCaffeineCacheProperties uidCaffeineCacheProperties) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(SERVICE_UID_CACHE_NAME);
        caffeineCacheManager.setCaffeine(new UidCaffeineCacheBuilder()
                .build(uidCaffeineCacheProperties)
        );

        return caffeineCacheManager;
    }
}
