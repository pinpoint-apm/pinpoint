package com.navercorp.pinpoint.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import com.navercorp.pinpoint.uid.dao.ApplicationNameDao;
import com.navercorp.pinpoint.uid.dao.ApplicationUidDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
public class BaseApplicationUidServiceImpl implements BaseApplicationUidService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationUidDao applicationUidDao;
    private final ApplicationNameDao applicationNameDao;
    private final IdGenerator<ApplicationUid> applicationUidGenerator;

    public BaseApplicationUidServiceImpl(ApplicationUidDao applicationUidDao, ApplicationNameDao applicationNameDao,
                                         @Qualifier("applicationUidGenerator") IdGenerator<ApplicationUid> applicationUidGenerator) {
        this.applicationUidDao = Objects.requireNonNull(applicationUidDao, "applicationIdDao");
        this.applicationNameDao = Objects.requireNonNull(applicationNameDao, "applicationInfoDao");
        this.applicationUidGenerator = Objects.requireNonNull(applicationUidGenerator, "applicationIdGenerator");
    }

    @Override
    public ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationName, "applicationName");
        return applicationUidDao.selectApplicationUid(serviceUid, applicationName);
    }

    @Override
    public ApplicationUid getOrCreateApplicationUid(ServiceUid serviceUid, String applicationName) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationName, "applicationName");

        ApplicationUid applicationUid = applicationUidDao.selectApplicationUid(serviceUid, applicationName);
        if (applicationUid != null) {
            return applicationUid;
        }

        ApplicationUid newApplicationUid = tryInsertApplicationUid(serviceUid, applicationName);
        if (newApplicationUid != null) {
            return newApplicationUid;
        }

        ApplicationUid retriedApplicationUid = applicationUidDao.selectApplicationUid(serviceUid, applicationName);
        if (retriedApplicationUid != null) {
            return retriedApplicationUid;
        }
        throw new IllegalStateException("Failed to create ApplicationUid for serviceUid: " + serviceUid + ", applicationName: " + applicationName);
    }

    private ApplicationUid tryInsertApplicationUid(ServiceUid serviceUid, String applicationName) {
        ApplicationUid newApplicationUid = insertApplicationNameWithRetries(serviceUid, applicationName, 3);
        if (newApplicationUid == null) {
            return null;
        }
        logger.debug("saved ({}, {} -> name:{})", serviceUid, newApplicationUid, applicationName);
        return insertApplicationUidWithRollback(serviceUid, applicationName, newApplicationUid);
    }

    private ApplicationUid insertApplicationUidWithRollback(ServiceUid serviceUid, String applicationName, ApplicationUid newApplicationUid) {
        try {
            boolean success = applicationUidDao.insertApplicationUidIfNotExists(serviceUid, applicationName, newApplicationUid);
            if (success) {
                logger.info("saved ({}, name:{} -> {})", serviceUid, applicationName, newApplicationUid);
                return newApplicationUid;
            }
        } catch (Throwable throwable) {
            logger.error("Failed to insert applicationUid. {}, name:{}, {}", serviceUid, applicationName, newApplicationUid, throwable);
        }

        logger.debug("rollback. {}, {}, name:{}", serviceUid, newApplicationUid, applicationName);
        try {
            applicationNameDao.deleteApplicationName(serviceUid, newApplicationUid);
        } catch (Throwable throwable) {
            logger.error("Failed to delete applicationName. {}, name:{}, {}", serviceUid, applicationName, newApplicationUid, throwable);
        }
        return null;
    }

    private ApplicationUid insertApplicationNameWithRetries(ServiceUid serviceUid, String applicationName, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            ApplicationUid newApplicationUid = applicationUidGenerator.generate();
            boolean nameInsertResult = applicationNameDao.insertApplicationNameIfNotExists(serviceUid, newApplicationUid, applicationName);
            if (nameInsertResult) {
                return newApplicationUid;
            }
        }
        logger.error("ApplicationUid collision. applicationName: {}, maxRetries: {}", applicationName, maxRetries);
        return null;
    }

    @Override
    public List<String> getApplicationNames(ServiceUid serviceUid) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        return applicationUidDao.selectApplicationNames(serviceUid);
    }

    @Override
    public String getApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationUid, "applicationUid");
        return applicationNameDao.selectApplicationName(serviceUid, applicationUid);
    }

    @Override
    public void deleteApplication(ServiceUid serviceUid, String applicationName) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationName, "applicationName");

        ApplicationUid applicationUid = getApplicationUid(serviceUid, applicationName);
        applicationUidDao.deleteApplicationUid(serviceUid, applicationName);
        logger.info("deleted ({}, name:{} -> {})", serviceUid, applicationName, applicationUid);
        if (applicationUid != null) {
            applicationNameDao.deleteApplicationName(serviceUid, applicationUid);
            logger.info("deleted ({} -> name:{})", applicationUid, applicationName);
        }
    }

    // async
    @Override
    public CompletableFuture<ApplicationUid> asyncGetOrCreateApplicationUid(ServiceUid serviceUid, String applicationName) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationName, "applicationName");

        return applicationUidDao.asyncSelectApplicationUid(serviceUid, applicationName)
                .thenCompose(applicationUid -> {
                    if (applicationUid != null) {
                        return CompletableFuture.completedFuture(applicationUid);
                    }
                    return asyncTryInsertApplicationUid(serviceUid, applicationName);
                })
                .thenCompose(newApplicationUid -> {
                    if (newApplicationUid != null) {
                        return CompletableFuture.completedFuture(newApplicationUid);
                    }
                    return applicationUidDao.asyncSelectApplicationUid(serviceUid, applicationName);
                }).thenApply(applicationUid -> {
                    if (applicationUid != null) {
                        return applicationUid;
                    }
                    throw new IllegalStateException("Failed to create ApplicationUid for serviceUid: " + serviceUid + ", applicationName: " + applicationName);
                });
    }

    private CompletableFuture<ApplicationUid> asyncTryInsertApplicationUid(ServiceUid serviceUid, String applicationName) {
        return asyncInsertApplicationNameWithRetries(serviceUid, applicationName, 3)
                .thenCompose(newApplicationUid -> {
                    if (newApplicationUid == null) {
                        logger.error("ApplicationUid collision. applicationName: {}, maxRetries: {}", applicationName, 3);
                        return CompletableFuture.completedFuture(null);
                    }
                    logger.debug("saved ({}, {} -> name:{})", serviceUid, newApplicationUid, applicationName);
                    return asyncInsertApplicationUidWithRollback(serviceUid, applicationName, newApplicationUid);
                });
    }

    private CompletableFuture<ApplicationUid> asyncInsertApplicationUidWithRollback(ServiceUid serviceUid, String applicationName, ApplicationUid newApplicationUid) {
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


    private CompletableFuture<ApplicationUid> asyncInsertApplicationNameWithRetries(ServiceUid serviceUid, String applicationName, int maxRetries) {
        CompletableFuture<ApplicationUid> future = CompletableFuture.completedFuture(null);
        for (int i = 0; i < maxRetries; i++) {
            future = future.thenCompose(applicationUid -> {
                if (applicationUid != null) {
                    return CompletableFuture.completedFuture(applicationUid);
                }

                ApplicationUid newApplicationUid = applicationUidGenerator.generate();
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
