package com.navercorp.pinpoint.web.uid.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.navercorp.pinpoint.common.server.uid.cache.CaffeineCacheProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "pinpoint.modules.service.dao.type", havingValue = "mysql")
public class ServiceUidMysqlCacheConfig {

    public static final String SERVICE_UID_CACHE_NAME = "applicationUidCache";
    public static final String SERVICE_NAME_CACHE_NAME = "applicationNameCache";

    @Bean
    @ConfigurationProperties(prefix = "web.service.uid.cache")
    public CaffeineCacheProperties serviceUidCacheProperties() {
        return new CaffeineCacheProperties();
    }

    @Bean("serviceUidCache")
    public CacheManager mySqlServiceUidCache(@Qualifier("serviceUidCacheProperties") CaffeineCacheProperties caffeineCacheProperties) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(SERVICE_UID_CACHE_NAME, SERVICE_NAME_CACHE_NAME);
        cacheManager.setCaffeine(buildCaffeine(caffeineCacheProperties));
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
