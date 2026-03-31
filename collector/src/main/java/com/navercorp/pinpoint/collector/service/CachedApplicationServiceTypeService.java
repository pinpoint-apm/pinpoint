package com.navercorp.pinpoint.collector.service;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.navercorp.pinpoint.collector.dao.ApplicationDao;
import com.navercorp.pinpoint.common.server.cache.NullValueExpiry;
import com.navercorp.pinpoint.common.server.config.AgentProperties;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


/**
 * Resolves serviceType for agents with missing serviceType header (-1).
 * Only handles default serviceUid (0).
 */
@Service
public class CachedApplicationServiceTypeService {

    private static final int NOT_FOUND = ServiceType.UNDEFINED.getCode();
    private static final Duration NOT_FOUND_EXPIRY = Duration.ofMinutes(1); // 1 min

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AsyncCache<String, Integer> serviceTypeCache;
    private final ApplicationDao applicationDao;
    private final Set<Integer> missingHeaderServiceTypeCodes;
    private final long timeoutMillis;

    public CachedApplicationServiceTypeService(ApplicationDao applicationDao,
                                               AgentProperties agentProperties,
                                               @Value("${collector.service-type.cache.timeout-millis:1000}") long timeoutMillis,
                                               @Value("${collector.service-type.cache.maximum-size:4096}") long maximumSize,
                                               @Value("${collector.service-type.cache.expire-after-write-minutes:20}") long expireAfterWriteMinutes) {
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao");
        this.missingHeaderServiceTypeCodes = agentProperties.getMissingHeaderServiceTypeCodes();
        this.timeoutMillis = timeoutMillis;
        this.serviceTypeCache = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfter(new NullValueExpiry<String, Integer>(Duration.ofMinutes(expireAfterWriteMinutes), null, NOT_FOUND_EXPIRY,
                        value -> value == NOT_FOUND))
                .buildAsync();
    }

    public void put(String applicationName, int serviceTypeCode) {
        serviceTypeCache.put(applicationName, CompletableFuture.completedFuture(serviceTypeCode));
    }

    public int getServiceTypeCode(String applicationName) {
        try {
            CompletableFuture<Integer> future = serviceTypeCache.get(applicationName, this::readFromDao);
            Integer result = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
            return result != null ? result : NOT_FOUND;
        } catch (Exception e) {
            logger.warn("Failed to get serviceTypeCode for applicationName={}", applicationName, e);
            return NOT_FOUND;
        }
    }

    private int readFromDao(String applicationName) {
        try {
            logger.debug("Reading serviceTypeCode from DAO for applicationName={}", applicationName);
            List<Integer> serviceTypeCodes = applicationDao.selectServiceTypeCodes(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName);
            logger.debug("DAO returned serviceTypeCodes={} for applicationName={}", serviceTypeCodes, applicationName);
            return selectServiceTypeCode(serviceTypeCodes);
        } catch (Exception e) {
            logger.warn("Failed to read serviceTypeCode for applicationName={}", applicationName, e);
            return NOT_FOUND;
        }
    }

    private int selectServiceTypeCode(List<Integer> serviceTypeCodes) {
        if (serviceTypeCodes.isEmpty()) {
            return NOT_FOUND;
        }
        if (serviceTypeCodes.size() == 1) {
            return serviceTypeCodes.get(0);
        }
        // Multiple serviceTypes: prefer missingHeaderServiceTypeCodes
        for (int serviceTypeCode : serviceTypeCodes) {
            if (missingHeaderServiceTypeCodes.contains(serviceTypeCode)) {
                return serviceTypeCode;
            }
        }
        return serviceTypeCodes.get(0);
    }
}