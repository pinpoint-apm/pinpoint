package com.navercorp.pinpoint.web.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.HbaseCellData;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.ApplicationUidRowKeyUtils;
import com.navercorp.pinpoint.web.uid.config.ApplicationUidConfig;
import com.navercorp.pinpoint.web.uid.dao.ApplicationNameDao;
import com.navercorp.pinpoint.web.uid.dao.ApplicationUidDao;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@ConditionalOnProperty(name = "pinpoint.web.application.uid.enable", havingValue = "true")
public class ApplicationUidServiceImpl implements ApplicationUidService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentNameService agentNameService;

    private final ApplicationUidDao applicationUidDao;
    private final ApplicationNameDao applicationNameDao;

    public ApplicationUidServiceImpl(AgentNameService agentNameService, ApplicationUidDao applicationUidDao, ApplicationNameDao applicationNameDao) {
        this.agentNameService = agentNameService;
        this.applicationUidDao = Objects.requireNonNull(applicationUidDao, "applicationIdDao");
        this.applicationNameDao = Objects.requireNonNull(applicationNameDao, "applicationInfoDao");
    }

    @Override
    public List<String> getApplicationNames(ServiceUid serviceUid) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        return applicationUidDao.selectApplicationNames(serviceUid);
    }

    @Override
    @Cacheable(cacheNames = "applicationUidCache", key = "{#serviceUid, #applicationName}", cacheManager = ApplicationUidConfig.APPLICATION_UID_CACHE_NAME, unless = "#result == null")
    public ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationName, "applicationName");
        return applicationUidDao.selectApplication(serviceUid, applicationName);
    }

    @Override
    @Cacheable(cacheNames = "applicationNameCache", key = "{#serviceUid, #applicationUid}", cacheManager = ApplicationUidConfig.APPLICATION_NAME_CACHE_NAME, unless = "#result == null")
    public String getApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationUid, "applicationUid");
        return applicationNameDao.selectApplicationName(serviceUid, applicationUid);
    }

    @Override
    @CacheEvict(cacheNames = "applicationUidCache", key = "{#serviceUid, #applicationName}", cacheManager = ApplicationUidConfig.APPLICATION_UID_CACHE_NAME)
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

    @Override
    public int cleanupEmptyApplication(@Nullable ServiceUid serviceUid, long fromTimestamp) {
        List<HbaseCellData> cellData = applicationUidDao.selectCellData(serviceUid).stream()
                .filter(hbaseCellData -> hbaseCellData.getTimestamp() < fromTimestamp)
                .toList();

        logger.info("cleanup start. EmptyApplication, {}", serviceUid);
        int cleanupCount = 0;
        for (int i = 0; i < cellData.size(); i++) {
            ServiceUid cellServiceUid = ApplicationUidRowKeyUtils.getServiceUid(cellData.get(i).getRowKey());
            String cellApplicationName = ApplicationUidRowKeyUtils.getApplicationName(cellData.get(i).getRowKey());
            ApplicationUid cellApplicationUid = (ApplicationUid) cellData.get(i).getValue();

            logIteration(i, cellData.size(), cellServiceUid, cellApplicationName, cellApplicationUid);
            boolean deleted = deleteApplicationIfEmpty(cellServiceUid, cellApplicationUid, cellApplicationName);
            if (deleted) {
                cleanupCount++;
            }
        }
        logger.info("cleanup end. EmptyApplication, {}", serviceUid);
        return cleanupCount;
    }

    private boolean deleteApplicationIfEmpty(ServiceUid serviceUid, ApplicationUid applicationUid, String applicationName) {
        if (agentNameService.getAgentIdentifier(serviceUid, applicationUid).size() > 0) {
            return false;
        }

        try {
            this.deleteApplication(serviceUid, applicationName);
            return true;
        } catch (Exception e) {
            logger.warn("Failed to delete application. {}, {}, {}", serviceUid, applicationUid, applicationName, e);
            return false;
        }
    }

    @Override
    public int cleanupInconsistentApplicationName(@Nullable ServiceUid serviceUid) {
        long bufferTime = System.currentTimeMillis() - 300_000L; // 5 minute buffer time
        List<HbaseCellData> cellData = applicationNameDao.selectCellData(serviceUid).stream()
                .filter(hbaseCellData -> hbaseCellData.getTimestamp() < bufferTime)
                .toList();

        logger.info("cleanup start. InconsistentApplicationName, {}", serviceUid);
        int cleanupCount = 0;
        for (int i = 0; i < cellData.size(); i++) {
            ServiceUid cellServiceUid = ApplicationUidRowKeyUtils.getServiceUid(cellData.get(i).getRowKey());
            ApplicationUid cellApplicationUid = ApplicationUidRowKeyUtils.getApplicationUidLong(cellData.get(i).getRowKey());
            String cellApplicationName = (String) cellData.get(i).getValue();

            logIteration(i, cellData.size(), cellServiceUid, cellApplicationName, cellApplicationUid);
            boolean deleted = deleteApplicationNameIfInconsistent(cellServiceUid, cellApplicationUid, cellApplicationName);
            if (deleted) {
                cleanupCount++;
            }
        }
        logger.info("cleanup end. InconsistentApplicationName, {}", serviceUid);
        return cleanupCount;
    }

    private boolean deleteApplicationNameIfInconsistent(ServiceUid serviceUid, ApplicationUid applicationUid, String applicationName) {
        ApplicationUid actualApplicationUid = applicationUidDao.selectApplication(serviceUid, applicationName);
        if (applicationUid.equals(actualApplicationUid)) {
            return false;
        }

        try {
            applicationNameDao.deleteApplicationName(serviceUid, applicationUid);
            logger.info("deleted {}, {}", serviceUid, applicationUid);
            return true;
        } catch (Exception e) {
            logger.warn("Failed to delete inconsistent applicationName. {}, {}", serviceUid, applicationUid, e);
            return false;
        }
    }


    private void logIteration(int index, int total, ServiceUid cellServiceUid, String cellApplicationName, ApplicationUid cellApplicationUid) {
        int logInterval = Math.max(total / 10, 1);
        if (logger.isDebugEnabled()) {
            logger.debug("Iteration {}/{}: {}, name={}, {}", index + 1, total, cellServiceUid, cellApplicationName, cellApplicationUid);
        } else if (logger.isInfoEnabled() && index % logInterval == 0) {
            logger.info("Iteration {}/{}: {}, name={}, {}", index + 1, total, cellServiceUid, cellApplicationName, cellApplicationUid);
        }
    }
}