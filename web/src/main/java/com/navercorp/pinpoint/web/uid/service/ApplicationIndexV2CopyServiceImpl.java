package com.navercorp.pinpoint.web.uid.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.dao.ApplicationDao;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.Objects;

@Service
public class ApplicationIndexV2CopyServiceImpl implements ApplicationIndexV2CopyService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationIndexDao applicationIndexDao;

    private final ApplicationDao applicationDao;
    private final AgentIdDao agentIdDao;

    public ApplicationIndexV2CopyServiceImpl(ApplicationIndexDao applicationIndexDao,
                                             ApplicationDao applicationDao,
                                             AgentIdDao agentIdDao) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
    }

    @Override
    public void copyApplication() {
        StopWatch stopWatch = new StopWatch("copyApplicationName");
        stopWatch.start("Select all applicationNames from v1");
        List<Application> applications = this.applicationIndexDao.selectAllApplicationNames();
        stopWatch.stop();

        stopWatch.start("Insert all applicationNames to v2");
        for (Application application : applications) {
            applicationDao.insert(ServiceUid.DEFAULT, application.getName(), application.getServiceTypeCode());
        }
        stopWatch.stop();
        logger.info(stopWatch.prettyPrint());
    }

    @Override
    public void copyAgentId() {
        StopWatch stopWatch = new StopWatch("copyAgentId");
        stopWatch.start("Select all applicationNames from v1");
        List<Application> applications = this.applicationIndexDao.selectAllApplicationNames();
        stopWatch.stop();
        stopWatch.start("Insert Each application agentIds from v1 to v2");
        for (Application application : applications) {
            List<String> agentIds = applicationIndexDao.selectAgentIds(application.getName(), application.getServiceTypeCode());
            for (int i = 0; i < agentIds.size(); i += 100) {
                int end = Math.min(i + 100, agentIds.size());
                List<String> agentIdBatch = agentIds.subList(i, end);
                agentIdDao.insert(ServiceUid.DEFAULT, application.getName(), application.getServiceTypeCode(), agentIdBatch);
            }
        }
        stopWatch.stop();
        logger.info(stopWatch.prettyPrint());
    }
}
