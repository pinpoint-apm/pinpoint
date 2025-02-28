package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.vo.ApplicationIdentifier;
import com.navercorp.pinpoint.common.server.vo.ApplicationUid;
import com.navercorp.pinpoint.common.server.vo.ServiceUid;
import com.navercorp.pinpoint.web.config.WebApplicationIdCacheConfig;
import com.navercorp.pinpoint.web.dao.ApplicationNameDao;
import com.navercorp.pinpoint.web.dao.ApplicationUidDao;
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
    public List<ApplicationIdentifier> getApplicationIds(String applicationName) {
        List<ApplicationIdentifier> applicationIdentifiers = applicationUidDao.selectApplicationIds(applicationName);
        return applicationIdentifiers.stream()
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<String> getApplicationNames(ServiceUid serviceUid) {
        List<String> names = applicationNameDao.selectApplicationNames(serviceUid);
        return names.stream()
                .filter(Objects::nonNull)
                .sorted()
                .toList();
    }

    @Override
    @Cacheable(cacheNames = "applicationUidCache", key = "{#serviceUid, #applicationName}", cacheManager = WebApplicationIdCacheConfig.APPLICATION_UID_CACHE_NAME, unless = "#result == null")
    public ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName) {
        return applicationUidDao.selectApplicationId(serviceUid, applicationName);
    }

    @Override
    @Cacheable(cacheNames = "applicationNameCache", key = "{#serviceUid, #applicationUid}", cacheManager = WebApplicationIdCacheConfig.APPLICATION_NAME_CACHE_NAME, unless = "#result == null")
    public String getApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid) {
        if (applicationUid == null) {
            return null;
        }
        return applicationNameDao.selectApplicationName(serviceUid, applicationUid);
    }

    @Override
    @CacheEvict(cacheNames = "applicationUidCache", key = "{#serviceUid, #applicationName}", cacheManager = WebApplicationIdCacheConfig.APPLICATION_UID_CACHE_NAME)
    public void deleteApplication(ServiceUid serviceUid, String applicationName) {
        ApplicationUid applicationUid = getApplicationUid(serviceUid, applicationName);
        applicationUidDao.deleteApplicationId(serviceUid, applicationName);
        logger.info("deleted (serviceUid:{}, name:{} -> id:{})", serviceUid, applicationName, applicationUid);
        if (applicationUid != null) {
            applicationNameDao.deleteApplicationName(serviceUid, applicationUid);
            logger.info("deleted (id:{} -> name:{})", applicationUid, applicationName);
        }
    }
}
