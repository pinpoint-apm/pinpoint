package com.navercorp.pinpoint.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.HbaseCellData;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.dao.ApplicationNameDao;
import com.navercorp.pinpoint.uid.dao.ApplicationUidDao;
import com.navercorp.pinpoint.uid.utils.UidRowKeyParseUtils;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ApplicationCleanupServiceImpl implements ApplicationCleanupService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentNameService agentNameService;
    private final BaseApplicationUidService baseApplicationUidService;

    private final ApplicationUidDao applicationUidDao;
    private final ApplicationNameDao applicationNameDao;

    public ApplicationCleanupServiceImpl(AgentNameService agentNameService, BaseApplicationUidService baseApplicationUidService,
                                         ApplicationUidDao applicationUidDao, ApplicationNameDao applicationNameDao) {
        this.agentNameService = Objects.requireNonNull(agentNameService, "agentNameService");
        this.baseApplicationUidService = Objects.requireNonNull(baseApplicationUidService, "applicationUidService");
        this.applicationUidDao = Objects.requireNonNull(applicationUidDao, "applicationUidDao");
        this.applicationNameDao = Objects.requireNonNull(applicationNameDao, "applicationInfoDao");
    }

    @Override
    public int cleanupEmptyApplication(@Nullable ServiceUid serviceUid, long fromTimestamp) {
        List<HbaseCellData> cellData = applicationUidDao.selectCellData(serviceUid).stream()
                .filter(hbaseCellData -> hbaseCellData.getTimestamp() < fromTimestamp)
                .toList();

        logger.info("cleanup start. EmptyApplication, {}", serviceUid);
        int cleanupCount = 0;
        for (int i = 0; i < cellData.size(); i++) {
            ServiceUid cellServiceUid = UidRowKeyParseUtils.getServiceUid(cellData.get(i).getRowKey());
            String cellApplicationName = UidRowKeyParseUtils.getApplicationName(cellData.get(i).getRowKey());
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
            baseApplicationUidService.deleteApplication(serviceUid, applicationName);
            return true;
        } catch (Exception e) {
            logger.warn("Failed to delete application. {}, {}, {}", serviceUid, applicationUid, applicationName, e);
            return false;
        }
    }

    @Override
    public int cleanupInconsistentApplicationUid(@Nullable ServiceUid serviceUid) {
        long bufferTime = System.currentTimeMillis() - 300_000L; // 5 minute buffer time
        List<HbaseCellData> cellData = applicationNameDao.selectCellData(serviceUid).stream()
                .filter(hbaseCellData -> hbaseCellData.getTimestamp() < bufferTime)
                .collect(Collectors.toList());

        logger.info("cleanup start. InconsistentApplicationUid, {}", serviceUid);
        int cleanupCount = 0;
        for (int i = 0; i < cellData.size(); i++) {
            ServiceUid cellServiceUid = UidRowKeyParseUtils.getServiceUid(cellData.get(i).getRowKey());
            ApplicationUid cellApplicationUid = UidRowKeyParseUtils.getApplicationUid(cellData.get(i).getRowKey());
            String cellApplicationName = (String) cellData.get(i).getValue();

            logIteration(i, cellData.size(), cellServiceUid, cellApplicationName, cellApplicationUid);
            boolean deleted = deleteApplicationNameIfInconsistent(cellServiceUid, cellApplicationUid, cellApplicationName);
            if (deleted) {
                cleanupCount++;
            }
        }
        logger.info("cleanup end. InconsistentApplicationUid, {}", serviceUid);
        return cleanupCount;
    }

    private boolean deleteApplicationNameIfInconsistent(ServiceUid serviceUid, ApplicationUid applicationUid, String applicationName) {
        ApplicationUid actualApplicationUid = applicationUidDao.selectApplicationUid(serviceUid, applicationName);
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