package com.navercorp.pinpoint.web.config;

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
@ConditionalOnProperty(name = "pinpoint.web.application.uid.enable", havingValue = "true")
public class WebApplicationIdCacheConfig {

    private final Logger logger = LogManager.getLogger(WebApplicationIdCacheConfig.class);

    public WebApplicationIdCacheConfig() {
        logger.info("Install {}", WebApplicationIdCacheConfig.class.getSimpleName());
    }

    public static final String APPLICATION_UID_CACHE_NAME = "applicationUidCache";
    public static final String APPLICATION_NAME_CACHE_NAME = "applicationNameCache";

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

    @Bean
    public CacheManager applicationNameCache() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(APPLICATION_NAME_CACHE_NAME);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(600, TimeUnit.SECONDS)
                .initialCapacity(200)
                .maximumSize(1000)
        );
        return cacheManager;
    }
}
