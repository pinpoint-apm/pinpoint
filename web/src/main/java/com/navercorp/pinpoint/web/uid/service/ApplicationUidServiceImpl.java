package com.navercorp.pinpoint.web.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.uid.config.ApplicationUidConfig;
import com.navercorp.pinpoint.web.uid.dao.ApplicationNameDao;
import com.navercorp.pinpoint.web.uid.dao.ApplicationUidDao;
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
        logger.info("deleted (serviceUid:{}, name:{} -> id:{})", serviceUid, applicationName, applicationUid);
        if (applicationUid != null) {
            applicationNameDao.deleteApplicationName(serviceUid, applicationUid);
            logger.info("deleted (id:{} -> name:{})", applicationUid, applicationName);
        }
    }
}
