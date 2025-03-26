package com.navercorp.pinpoint.collector.uid.service.async;

import com.navercorp.pinpoint.collector.uid.dao.ApplicationNameDao;
import com.navercorp.pinpoint.collector.uid.dao.ApplicationUidDao;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

    public AsyncApplicationUidServiceImpl(ApplicationUidDao applicationUidDao, ApplicationNameDao applicationNameDao, IdGenerator<ApplicationUid> applicationIdGenerator) {
        this.applicationUidDao = Objects.requireNonNull(applicationUidDao, "applicationUidDao");
        this.applicationNameDao = Objects.requireNonNull(applicationNameDao, "applicationNameDao");
        this.applicationIdGenerator = Objects.requireNonNull(applicationIdGenerator, "applicationIdGenerator");
    }

    @Override
    public CompletableFuture<ApplicationUid> getApplicationId(ServiceUid serviceUid, String applicationName) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationName, "applicationName");
        return applicationUidDao.asyncSelectApplicationUid(serviceUid, applicationName);
    }

    @Override
    public CompletableFuture<ApplicationUid> getOrCreateApplicationId(ServiceUid serviceUid, String applicationName) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationName, "applicationName");

        return getApplicationId(serviceUid, applicationName)
                .thenCompose(applicationUid -> {
                    if (applicationUid != null) {
                        return CompletableFuture.completedFuture(applicationUid);
                    }
                    return tryInsertApplicationId(serviceUid, applicationName);
                })
                .thenCompose(newApplicationUid -> {
                    if (newApplicationUid != null) {
                        return CompletableFuture.completedFuture(newApplicationUid);
                    }
                    return getApplicationId(serviceUid, applicationName);
                });
    }

    private CompletableFuture<ApplicationUid> tryInsertApplicationId(ServiceUid serviceUid, String applicationName) {
        return insertApplicationNameWithRetries(serviceUid, applicationName, 3)
                .thenCompose(newApplicationUid -> {
                    if (newApplicationUid == null) {
                        logger.error("ApplicationUid collision. applicationName: {}, maxRetries: {}", applicationName, 3);
                        return CompletableFuture.completedFuture(null);
                    }
                    logger.debug("saved ({}, {} -> name:{})", serviceUid, newApplicationUid, applicationName);

                    return applicationUidDao.asyncInsertApplicationUidIfNotExists(serviceUid, applicationName, newApplicationUid)
                            .handle((success, throwable) -> {
                                if (throwable != null) {
                                    logger.error("Failed to insert applicationUid. {}, name{}, {}", serviceUid, applicationName, newApplicationUid, throwable);
                                    applicationNameDao.asyncDeleteApplicationName(serviceUid, newApplicationUid);
                                    return null;
                                } else if (!success) {
                                    logger.debug("applicationName already exists. {}, name:{}", serviceUid, applicationName);
                                    applicationNameDao.asyncDeleteApplicationName(serviceUid, newApplicationUid);
                                    return null;
                                }
                                logger.info("saved ({}, name:{} -> {})", serviceUid, applicationName, newApplicationUid);
                                return newApplicationUid;
                            });
                });
    }


    private CompletableFuture<ApplicationUid> insertApplicationNameWithRetries(ServiceUid serviceUid, String applicationName, int maxRetries) {
        CompletableFuture<ApplicationUid> future = CompletableFuture.completedFuture(null);
        for (int i = 0; i < maxRetries; i++) {
            future = future.thenCompose(applicationUid -> {
                if (applicationUid != null) {
                    return CompletableFuture.completedFuture(applicationUid);
                } else {
                    ApplicationUid newApplicationUid = applicationIdGenerator.generate();
                    return applicationNameDao.asyncInsertApplicationNameIfNotExists(serviceUid, newApplicationUid, applicationName)
                            .thenApply(success -> {
                                if (success) {
                                    return newApplicationUid;
                                }
                                return null;
                            });
                }
            });
        }
        return future;
    }
}
