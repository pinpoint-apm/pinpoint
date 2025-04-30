package com.navercorp.pinpoint.collector.uid.config;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.cache.CaffeineCacheProperties;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import com.navercorp.pinpoint.common.server.util.RandomApplicationUidGenerator;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
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
@ConditionalOnProperty(value = "pinpoint.collector.application.uid.enable", havingValue = "true")
public class ApplicationUidConfig {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public ApplicationUidConfig() {
        logger.info("Install {}", ApplicationUidConfig.class.getSimpleName());
    }

    public static final String APPLICATION_UID_CACHE_NAME = "applicationUidCache";

    @Bean
    @ConfigurationProperties(prefix = "collector.application.uid.cache")
    public CaffeineCacheProperties applicationUidCacheProperties() {
        return new CaffeineCacheProperties();
    }

    @Bean
    public CacheManager applicationUidCache(@Qualifier("applicationUidCacheProperties") CaffeineCacheProperties caffeineCacheProperties) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(APPLICATION_UID_CACHE_NAME);
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

    @Bean
    public IdGenerator<ApplicationUid> applicationUidGenerator() {
        return new RandomApplicationUidGenerator();
    }


    //micrometer caffeine cache metrics
    @Bean
    public MeterBinder caffeineCacheMeterBinder(@Qualifier("applicationUidCache") CacheManager cacheManager,
                                                @Qualifier("applicationUidCacheProperties") CaffeineCacheProperties properties) {
        if (!properties.isRecordStats()) {
            logger.info("Skipping CaffeineCacheMetrics. recordStats is false");
            return meterRegistry -> {};
        }
        org.springframework.cache.Cache springCache = cacheManager.getCache(APPLICATION_UID_CACHE_NAME);
        if (springCache == null) {
            logger.warn("Skipping CaffeineCacheMetrics. cache '{}' not found", APPLICATION_UID_CACHE_NAME);
            return meterRegistry -> {};
        }

        Cache<?, ?> nativeCache = (Cache<?, ?>) springCache.getNativeCache();
        return meterRegistry -> CaffeineCacheMetrics.monitor(meterRegistry, nativeCache, APPLICATION_UID_CACHE_NAME);
    }
}
