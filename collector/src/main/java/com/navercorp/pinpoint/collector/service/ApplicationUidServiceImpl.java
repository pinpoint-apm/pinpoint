package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.dao.ApplicationUidDao;
import com.navercorp.pinpoint.collector.dao.ApplicationNameDao;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import com.navercorp.pinpoint.common.server.vo.ApplicationUid;
import com.navercorp.pinpoint.common.server.vo.ServiceUid;
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
    private final IdGenerator<ApplicationUid> applicationIdGenerator;

    public ApplicationUidServiceImpl(ApplicationUidDao applicationUidDao, ApplicationNameDao applicationNameDao,
                                     @Qualifier("applicationIdGenerator") IdGenerator<ApplicationUid> applicationIdGenerator) {
        this.applicationUidDao = Objects.requireNonNull(applicationUidDao, "applicationIdDao");
        this.applicationNameDao = Objects.requireNonNull(applicationNameDao, "applicationInfoDao");
        this.applicationIdGenerator = Objects.requireNonNull(applicationIdGenerator, "applicationIdGenerator");
    }

    @Override
    public ApplicationUid getApplicationId(ServiceUid serviceUid, String applicationName) {
        if (applicationName == null) {
            return null;
        }
        return applicationUidDao.selectApplicationUid(serviceUid, applicationName);
    }

    @Override
    public ApplicationUid getOrCreateApplicationId(ServiceUid serviceUid, String applicationName) {
        Objects.requireNonNull(applicationName, "applicationName");
        ApplicationUid applicationUid = getApplicationId(serviceUid, applicationName);
        if (applicationUid != null) {
            return applicationUid;
        }

        ApplicationUid newApplicationUid = tryInsertApplicationId(serviceUid, applicationName);
        if (newApplicationUid != null) {
            return newApplicationUid;
        }

        ApplicationUid updatedApplicationUid = getApplicationId(serviceUid, applicationName);
        return updatedApplicationUid;
    }

    private ApplicationUid tryInsertApplicationId(ServiceUid serviceUid, String applicationName) {
        // 1. insert (id -> name)
        ApplicationUid newApplicationUid = InsertApplicationNameWithRetries(serviceUid, applicationName, 3);
        if (newApplicationUid != null) {
            logger.debug("saved (id:{} -> name:{})", newApplicationUid, applicationName);
        } else {
            return null;
        }

        // 2. insert (name -> id)
        try {
            boolean idInsertResult = applicationUidDao.insertApplicationUidIfNotExists(serviceUid, applicationName, newApplicationUid);
            if (idInsertResult) {
                logger.info("saved ({}, name:{} -> id:{})", serviceUid, applicationName, newApplicationUid);
                return newApplicationUid;
            } else {
                logger.debug("Failed to save. already existing serviceName (name:{} -> id:{})", applicationName, newApplicationUid);
                applicationNameDao.deleteApplicationName(serviceUid, newApplicationUid);
            }
        } catch (Exception e) {
            logger.warn("Failed to save ({}, name:{} -> id:{})", serviceUid, applicationName, newApplicationUid, e);
            applicationNameDao.deleteApplicationName(serviceUid, newApplicationUid);
        }
        return null;
    }

    private ApplicationUid InsertApplicationNameWithRetries(ServiceUid serviceUid, String applicationName, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            ApplicationUid newApplicationUid = applicationIdGenerator.generate();
            boolean nameInsertResult = applicationNameDao.insertApplicationNameIfNotExists(serviceUid, newApplicationUid, applicationName);
            if (nameInsertResult) {
                return newApplicationUid;
            }
        }
        logger.error("ApplicationId collision occurred. applicationName: {}, maxRetries: {}", applicationName, maxRetries);
        return null;
    }
}
