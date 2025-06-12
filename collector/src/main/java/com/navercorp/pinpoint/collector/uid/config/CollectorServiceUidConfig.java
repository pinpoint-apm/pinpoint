package com.navercorp.pinpoint.collector.uid.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.navercorp.pinpoint.uid.config.cache.CaffeineCacheProperties;
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
@ConditionalOnProperty(name = "pinpoint.modules.v4.enabled", havingValue = "true")
public class CollectorServiceUidConfig {

    public static final String SERVICE_UID_CACHE_NAME = "serviceUidCache";

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
