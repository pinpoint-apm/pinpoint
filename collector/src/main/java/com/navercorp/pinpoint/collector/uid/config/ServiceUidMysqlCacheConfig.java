package com.navercorp.pinpoint.collector.uid.config;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.uid.cache.CaffeineCacheProperties;
import com.navercorp.pinpoint.service.dao.ServiceDao;
import com.navercorp.pinpoint.service.vo.ServiceEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StopWatch;

import java.util.List;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "pinpoint.modules.service.dao.type", havingValue = "mysql")
public class ServiceUidMysqlCacheConfig {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public ServiceUidMysqlCacheConfig() {
        logger.info("Install {}", ServiceUidMysqlCacheConfig.class.getSimpleName());
    }

    public static final String SERVICE_UID_CACHE_NAME = "collectorServiceUidCache";

    @Bean
    @ConfigurationProperties(prefix = "collector.service.uid.cache")
    public CaffeineCacheProperties serviceUidCacheProperties() {
        return new CaffeineCacheProperties();
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
        if (properties.getExpireAfterAccess() != null) {
            builder.expireAfterAccess(properties.getExpireAfterAccess());
        }
        return builder;
    }

    @Bean
    public Cache<String, ServiceUid> serviceUidCache(@Qualifier("serviceUidCacheProperties") CaffeineCacheProperties properties) {
        return buildCaffeine(properties).build();
    }

    @Bean
    @ConditionalOnProperty(value = "collector.service.uid.cache.warmup.enabled", havingValue = "true", matchIfMissing = true)
    public ApplicationListener<ApplicationReadyEvent> cacheWarmUpListener(@Qualifier("serviceUidCacheProperties") CaffeineCacheProperties properties,
                                                                          @Qualifier("serviceUidCache") Cache<String, ServiceUid> cache,
                                                                          ServiceDao serviceDao) {
        return event -> {
            StopWatch stopWatch = new StopWatch();
            logger.info("Starting cache warm-up for {} with initial capacity: {}", SERVICE_UID_CACHE_NAME, properties.getInitialCapacity());
            try {
                stopWatch.start("cache warm up");
                List<ServiceEntry> serviceEntries = serviceDao.selectServiceList(properties.getInitialCapacity());
                for (ServiceEntry entry : serviceEntries) {
                    cache.put(entry.getName(), ServiceUid.of(entry.getUid()));
                }
                stopWatch.stop();
                logger.debug(stopWatch.prettyPrint());
                logger.info("Cache warm-up completed for {}. {} entries, Time taken: {} ms", SERVICE_UID_CACHE_NAME, serviceEntries.size(), stopWatch.getTotalTimeMillis());
            } catch (Exception e) {
                logger.warn("Failed to warm up cache for {}: {}", SERVICE_UID_CACHE_NAME, e.getMessage(), e);
            }
        };
    }
}
