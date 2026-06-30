package com.navercorp.pinpoint.collector.uid.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.navercorp.pinpoint.common.server.cache.NullValueExpiry;
import com.navercorp.pinpoint.common.server.uid.cache.CaffeineCacheProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;

@Configuration
@EnableScheduling
public class ServiceLookupCacheConfiguration {

    public static final String CACHE_MANAGER_NAME = "collectorServiceLookupCacheManager";
    public static final String SERVICE_LOOKUP_CACHE_NAME = "collectorServiceLookupCache";

    @Bean
    @ConfigurationProperties(prefix = "collector.service.lookup.cache")
    public CaffeineCacheProperties collectorServiceLookupCacheProperties() {
        return new CaffeineCacheProperties();
    }

    @Bean
    public ServiceLookupLoadProperties collectorServiceLookupLoadProperties() {
        return new ServiceLookupLoadProperties();
    }

    @Bean(CACHE_MANAGER_NAME)
    public CacheManager collectorServiceLookupCacheManager(
            @Qualifier("collectorServiceLookupCacheProperties") CaffeineCacheProperties properties,
            @Value("${collector.service.lookup.cache.missingExpireAfterWrite:1m}") Duration missingExpireAfterWrite) {

        CaffeineCacheManager cacheManager = new CaffeineCacheManager(SERVICE_LOOKUP_CACHE_NAME);
        cacheManager.setCaffeine(buildCaffeine(properties, missingExpireAfterWrite));
        cacheManager.setAsyncCacheMode(true);
        cacheManager.setAllowNullValues(true);
        return cacheManager;
    }

    private Caffeine<Object, Object> buildCaffeine(CaffeineCacheProperties properties,
                                                   Duration missingExpireAfterWrite) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        if (properties.getInitialCapacity() >= 0) {
            builder.initialCapacity(properties.getInitialCapacity());
        }
        if (properties.getMaximumSize() >= 0) {
            builder.maximumSize(properties.getMaximumSize());
        }
        if (properties.isRecordStats()) {
            builder.recordStats();
        }

        builder.expireAfter(new NullValueExpiry<>(
                properties.getExpireAfterWrite(),
                properties.getExpireAfterAccess(),
                missingExpireAfterWrite));
        return builder;
    }
}
