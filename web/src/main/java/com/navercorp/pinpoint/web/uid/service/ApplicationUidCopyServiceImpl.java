package com.navercorp.pinpoint.web.uid.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.service.AgentIdService;
import com.navercorp.pinpoint.uid.service.BaseApplicationUidService;
import com.navercorp.pinpoint.uid.vo.ApplicationUidRow;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.Objects;

@Service
@ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "true")
public class ApplicationUidCopyServiceImpl implements ApplicationUidCopyService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationIndexDao applicationIndexDao;
    private final BaseApplicationUidService baseApplicationUidService;
    private final AgentIdService agentIdService;

    public ApplicationUidCopyServiceImpl(ApplicationIndexDao applicationIndexDao, BaseApplicationUidService baseApplicationUidService, AgentIdService agentIdService) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.baseApplicationUidService = Objects.requireNonNull(baseApplicationUidService, "baseApplicationUidService");
        this.agentIdService = Objects.requireNonNull(agentIdService, "agentIdService");
    }

    @Override
    public void copyApplication() {
        StopWatch stopWatch = new StopWatch("copyApplicationName and create uid");
        stopWatch.start("selectAllApplicationNames");
        List<Application> applications = this.applicationIndexDao.selectAllApplicationNames();
        stopWatch.stop();

        List<ApplicationUidRow> beforeInsert = List.of();
        if (logger.isInfoEnabled()) {
            beforeInsert = baseApplicationUidService.getApplications(ServiceUid.DEFAULT);
        }

        stopWatch.start("getOrCreateApplicationUid");
        for (Application application : applications) {
            baseApplicationUidService.getOrCreateApplicationUid(ServiceUid.DEFAULT, application.getName(), application.getServiceTypeCode());
        }
        stopWatch.stop();

        if (logger.isDebugEnabled()) {
            stopWatch.start("baseApplicationUidService.getApplications");
            List<ApplicationUidRow> afterInsert = baseApplicationUidService.getApplications(ServiceUid.DEFAULT);
            stopWatch.stop();
            logger.debug("syncApplicationUid total:{}, time taken: {} ms, before:{} after: {}", applications.size(), stopWatch.getTotalTimeMillis(), beforeInsert.size(), afterInsert.size());
        }
        logger.info(stopWatch.prettyPrint());
    }

    @Override
    public void copyAgentId() {
        StopWatch stopWatch = new StopWatch("copyAgentId");
        stopWatch.start("uidApplicationNames");
        List<ApplicationUidRow> ApplicationNameList = baseApplicationUidService.getApplications(ServiceUid.DEFAULT);
        stopWatch.stop();
        stopWatch.start("insertEach");
        for (ApplicationUidRow row : ApplicationNameList) {
            List<String> agentIds = applicationIndexDao.selectAgentIds(row.applicationName());
            for (String agentId : agentIds) {
                agentIdService.insert(row.serviceUid(), row.applicationUid(), agentId);
            }
        }
        stopWatch.stop();
        logger.info(stopWatch.prettyPrint());
    }
}
