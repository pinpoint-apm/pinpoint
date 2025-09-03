package com.navercorp.pinpoint.collector.uid.config;


import com.github.benmanes.caffeine.cache.Caffeine;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.uid.cache.CaffeineCacheProperties;
import com.navercorp.pinpoint.common.server.uid.cache.NullValueExpiry;
import com.navercorp.pinpoint.service.dao.ServiceDao;
import com.navercorp.pinpoint.service.vo.ServiceEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StopWatch;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "pinpoint.modules.service.dao.type", havingValue = "mysql")
public class ServiceUidMysqlCacheConfig {

    public static final String SERVICE_UID_CACHE_NAME = "serviceUidCache";

    @Bean
    @ConfigurationProperties(prefix = "collector.service.uid.cache")
    public CaffeineCacheProperties serviceUidCacheProperties() {
        return new CaffeineCacheProperties();
    }

    @Bean
    public CacheManager serviceUidCache(@Qualifier("serviceUidCacheProperties") CaffeineCacheProperties properties,
                                        @Value("${collector.service.uid.cache.nullValueExpireAfterWrite:1m}") Duration nullValueExpireAfterWrite) {

        CaffeineCacheManager cacheManager = new CaffeineCacheManager(SERVICE_UID_CACHE_NAME);
        cacheManager.setCaffeine(buildCaffeine(properties, nullValueExpireAfterWrite));
        cacheManager.setAllowNullValues(true);
        return cacheManager;
    }

    private Caffeine<Object, Object> buildCaffeine(CaffeineCacheProperties properties, Duration nullValueExpireAfterWrite) {
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

        builder.expireAfter(new NullValueExpiry(properties.getExpireAfterWrite(), properties.getExpireAfterAccess(),
                nullValueExpireAfterWrite));
        return builder;
    }


    @Bean
    @ConditionalOnProperty(name = "collector.service.uid.cache.bulk.update.enabled", havingValue = "true", matchIfMissing = true)
    public CacheUpdateScheduler cacheUpdateScheduler(@Qualifier("serviceUidCacheProperties") CaffeineCacheProperties serviceUidCacheProperties,
                                                     @Qualifier("serviceUidCache") CacheManager cacheManager,
                                                     ServiceDao serviceDao) {
        return new CacheUpdateScheduler(serviceUidCacheProperties, cacheManager, serviceDao);
    }

    public static class CacheUpdateScheduler {
        private final Logger logger = LogManager.getLogger(this.getClass());
        private final ThrottledLogger tLogger = ThrottledLogger.getLogger(logger, 100);

        private final CaffeineCacheProperties properties;
        private final CacheManager cacheManager;
        private final ServiceDao serviceDao;

        public CacheUpdateScheduler(CaffeineCacheProperties properties,
                                    CacheManager cacheManager,
                                    ServiceDao serviceDao) {
            this.properties = Objects.requireNonNull(properties, "properties");
            this.cacheManager = Objects.requireNonNull(cacheManager, "cacheManager");
            this.serviceDao = Objects.requireNonNull(serviceDao, "serviceDao");
        }

        @Scheduled(fixedRateString = "${collector.service.uid.cache.bulk.update.interval:60000}")
        public void update() {
            Cache cache = Objects.requireNonNull(cacheManager.getCache(SERVICE_UID_CACHE_NAME));
            StopWatch stopWatch = new StopWatch(SERVICE_UID_CACHE_NAME);
            stopWatch.start("selectServiceList");
            List<ServiceEntry> serviceEntries = serviceDao.selectServiceList((int) properties.getMaximumSize());
            stopWatch.stop();
            stopWatch.start("cache puts");
            for (ServiceEntry entry : serviceEntries) {
                cache.put(entry.getName(), ServiceUid.of(entry.getUid()));
            }
            stopWatch.stop();
            tLogger.info("Update {}. {}/{} entries, Time taken: {} ms", SERVICE_UID_CACHE_NAME, serviceEntries.size(), properties.getMaximumSize(), stopWatch.getTotalTimeMillis());
            logger.debug(stopWatch.prettyPrint());
        }

    }
}
