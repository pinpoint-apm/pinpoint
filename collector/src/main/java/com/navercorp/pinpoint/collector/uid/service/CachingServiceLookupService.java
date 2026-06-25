package com.navercorp.pinpoint.collector.uid.service;

import com.navercorp.pinpoint.collector.uid.config.ServiceLookupCacheConfiguration;
import com.navercorp.pinpoint.collector.uid.config.ServiceLookupLoadProperties;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.uid.cache.CaffeineCacheProperties;
import com.navercorp.pinpoint.service.dao.ServiceRegistryDao;
import com.navercorp.pinpoint.service.vo.ServiceEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public class CachingServiceLookupService implements ServiceLookupService, ApplicationRunner {

    private static final int DEFAULT_LOAD_LIMIT_PERCENT = 90;

    private final Logger logger = LogManager.getLogger(getClass());
    private final ThrottledLogger tLogger = ThrottledLogger.getLogger(logger, 100);

    private final CaffeineCacheProperties properties;
    private final ServiceLookupLoadProperties loadProperties;
    private final Cache serviceLookupCache;
    private final ServiceRegistryDao serviceRegistryDao;
    private final Executor executor;
    private final AtomicBoolean refreshing = new AtomicBoolean(false);

    public CachingServiceLookupService(CaffeineCacheProperties properties,
                                       CacheManager cacheManager,
                                       ServiceRegistryDao serviceRegistryDao,
                                       ServiceLookupLoadProperties loadProperties,
                                       Executor executor) {
        this.properties = Objects.requireNonNull(properties, "properties");
        this.loadProperties = Objects.requireNonNull(loadProperties, "loadProperties");
        Objects.requireNonNull(cacheManager, "cacheManager");
        this.serviceLookupCache = Objects.requireNonNull(
                cacheManager.getCache(ServiceLookupCacheConfiguration.SERVICE_LOOKUP_CACHE_NAME), "serviceLookupCache");
        this.serviceRegistryDao = Objects.requireNonNull(serviceRegistryDao, "serviceRegistryDao");
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    @Override
    public CompletableFuture<ServiceUid> getServiceUid(String serviceName) {
        Objects.requireNonNull(serviceName, "serviceName");
        if (ServiceUid.DEFAULT_SERVICE_UID_NAME.equals(serviceName)) {
            return CompletableFuture.completedFuture(ServiceUid.DEFAULT);
        }
        return serviceLookupCache.retrieve(serviceName, () -> loadServiceUidAsync(serviceName));
    }

    private CompletableFuture<ServiceUid> loadServiceUidAsync(String serviceName) {
        return CompletableFuture.supplyAsync(() -> {
            ServiceEntity service = serviceRegistryDao.selectService(serviceName);
            if (service == null) {
                return null;
            }
            return ServiceUid.of(service.getUid());
        }, executor);
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!loadProperties.isWarmupEnabled()) {
            logger.info("Skip {} startup warmup.", ServiceLookupCacheConfiguration.SERVICE_LOOKUP_CACHE_NAME);
            return;
        }
        warmup();
    }

    private void warmup() {
        int limit = getLoadLimit(loadProperties.getLimit(), properties.getMaximumSize());
        if (limit <= 0) {
            logger.info("Skip {} startup warmup. limit={}", ServiceLookupCacheConfiguration.SERVICE_LOOKUP_CACHE_NAME, limit);
            return;
        }

        StopWatch stopWatch = new StopWatch(ServiceLookupCacheConfiguration.SERVICE_LOOKUP_CACHE_NAME);
        stopWatch.start("Warm up");
        int count = load(limit, "Warm up");
        stopWatch.stop();

        if (count < 0) {
            return;
        }
        tLogger.info("Warm up {}. {}/{} entries, Time taken: {} ms",
                ServiceLookupCacheConfiguration.SERVICE_LOOKUP_CACHE_NAME,
                count, limit, stopWatch.getTotalTimeMillis());
        logger.debug(stopWatch.prettyPrint());
    }

    @Scheduled(fixedRateString = "#{@collectorServiceLookupLoadProperties.refreshInterval}")
    public void refresh() {
        if (!loadProperties.isRefreshEnabled()) {
            logger.debug("Skip {} scheduled refresh.", ServiceLookupCacheConfiguration.SERVICE_LOOKUP_CACHE_NAME);
            return;
        }
        int limit = getLoadLimit(loadProperties.getLimit(), properties.getMaximumSize());
        if (limit <= 0) {
            logger.info("Skip {} scheduled refresh. limit={}", ServiceLookupCacheConfiguration.SERVICE_LOOKUP_CACHE_NAME, limit);
            return;
        }

        int count = load(limit, "Refresh");
        if (count < 0) {
            return;
        }
        tLogger.info("Refresh {}. {}/{} entries",
                ServiceLookupCacheConfiguration.SERVICE_LOOKUP_CACHE_NAME, count, limit);
    }

    private int load(int limit, String operation) {
        if (!refreshing.compareAndSet(false, true)) {
            logger.debug("Skip {} {}. Previous load is still running.",
                    ServiceLookupCacheConfiguration.SERVICE_LOOKUP_CACHE_NAME, operation);
            return -1;
        }

        try {
            List<ServiceEntity> serviceList = serviceRegistryDao.selectServiceList(limit);
            for (ServiceEntity service : serviceList) {
                serviceLookupCache.put(service.getName(), ServiceUid.of(service.getUid()));
            }
            return serviceList.size();
        } finally {
            refreshing.set(false);
        }
    }

    private int getLoadLimit(long configuredLimit, long maximumSize) {
        if (configuredLimit >= 0) {
            return (int) Math.min(configuredLimit, Integer.MAX_VALUE);
        }
        if (maximumSize <= 0) {
            return 0;
        }
        long limit = Math.max(1, (long) Math.ceil(maximumSize * (DEFAULT_LOAD_LIMIT_PERCENT / 100.0)));
        return (int) Math.min(limit, Integer.MAX_VALUE);
    }
}
