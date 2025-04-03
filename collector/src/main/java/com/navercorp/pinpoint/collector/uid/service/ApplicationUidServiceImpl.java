package com.navercorp.pinpoint.collector.uid.service;

import com.navercorp.pinpoint.collector.uid.dao.ApplicationNameDao;
import com.navercorp.pinpoint.collector.uid.dao.ApplicationUidDao;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@ConditionalOnProperty(value = "pinpoint.collector.application.uid.enable", havingValue = "true")
public class ApplicationUidServiceImpl implements ApplicationUidService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationUidDao applicationUidDao;
    private final ApplicationNameDao applicationNameDao;
    private final IdGenerator<ApplicationUid> applicationUidGenerator;

    public ApplicationUidServiceImpl(ApplicationUidDao applicationUidDao, ApplicationNameDao applicationNameDao,
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

        return applicationUidDao.selectApplicationUid(serviceUid, applicationName);
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
}
