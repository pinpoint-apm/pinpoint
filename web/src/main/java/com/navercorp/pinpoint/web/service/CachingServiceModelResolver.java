package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.uid.cache.CaffeineCacheProperties;
import com.navercorp.pinpoint.service.service.ServiceRegistryService;
import com.navercorp.pinpoint.service.vo.ServiceEntity;
import com.navercorp.pinpoint.web.vo.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class CachingServiceModelResolver extends ServiceModelResolver implements ApplicationRunner {

    private static final int DEFAULT_LOAD_LIMIT_PERCENT = 90;

    private final Logger logger = LogManager.getLogger(getClass());

    private final ServiceRegistryService serviceRegistryService;
    private final CaffeineCacheProperties cacheProperties;
    private final ServiceModelLoadProperties loadProperties;
    private final Cache serviceByNameCache;
    private final Cache serviceByUidCache;
    private final AtomicBoolean loading = new AtomicBoolean(false);

    public CachingServiceModelResolver(ServiceRegistryService serviceRegistryService,
                                       CacheManager cacheManager,
                                       CaffeineCacheProperties cacheProperties,
                                       ServiceModelLoadProperties loadProperties) {
        super(serviceRegistryService);
        this.serviceRegistryService = serviceRegistryService;
        Objects.requireNonNull(cacheManager, "cacheManager");
        this.serviceByNameCache = Objects.requireNonNull(
                cacheManager.getCache(ServiceModelCacheConfiguration.SERVICE_BY_NAME_CACHE_NAME), "serviceByNameCache");
        this.serviceByUidCache = Objects.requireNonNull(
                cacheManager.getCache(ServiceModelCacheConfiguration.SERVICE_BY_UID_CACHE_NAME), "serviceByUidCache");
        this.cacheProperties = Objects.requireNonNull(cacheProperties, "cacheProperties");
        this.loadProperties = Objects.requireNonNull(loadProperties, "loadProperties");
    }

    @Override
    protected Service resolveService(int serviceUid) {
        return serviceByUidCache.get(serviceUid, () -> super.resolveService(serviceUid));
    }

    @Override
    protected Service resolveService(String serviceName) {
        return serviceByNameCache.get(serviceName, () -> super.resolveService(serviceName));
    }

    @Override
    public void run(ApplicationArguments args) {
        warmup();
    }

    public void warmup() {
        if (!loadProperties.isWarmupEnabled()) {
            logger.info("Skip service model cache startup warmup.");
            return;
        }
        long startTime = System.currentTimeMillis();
        int count = doLoad("Warm up");
        if (count < 0) {
            return;
        }
        logger.info("Warm up service model cache. {} entries, Time taken: {} ms",
                count, System.currentTimeMillis() - startTime);
    }

    @Scheduled(
            initialDelayString = "#{@webServiceModelLoadProperties.refreshInterval}",
            fixedRateString = "#{@webServiceModelLoadProperties.refreshInterval}")
    public void refresh() {
        if (!loadProperties.isRefreshEnabled()) {
            logger.debug("Skip service model cache scheduled refresh.");
            return;
        }
        int count = doLoad("Refresh");
        if (count < 0) {
            return;
        }
        logger.debug("Refresh service model cache. {} entries", count);
    }

    private int doLoad(String operation) {
        int limit = getLoadLimit(loadProperties.getLimit(), cacheProperties.getMaximumSize());
        if (limit <= 0) {
            logger.info("Skip service model cache {}. limit={}", operation, limit);
            return -1;
        }
        return load(limit, operation);
    }

    private int load(int limit, String operation) {
        if (!loading.compareAndSet(false, true)) {
            logger.debug("Skip service model cache {}. Previous load is still running.", operation);
            return -1;
        }

        try {
            List<ServiceEntity> serviceList = serviceRegistryService.getServiceList(limit);
            for (ServiceEntity entity : serviceList) {
                Service service = toService(entity);
                serviceByNameCache.put(service.getServiceName(), service);
                serviceByUidCache.put(service.getServiceUid().getUid(), service);
            }
            return serviceList.size();
        } catch (Exception e) {
            logger.error("Failed to load service model cache. operation={}", operation, e);
            return -1;
        } finally {
            loading.set(false);
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
