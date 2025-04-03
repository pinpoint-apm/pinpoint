package com.navercorp.pinpoint.collector.uid.service.async;

import com.navercorp.pinpoint.collector.uid.config.ApplicationUidConfig;
import com.navercorp.pinpoint.collector.uid.dao.ApplicationNameDao;
import com.navercorp.pinpoint.collector.uid.dao.ApplicationUidDao;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@ConditionalOnProperty(value = "pinpoint.collector.application.uid.enable", havingValue = "true")
public class AsyncApplicationUidServiceImpl implements AsyncApplicationUidService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationUidDao applicationUidDao;
    private final ApplicationNameDao applicationNameDao;
    private final IdGenerator<ApplicationUid> applicationIdGenerator;
    private final Cache applicationUidCache;

    public AsyncApplicationUidServiceImpl(ApplicationUidDao applicationUidDao, ApplicationNameDao applicationNameDao,
                                          IdGenerator<ApplicationUid> applicationIdGenerator,
                                          @Qualifier(ApplicationUidConfig.APPLICATION_UID_CACHE_NAME) CacheManager cacheManager) {
        this.applicationUidDao = Objects.requireNonNull(applicationUidDao, "applicationUidDao");
        this.applicationNameDao = Objects.requireNonNull(applicationNameDao, "applicationNameDao");
        this.applicationIdGenerator = Objects.requireNonNull(applicationIdGenerator, "applicationIdGenerator");
        this.applicationUidCache = Objects.requireNonNull(cacheManager, "cacheManager").getCache("applicationUidCache");
    }

    private SimpleKey createCacheKey(ServiceUid serviceUid, String applicationName) {
        return new SimpleKey(serviceUid, applicationName);
    }

    // Returns from cache if available but does not cache the result
    // Caching is handled by async getOrCreateApplicationUid and synchronous methods
    @Override
    public CompletableFuture<ApplicationUid> getApplicationUid(ServiceUid serviceUid, String applicationName) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationName, "applicationName");

        ApplicationUid applicationUid = applicationUidCache.get(createCacheKey(serviceUid, applicationName), ApplicationUid.class);
        if (applicationUid != null) {
            return CompletableFuture.completedFuture(applicationUid);
        }

        return applicationUidDao.asyncSelectApplicationUid(serviceUid, applicationName);
    }

    @Override
    public CompletableFuture<ApplicationUid> getOrCreateApplicationUid(ServiceUid serviceUid, String applicationName) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationName, "applicationName");

        return applicationUidCache.retrieve(createCacheKey(serviceUid, applicationName), () -> applicationUidDao.asyncSelectApplicationUid(serviceUid, applicationName)
                .thenCompose(applicationUid -> {
                    if (applicationUid != null) {
                        return CompletableFuture.completedFuture(applicationUid);
                    }
                    return tryInsertApplicationUid(serviceUid, applicationName);
                })
                .thenCompose(newApplicationUid -> {
                    if (newApplicationUid != null) {
                        return CompletableFuture.completedFuture(newApplicationUid);
                    }
                    return applicationUidDao.asyncSelectApplicationUid(serviceUid, applicationName);
                })
        );
    }

    private CompletableFuture<ApplicationUid> tryInsertApplicationUid(ServiceUid serviceUid, String applicationName) {
        return insertApplicationNameWithRetries(serviceUid, applicationName, 3)
                .thenCompose(newApplicationUid -> {
                    if (newApplicationUid == null) {
                        logger.error("ApplicationUid collision. applicationName: {}, maxRetries: {}", applicationName, 3);
                        return CompletableFuture.completedFuture(null);
                    }
                    logger.debug("saved ({}, {} -> name:{})", serviceUid, newApplicationUid, applicationName);
                    return insertApplicationUidWithRollback(serviceUid, applicationName, newApplicationUid);
                });
    }

    private CompletableFuture<ApplicationUid> insertApplicationUidWithRollback(ServiceUid serviceUid, String applicationName, ApplicationUid newApplicationUid) {
        return applicationUidDao.asyncInsertApplicationUidIfNotExists(serviceUid, applicationName, newApplicationUid)
                .exceptionally(throwable -> {
                    logger.error("Failed to insert applicationUid. {}, name:{}, {}", serviceUid, applicationName, newApplicationUid, throwable);
                    return false;
                })
                .thenCompose(success -> {
                    if (!success) {
                        logger.debug("rollback. {}, {}, name:{}", serviceUid, newApplicationUid, applicationName);
                        applicationNameDao.asyncDeleteApplicationName(serviceUid, newApplicationUid).whenComplete((result, throwable) -> {
                            if (throwable != null) {
                                logger.error("Failed to delete applicationName. {}, name:{}", serviceUid, applicationName, throwable);
                            }
                        });
                        return CompletableFuture.completedFuture(null);
                    }
                    logger.info("saved ({}, name:{} -> {})", serviceUid, applicationName, newApplicationUid);
                    return CompletableFuture.completedFuture(newApplicationUid);
                });
    }


    private CompletableFuture<ApplicationUid> insertApplicationNameWithRetries(ServiceUid serviceUid, String applicationName, int maxRetries) {
        CompletableFuture<ApplicationUid> future = CompletableFuture.completedFuture(null);
        for (int i = 0; i < maxRetries; i++) {
            future = future.thenCompose(applicationUid -> {
                if (applicationUid != null) {
                    return CompletableFuture.completedFuture(applicationUid);
                }

                ApplicationUid newApplicationUid = applicationIdGenerator.generate();
                return applicationNameDao.asyncInsertApplicationNameIfNotExists(serviceUid, newApplicationUid, applicationName)
                        .thenApply(success -> {
                            if (success) {
                                return newApplicationUid;
                            }
                            return null;
                        });
            });
        }
        return future;
    }
}
