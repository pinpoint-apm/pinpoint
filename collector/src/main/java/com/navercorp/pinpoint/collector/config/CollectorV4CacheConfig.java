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
@ConditionalOnProperty(value = "pinpoint.collector.v4.enable", havingValue = "true")
public class CollectorV4CacheConfig {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public CollectorV4CacheConfig() {
        logger.info("Install {}", CollectorV4CacheConfig.class.getSimpleName());
    }

    public static final String SERVICE_UID_CACHE_NAME = "collectorServiceUidCache";

    @Bean
    public CacheManager collectorServiceUidCache() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(SERVICE_UID_CACHE_NAME);
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(600, TimeUnit.SECONDS)
                .initialCapacity(10)
                .maximumSize(200));
        return caffeineCacheManager;
    }
}
