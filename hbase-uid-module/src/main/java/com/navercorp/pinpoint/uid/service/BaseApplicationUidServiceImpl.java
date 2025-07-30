package com.navercorp.pinpoint.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import com.navercorp.pinpoint.uid.dao.ApplicationUidDao;
import com.navercorp.pinpoint.uid.dao.ApplicationUidAttrDao;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;
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
    private final ApplicationUidAttrDao applicationUidAttrDao;
    private final IdGenerator<ApplicationUid> applicationUidGenerator;

    public BaseApplicationUidServiceImpl(ApplicationUidDao applicationUidDao, ApplicationUidAttrDao applicationUidAttrDao,
                                         @Qualifier("applicationUidGenerator") IdGenerator<ApplicationUid> applicationUidGenerator) {
        this.applicationUidDao = Objects.requireNonNull(applicationUidDao, "applicationIdDao");
        this.applicationUidAttrDao = Objects.requireNonNull(applicationUidAttrDao, "applicationInfoDao");
        this.applicationUidGenerator = Objects.requireNonNull(applicationUidGenerator, "applicationIdGenerator");
    }

    @Override
    public List<ApplicationUid> getApplicationUid(ServiceUid serviceUid, String applicationName) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationName, "applicationUidInfo");

        return applicationUidDao.selectApplicationUid(serviceUid, applicationName);
    }

    @Override
    public ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationName, "applicationName");
        ApplicationUidAttribute applicationUidAttribute = new ApplicationUidAttribute(applicationName, serviceTypeCode);

        return applicationUidDao.selectApplicationUid(serviceUid, applicationUidAttribute);
    }

    @Override
    public ApplicationUid getOrCreateApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationName, "applicationName");
        ApplicationUidAttribute applicationUidAttribute = new ApplicationUidAttribute(applicationName, serviceTypeCode);

        ApplicationUid applicationUid = applicationUidDao.selectApplicationUid(serviceUid, applicationUidAttribute);
        if (applicationUid != null) {
            return applicationUid;
        }

        ApplicationUid newApplicationUid = tryInsertApplicationUid(serviceUid, applicationUidAttribute);
        if (newApplicationUid != null) {
            return newApplicationUid;
        }

        ApplicationUid retriedApplicationUid = applicationUidDao.selectApplicationUid(serviceUid, applicationUidAttribute);
        if (retriedApplicationUid != null) {
            return retriedApplicationUid;
        }
        throw new IllegalStateException("Failed to create ApplicationUid. " + serviceUid + ", " + applicationUidAttribute);
    }

    private ApplicationUid tryInsertApplicationUid(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute) {
        ApplicationUid newApplicationUid = insertApplicationNameWithRetries(serviceUid, applicationUidAttribute, 3);
        if (newApplicationUid == null) {
            return null;
        }
        logger.debug("saved ({}, {} -> {})", serviceUid, newApplicationUid, applicationUidAttribute);
        return insertApplicationUidWithRollback(serviceUid, applicationUidAttribute, newApplicationUid);
    }

    private ApplicationUid insertApplicationUidWithRollback(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute, ApplicationUid newApplicationUid) {
        try {
            boolean success = applicationUidDao.insertApplicationUidIfNotExists(serviceUid, applicationUidAttribute, newApplicationUid);
            if (success) {
                logger.info("saved ({}, {} -> {})", serviceUid, applicationUidAttribute, newApplicationUid);
                return newApplicationUid;
            }
        } catch (Throwable throwable) {
            logger.error("Failed to insert applicationUid. {}, {}, new:{}", serviceUid, applicationUidAttribute, newApplicationUid, throwable);
        }

        logger.debug("rollback. ({}, {} -> ?)", serviceUid, newApplicationUid);
        try {
            applicationUidAttrDao.deleteApplicationName(serviceUid, newApplicationUid);
        } catch (Throwable throwable) {
            logger.error("Failed to delete applicationName. {}, {}, {}", serviceUid, applicationUidAttribute, newApplicationUid, throwable);
        }
        return null;
    }

    private ApplicationUid insertApplicationNameWithRetries(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            ApplicationUid newApplicationUid = applicationUidGenerator.generate();
            boolean nameInsertResult = applicationUidAttrDao.insertApplicationNameIfNotExists(serviceUid, newApplicationUid, applicationUidAttribute);
            if (nameInsertResult) {
                return newApplicationUid;
            }
        }
        logger.error("ApplicationUid collision. {}, {}, maxRetries: {}", serviceUid, applicationUidAttribute, maxRetries);
        return null;
    }

    @Override
    public List<ApplicationUidAttribute> getApplications(ServiceUid serviceUid) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        return applicationUidDao.selectApplicationInfo(serviceUid);
    }

    @Override
    public ApplicationUidAttribute getApplication(ServiceUid serviceUid, ApplicationUid applicationUid) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationUid, "applicationUid");
        return applicationUidAttrDao.selectApplicationInfo(serviceUid, applicationUid);
    }

    @Override
    public void deleteApplication(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationName, "applicationName");
        ApplicationUidAttribute applicationUidAttribute = new ApplicationUidAttribute(applicationName, serviceTypeCode);

        ApplicationUid applicationUid = applicationUidDao.selectApplicationUid(serviceUid, applicationUidAttribute);
        applicationUidDao.deleteApplicationUid(serviceUid, applicationUidAttribute);
        logger.info("deleted ({}, {} -> {})", serviceUid, applicationUidAttribute, applicationUid);
        if (applicationUid != null) {
            applicationUidAttrDao.deleteApplicationName(serviceUid, applicationUid);
            logger.info("deleted ({}, {} -> {})", serviceUid, applicationUid, applicationUidAttribute);
        }
    }

    // async
    @Override
    public CompletableFuture<ApplicationUid> asyncGetOrCreateApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationName, "applicationName");
        ApplicationUidAttribute applicationUidAttribute = new ApplicationUidAttribute(applicationName, serviceTypeCode);

        return applicationUidDao.asyncSelectApplicationUid(serviceUid, applicationUidAttribute)
                .thenCompose(applicationUid -> {
                    if (applicationUid != null) {
                        return CompletableFuture.completedFuture(applicationUid);
                    }
                    return asyncTryInsertApplicationUid(serviceUid, applicationUidAttribute);
                })
                .thenCompose(newApplicationUid -> {
                    if (newApplicationUid != null) {
                        return CompletableFuture.completedFuture(newApplicationUid);
                    }
                    return applicationUidDao.asyncSelectApplicationUid(serviceUid, applicationUidAttribute);
                }).thenApply(applicationUid -> {
                    if (applicationUid != null) {
                        return applicationUid;
                    }
                    throw new IllegalStateException("Failed to create ApplicationUid. " + serviceUid + ", " + applicationUidAttribute);
                });
    }

    private CompletableFuture<ApplicationUid> asyncTryInsertApplicationUid(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute) {
        return asyncInsertApplicationNameWithRetries(serviceUid, applicationUidAttribute, 3)
                .thenCompose(newApplicationUid -> {
                    if (newApplicationUid == null) {
                        logger.error("ApplicationUid collision. {}, maxRetries: {}", applicationUidAttribute, 3);
                        return CompletableFuture.completedFuture(null);
                    }
                    logger.debug("saved ({}, {} -> {})", serviceUid, newApplicationUid, applicationUidAttribute);
                    return asyncInsertApplicationUidWithRollback(serviceUid, applicationUidAttribute, newApplicationUid);
                });
    }

    private CompletableFuture<ApplicationUid> asyncInsertApplicationUidWithRollback(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute, ApplicationUid newApplicationUid) {
        return applicationUidDao.asyncInsertApplicationUidIfNotExists(serviceUid, applicationUidAttribute, newApplicationUid)
                .exceptionally(throwable -> {
                    logger.error("Failed to insert applicationUid. {}, {}, {}", serviceUid, applicationUidAttribute, newApplicationUid, throwable);
                    return false;
                })
                .thenCompose(success -> {
                    if (!success) {
                        logger.debug("rollback. ({}, {} -> ?)", serviceUid, newApplicationUid);
                        applicationUidAttrDao.asyncDeleteApplicationName(serviceUid, newApplicationUid).whenComplete((result, throwable) -> {
                            if (throwable != null) {
                                logger.error("Failed to delete applicationName. {}, {}", serviceUid, applicationUidAttribute, throwable);
                            }
                        });
                        return CompletableFuture.completedFuture(null);
                    }
                    logger.info("saved ({}, {} -> {})", serviceUid, applicationUidAttribute, newApplicationUid);
                    return CompletableFuture.completedFuture(newApplicationUid);
                });
    }


    private CompletableFuture<ApplicationUid> asyncInsertApplicationNameWithRetries(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute, int maxRetries) {
        CompletableFuture<ApplicationUid> future = CompletableFuture.completedFuture(null);
        for (int i = 0; i < maxRetries; i++) {
            future = future.thenCompose(applicationUid -> {
                if (applicationUid != null) {
                    return CompletableFuture.completedFuture(applicationUid);
                }

                ApplicationUid newApplicationUid = applicationUidGenerator.generate();
                return applicationUidAttrDao.asyncInsertApplicationNameIfNotExists(serviceUid, newApplicationUid, applicationUidAttribute)
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
