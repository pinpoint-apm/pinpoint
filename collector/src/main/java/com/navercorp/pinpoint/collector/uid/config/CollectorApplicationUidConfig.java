package com.navercorp.pinpoint.collector.uid.config;


import com.github.benmanes.caffeine.cache.Caffeine;
import com.navercorp.pinpoint.uid.config.cache.CaffeineCacheProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CollectorApplicationUidConfig {

    public static final String APPLICATION_UID_CACHE_NAME = "applicationUidCache";

    @Bean
    @ConfigurationProperties(prefix = "collector.application.uid.cache")
    public CaffeineCacheProperties applicationUidCacheProperties() {
        return new CaffeineCacheProperties();
    }

    @Bean
    public CacheManager applicationUidCache(@Qualifier("applicationUidCacheProperties") CaffeineCacheProperties properties) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(APPLICATION_UID_CACHE_NAME);
        cacheManager.setCaffeine(buildCaffeine(properties));
        cacheManager.setAsyncCacheMode(true);
        cacheManager.setAllowNullValues(false);
        return cacheManager;
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
