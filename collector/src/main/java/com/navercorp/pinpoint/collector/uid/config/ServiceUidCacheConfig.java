package com.navercorp.pinpoint.collector.uid.config;


import com.github.benmanes.caffeine.cache.Caffeine;
import com.navercorp.pinpoint.common.server.uid.cache.CaffeineCacheProperties;
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
    @ConfigurationProperties(prefix = "collector.service.uid.cache")
    public CaffeineCacheProperties serviceUidCacheProperties() {
        return new CaffeineCacheProperties();
    }

    @Bean
    public CacheManager collectorServiceUidCache(@Qualifier("serviceUidCacheProperties") CaffeineCacheProperties caffeineCacheProperties) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(SERVICE_UID_CACHE_NAME);
        caffeineCacheManager.setCaffeine(buildCaffeine(caffeineCacheProperties));
        return caffeineCacheManager;
    }

    private Caffeine<Object, Object> buildCaffeine(CaffeineCacheProperties properties) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        if (properties.getInitialCapacity() != -1) {
            builder.initialCapacity(properties.getInitialCapacity());
        }
        if (properties.getMaximumSize() != -1) {
            builder.maximumSize(properties.getMaximumSize());
        }
        if (properties.isRecordStats()) {
            builder.recordStats();
        }

        if (properties.getExpireAfterWrite() != null) {
            builder.expireAfterWrite(properties.getExpireAfterWrite());
        }
        return builder;
    }
}
