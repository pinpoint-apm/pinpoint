package com.navercorp.pinpoint.web.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.navercorp.pinpoint.common.server.cache.NullValueExpiry;
import com.navercorp.pinpoint.common.server.uid.cache.CaffeineCacheProperties;
import com.navercorp.pinpoint.service.service.ServiceRegistryService;
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
public class ServiceModelCacheConfiguration {

    public static final String CACHE_MANAGER_NAME = "webServiceModelCacheManager";
    public static final String SERVICE_BY_NAME_CACHE_NAME = "serviceModelByNameCache";
    public static final String SERVICE_BY_UID_CACHE_NAME = "serviceModelByUidCache";

    @Bean
    @ConfigurationProperties(prefix = "web.service.registry.cache")
    public CaffeineCacheProperties webServiceModelCacheProperties() {
        return new CaffeineCacheProperties();
    }

    @Bean
    public ServiceModelLoadProperties webServiceModelLoadProperties() {
        return new ServiceModelLoadProperties();
    }

    @Bean(CACHE_MANAGER_NAME)
    public CacheManager webServiceModelCacheManager(
            @Qualifier("webServiceModelCacheProperties") CaffeineCacheProperties properties,
            @Value("${web.service.registry.cache.missingExpireAfterWrite:5m}") Duration missingExpireAfterWrite) {

        CaffeineCacheManager cacheManager = new CaffeineCacheManager(SERVICE_BY_NAME_CACHE_NAME, SERVICE_BY_UID_CACHE_NAME);
        cacheManager.setCaffeine(buildCaffeine(properties, missingExpireAfterWrite));
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

    @Bean
    public ServiceModelResolver serviceModelResolver(
            ServiceRegistryService serviceRegistryService,
            @Qualifier(CACHE_MANAGER_NAME) CacheManager cacheManager,
            @Qualifier("webServiceModelCacheProperties") CaffeineCacheProperties cacheProperties,
            ServiceModelLoadProperties loadProperties) {
        return new CachingServiceModelResolver(serviceRegistryService, cacheManager, cacheProperties, loadProperties);
    }
}
