package com.navercorp.pinpoint.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.dao.ApplicationUidAttrDao;
import com.navercorp.pinpoint.uid.dao.ApplicationUidDao;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttrRow;
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

    private final ApplicationUidDao applicationUidDao;
    private final ApplicationUidAttrDao applicationUidAttrDao;

    public ApplicationCleanupServiceImpl(ApplicationUidDao applicationUidDao, ApplicationUidAttrDao applicationUidAttrDao) {
        this.applicationUidDao = Objects.requireNonNull(applicationUidDao, "applicationUidDao");
        this.applicationUidAttrDao = Objects.requireNonNull(applicationUidAttrDao, "applicationInfoDao");
    }

    @Override
    public int cleanupInconsistentApplicationUid(@Nullable ServiceUid serviceUid) {
        long bufferTime = System.currentTimeMillis() - 300_000L; // 5 minute buffer time
        List<ApplicationUidAttrRow> rowDataList = applicationUidAttrDao.scanApplicationAttrRow(serviceUid);
        logger.info("cleanup start. InconsistentApplicationUid, {}", serviceUid);
        int counter = 0;
        int cleanupCount = 0;
        for (ApplicationUidAttrRow rowData : rowDataList) {
            counter++;
            if (rowData.timeStamp() > bufferTime) {
                continue;
            }
            ServiceUid rowServiceUid = rowData.serviceUid();
            ApplicationUid rowApplicationUid = rowData.applicationUid();
            ApplicationUidAttribute rowApplicationAttr = rowData.applicationUidAttribute();

            logIteration(counter, rowDataList.size(), rowServiceUid, rowApplicationAttr, rowApplicationUid);
            boolean deleted = deleteApplicationNameIfInconsistent(rowServiceUid, rowApplicationUid, rowApplicationAttr);
            if (deleted) {
                cleanupCount++;
            }
        }
        logger.info("cleanup end. InconsistentApplicationUid, {}", serviceUid);
        return cleanupCount;
    }

    private boolean deleteApplicationNameIfInconsistent(ServiceUid serviceUid, ApplicationUid applicationUid, ApplicationUidAttribute applicationUidAttribute) {
        ApplicationUid actualApplicationUid = applicationUidDao.getApplicationUid(serviceUid, applicationUidAttribute);
        if (applicationUid.equals(actualApplicationUid)) {
            return false;
        }

        try {
            applicationUidAttrDao.deleteApplicationAttr(serviceUid, applicationUid);
            logger.info("deleted {}, {}", serviceUid, applicationUid);
            return true;
        } catch (Exception e) {
            logger.warn("Failed to delete inconsistent applicationUid. {}, {}", serviceUid, applicationUid, e);
            return false;
        }
    }

    private void logIteration(int iteration, int total, ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute, ApplicationUid applicationUid) {
        int logInterval = Math.max(total / 10, 1);
        String logString = String.format("Iteration %d/%d: %s, %s#%s, %s",
                iteration, total, serviceUid,
                applicationUidAttribute.applicationName(), applicationUidAttribute.serviceTypeCode(),
                applicationUid);
        if (logger.isDebugEnabled()) {
            logger.debug(logString);
        } else if (logger.isInfoEnabled() && iteration % logInterval == 0) {
            logger.info(logString);
        }
    }
}