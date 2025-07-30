package com.navercorp.pinpoint.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.HbaseCellData;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.dao.ApplicationUidDao;
import com.navercorp.pinpoint.uid.dao.ApplicationUidAttrDao;
import com.navercorp.pinpoint.uid.utils.UidBytesParseUtils;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ApplicationCleanupServiceImpl implements ApplicationCleanupService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentNameService agentNameService;
    private final BaseApplicationUidService baseApplicationUidService;

    private final ApplicationUidDao applicationUidDao;
    private final ApplicationUidAttrDao applicationUidAttrDao;

    public ApplicationCleanupServiceImpl(AgentNameService agentNameService, BaseApplicationUidService baseApplicationUidService,
                                         ApplicationUidDao applicationUidDao, ApplicationUidAttrDao applicationUidAttrDao) {
        this.agentNameService = Objects.requireNonNull(agentNameService, "agentNameService");
        this.baseApplicationUidService = Objects.requireNonNull(baseApplicationUidService, "applicationUidService");
        this.applicationUidDao = Objects.requireNonNull(applicationUidDao, "applicationUidDao");
        this.applicationUidAttrDao = Objects.requireNonNull(applicationUidAttrDao, "applicationInfoDao");
    }

    @Override
    public int cleanupEmptyApplication(@Nullable ServiceUid serviceUid, long fromTimestamp) {
        List<HbaseCellData> cellData = applicationUidDao.selectCellData(serviceUid).stream()
                .filter(hbaseCellData -> hbaseCellData.getTimestamp() < fromTimestamp)
                .toList();

        logger.info("cleanup start. EmptyApplication, {}", serviceUid);
        int cleanupCount = 0;
        for (int i = 0; i < cellData.size(); i++) {
            ServiceUid cellServiceUid = UidBytesParseUtils.parseServiceUidFromRowKey(cellData.get(i).getRowKey());
            ApplicationUidAttribute cellApplicationUidAttribute = UidBytesParseUtils.parseApplicationUidAttrFromRowKey(cellData.get(i).getRowKey());
            ApplicationUid cellApplicationUid = (ApplicationUid) cellData.get(i).getValue();

            logIteration(i, cellData.size(), cellServiceUid, cellApplicationUidAttribute, cellApplicationUid);
            boolean deleted = deleteApplicationIfEmpty(cellServiceUid, cellApplicationUid, cellApplicationUidAttribute);
            if (deleted) {
                cleanupCount++;
            }
        }
        logger.info("cleanup end. EmptyApplication, {}", serviceUid);
        return cleanupCount;
    }

    private boolean deleteApplicationIfEmpty(ServiceUid serviceUid, ApplicationUid applicationUid, ApplicationUidAttribute applicationUidAttribute) {
        if (agentNameService.getAgentIdentifier(serviceUid, applicationUid).size() > 0) {
            return false;
        }

        try {
            baseApplicationUidService.deleteApplication(serviceUid, applicationUidAttribute.applicationName(), applicationUidAttribute.serviceTypeCode());
            return true;
        } catch (Exception e) {
            logger.warn("Failed to delete application. {}, {}, {}", serviceUid, applicationUid, applicationUidAttribute, e);
            return false;
        }
    }

    @Override
    public int cleanupInconsistentApplicationUid(@Nullable ServiceUid serviceUid) {
        long bufferTime = System.currentTimeMillis() - 300_000L; // 5 minute buffer time
        List<HbaseCellData> cellData = applicationUidAttrDao.selectCellData(serviceUid).stream()
                .filter(hbaseCellData -> hbaseCellData.getTimestamp() < bufferTime)
                .toList();

        logger.info("cleanup start. InconsistentApplicationUid, {}", serviceUid);
        int cleanupCount = 0;
        for (int i = 0; i < cellData.size(); i++) {
            ServiceUid cellServiceUid = UidBytesParseUtils.parseServiceUidFromRowKey(cellData.get(i).getRowKey());
            ApplicationUid cellApplicationUid = UidBytesParseUtils.parseApplicationUidFromRowKey(cellData.get(i).getRowKey());
            ApplicationUidAttribute cellApplicationUidAttribute = (ApplicationUidAttribute) cellData.get(i).getValue();

            logIteration(i, cellData.size(), cellServiceUid, cellApplicationUidAttribute, cellApplicationUid);
            boolean deleted = deleteApplicationNameIfInconsistent(cellServiceUid, cellApplicationUid, cellApplicationUidAttribute);
            if (deleted) {
                cleanupCount++;
            }
        }
        logger.info("cleanup end. InconsistentApplicationUid, {}", serviceUid);
        return cleanupCount;
    }

    private boolean deleteApplicationNameIfInconsistent(ServiceUid serviceUid, ApplicationUid applicationUid, ApplicationUidAttribute applicationUidAttribute) {
        ApplicationUid actualApplicationUid = applicationUidDao.selectApplicationUid(serviceUid, applicationUidAttribute);
        if (applicationUid.equals(actualApplicationUid)) {
            return false;
        }

        try {
            applicationUidAttrDao.deleteApplicationName(serviceUid, applicationUid);
            logger.info("deleted {}, {}", serviceUid, applicationUid);
            return true;
        } catch (Exception e) {
            logger.warn("Failed to delete inconsistent applicationName. {}, {}", serviceUid, applicationUid, e);
            return false;
        }
    }

    private void logIteration(int index, int total, ServiceUid cellServiceUid, ApplicationUidAttribute cellApplicationUidAttribute, ApplicationUid cellApplicationUid) {
        int logInterval = Math.max(total / 10, 1);
        String logString = String.format("Iteration %d/%d: %s, name=%s@%s, %s",
                index + 1, total, cellServiceUid,
                cellApplicationUidAttribute.applicationName(), cellApplicationUidAttribute.serviceTypeCode(),
                cellApplicationUid);
        if (logger.isDebugEnabled()) {
            logger.debug(logString);
        } else if (logger.isInfoEnabled() && index % logInterval == 0) {
            logger.info(logString);
        }
    }
}