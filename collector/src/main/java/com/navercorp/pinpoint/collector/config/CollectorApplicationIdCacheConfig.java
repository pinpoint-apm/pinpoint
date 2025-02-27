package com.navercorp.pinpoint.collector.config;


import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@ConditionalOnProperty(value = "pinpoint.collector.application.uid.enable", havingValue = "true")
public class CollectorApplicationIdCacheConfig {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public CollectorApplicationIdCacheConfig() {
        logger.info("Install {}", CollectorApplicationIdCacheConfig.class.getSimpleName());
    }

    public static final String APPLICATION_UID_CACHE_NAME = "applicationUidCache";

    @Bean
    public CacheManager applicationUidCache() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(APPLICATION_UID_CACHE_NAME);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(600, TimeUnit.SECONDS)
                .initialCapacity(200)
                .maximumSize(1000)
        );
        return cacheManager;
    }
}
