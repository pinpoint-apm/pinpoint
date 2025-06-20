package com.navercorp.pinpoint.uid.service.async;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import com.navercorp.pinpoint.uid.dao.ApplicationNameDao;
import com.navercorp.pinpoint.uid.dao.ApplicationUidDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncApplicationUidServiceImpl implements AsyncApplicationUidService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationUidDao applicationUidDao;
    private final ApplicationNameDao applicationNameDao;
    private final IdGenerator<ApplicationUid> applicationIdGenerator;

    public AsyncApplicationUidServiceImpl(ApplicationUidDao applicationUidDao, ApplicationNameDao applicationNameDao,
                                          IdGenerator<ApplicationUid> applicationIdGenerator) {
        this.applicationUidDao = Objects.requireNonNull(applicationUidDao, "applicationUidDao");
        this.applicationNameDao = Objects.requireNonNull(applicationNameDao, "applicationNameDao");
        this.applicationIdGenerator = Objects.requireNonNull(applicationIdGenerator, "applicationIdGenerator");
    }

    @Override
    public CompletableFuture<ApplicationUid> getApplicationUid(ServiceUid serviceUid, String applicationName) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationName, "applicationName");
        return applicationUidDao.asyncSelectApplicationUid(serviceUid, applicationName);
    }

    @Override
    public CompletableFuture<ApplicationUid> getOrCreateApplicationUid(ServiceUid serviceUid, String applicationName) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationName, "applicationName");

        return applicationUidDao.asyncSelectApplicationUid(serviceUid, applicationName)
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
                });
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
