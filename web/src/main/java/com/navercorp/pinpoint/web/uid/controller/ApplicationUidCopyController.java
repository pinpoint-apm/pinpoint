package com.navercorp.pinpoint.web.uid.controller;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.dao.AgentIdDao;
import com.navercorp.pinpoint.uid.service.BaseApplicationUidService;
import com.navercorp.pinpoint.uid.vo.ApplicationUidRow;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin/uid/copy")
@ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "true")
public class ApplicationUidCopyController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationIndexDao applicationIndexDao;
    private final BaseApplicationUidService baseApplicationUidService;
    private final AgentIdDao agentIdDao;

    public ApplicationUidCopyController(ApplicationIndexDao applicationIndexDao,
                                        BaseApplicationUidService baseApplicationUidService, AgentIdDao agentIdDao) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.baseApplicationUidService = Objects.requireNonNull(baseApplicationUidService, "baseApplicationUidService");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
    }

    @GetMapping(value = "application")
    public ResponseEntity<String> copyApplicationList() {
        StopWatch stopWatch = new StopWatch("copyApplicationUid");
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

        if (logger.isInfoEnabled()) {
            stopWatch.start("!Debug baseApplicationUidService.getApplications");
            List<ApplicationUidRow> afterInsert = baseApplicationUidService.getApplications(ServiceUid.DEFAULT);
            stopWatch.stop();
            logger.info("syncApplicationUid total:{}, time taken: {} ms, before:{} after: {}", applications.size(), stopWatch.getTotalTimeMillis(), beforeInsert.size(), afterInsert.size());
        }
        logger.info(stopWatch.prettyPrint());
        return ResponseEntity.ok("OK");
    }

    @GetMapping(value = "agent")
    public ResponseEntity<String> copyAgentList() {
        StopWatch stopWatch = new StopWatch("copyAgentList");
        stopWatch.start("uidApplicationNames");
        List<ApplicationUidRow> ApplicationNameList = baseApplicationUidService.getApplications(ServiceUid.DEFAULT);
        stopWatch.stop();

        stopWatch.start("insertEach");
        for (ApplicationUidRow row : ApplicationNameList) {
            List<String> agentIds = applicationIndexDao.selectAgentIds(row.applicationName());
            for (String agentId : agentIds) {
                agentIdDao.insert(row.serviceUid(), row.applicationUid(), agentId);
            }
        }
        stopWatch.stop();
        logger.info(stopWatch.prettyPrint());
        return ResponseEntity.ok("OK");
    }
}
