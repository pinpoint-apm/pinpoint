package com.navercorp.pinpoint.web.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class WebPinpointIdCacheConfig {

    private final Logger logger = LogManager.getLogger(WebPinpointIdCacheConfig.class);

    public WebPinpointIdCacheConfig() {
        logger.info("Install {}", WebPinpointIdCacheConfig.class.getSimpleName());
    }

    public static final String SERVICE_UID_CACHE_NAME = "serviceUidCache";
    public static final String SERVICE_NAME_CACHE_NAME = "serviceNameCache";

    @Bean
    public CacheManager serviceUidCache() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(SERVICE_UID_CACHE_NAME);
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(600, TimeUnit.SECONDS)
                .initialCapacity(10)
                .maximumSize(200));
        return caffeineCacheManager;
    }

    @Bean
    public CacheManager serviceNameCache() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(SERVICE_NAME_CACHE_NAME);
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(600, TimeUnit.SECONDS)
                .initialCapacity(10)
                .maximumSize(200));
        return caffeineCacheManager;
    }
}
