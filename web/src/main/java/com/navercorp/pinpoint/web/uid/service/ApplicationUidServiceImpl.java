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
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "pinpoint.web.application.uid.enable", havingValue = "true")
public class ApplicationUidServiceImpl implements ApplicationUidService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationUidDao applicationUidDao;
    private final ApplicationNameDao applicationNameDao;

    public ApplicationUidServiceImpl(ApplicationUidDao applicationUidDao, ApplicationNameDao applicationNameDao) {
        this.applicationUidDao = Objects.requireNonNull(applicationUidDao, "applicationIdDao");
        this.applicationNameDao = Objects.requireNonNull(applicationNameDao, "applicationInfoDao");
    }

    @Override
    public List<String> getApplicationNames(ServiceUid serviceUid) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        return applicationUidDao.selectApplicationUidRows(serviceUid);
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
        logger.info("deleted ({}, name:{} -> id:{})", serviceUid, applicationName, applicationUid);
        if (applicationUid != null) {
            applicationNameDao.deleteApplicationName(serviceUid, applicationUid);
            logger.info("deleted (id:{} -> name:{})", applicationUid, applicationName);
        }
    }

    @Override
    public int cleanupInconsistentApplicationName(@Nullable ServiceUid serviceUid) {
        int cleanupCount = 0;
        long timestampThreshold = System.currentTimeMillis() - 60_000L; // 1 minute
        List<HbaseCellData> applicationNameCellDataList = applicationNameDao.selectCellData(serviceUid)
                .stream()
                .filter(hbaseCellData -> hbaseCellData.getTimestamp() < timestampThreshold)
                .collect(Collectors.toList());
//        Collections.shuffle(applicationNameCellDataList);

        for (int i = 0; i < applicationNameCellDataList.size(); i++) {
            HbaseCellData hbaseCellData = applicationNameCellDataList.get(i);
            ServiceUid cellServiceUid = ApplicationUidRowKeyUtils.getServiceUid(hbaseCellData.getRowKey());
            ApplicationUid cellApplicationUid = ApplicationUidRowKeyUtils.getApplicationUidLong(hbaseCellData.getRowKey());
            String cellApplicationName = (String) hbaseCellData.getValue();

            logCleanupProgress(i, applicationNameCellDataList.size(), cellServiceUid, cellApplicationUid, cellApplicationName);
            boolean deleted = deleteApplicationNameIfInconsistent(cellServiceUid, cellApplicationUid, cellApplicationName);
            if (deleted) {
                logger.info("deleted {}, {}", cellServiceUid, cellApplicationUid);
                cleanupCount++;
            }
        }

        return cleanupCount;
    }

    private void logCleanupProgress(int index, int total, ServiceUid cellServiceUid, ApplicationUid cellApplicationUid, String cellApplicationName) {
        String logMessage = String.format("cleanup %4d/%d %s, %s, name=%s", index + 1, total,
                cellServiceUid, cellApplicationUid, cellApplicationName);
        if (index % Math.max(total / 10, 1) == 0) {
            logger.info(logMessage);
        } else {
            logger.debug(logMessage);
        }
    }


    private boolean deleteApplicationNameIfInconsistent(ServiceUid serviceUid, ApplicationUid applicationUid, String applicationName) {
        if (applicationUid.equals(applicationUidDao.selectApplication(serviceUid, applicationName))) {
            return false;
        }

        try {
            applicationNameDao.deleteApplicationName(serviceUid, applicationUid);
            return true;
        } catch (Exception e) {
            logger.warn("Failed to delete inconsistent applicationName. {}, {}", serviceUid, applicationUid, e);
            return false;
        }
    }

}