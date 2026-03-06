package com.navercorp.pinpoint.web.uid.service;

import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.SimpleAgentKey;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.dao.AgentInfoDao;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.dao.ApplicationDao;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ApplicationIndexV2CopyServiceImpl implements ApplicationIndexV2CopyService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationIndexDao applicationIndexDao;
    private final AgentInfoDao agentInfoDao;

    private final ApplicationDao applicationDao;
    private final AgentIdDao agentIdDao;

    private final AgentLifeCycleDao agentLifeCycleDao;

    public ApplicationIndexV2CopyServiceImpl(ApplicationIndexDao applicationIndexDao,
                                             AgentInfoDao agentInfoDao,
                                             ApplicationDao applicationDao,
                                             AgentIdDao agentIdDao,
                                             AgentLifeCycleDao agentLifeCycleDao) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.agentInfoDao = Objects.requireNonNull(agentInfoDao, "agentInfoDao");
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.agentLifeCycleDao = Objects.requireNonNull(agentLifeCycleDao, "agentLifeCycleDao");
    }

    @Override
    public void copyApplication() {
        StopWatch stopWatch = new StopWatch("copyApplicationName");
        stopWatch.start("Select all applicationNames from v1");
        List<Application> applications = this.applicationIndexDao.selectAllApplicationNames();
        stopWatch.stop();

        stopWatch.start("Insert all applicationNames to v2");
        for (Application application : applications) {
            if (application.getServiceType().equals(ServiceType.UNDEFINED)) {
                continue;
            }
            applicationDao.insert(ServiceUid.DEFAULT_SERVICE_UID_CODE, application.getApplicationName(), application.getServiceTypeCode());
        }
        stopWatch.stop();
        logger.info(stopWatch.prettyPrint());
    }

    @Override
    public void copyAgentId(int durationDays, int maxIteration, int batchSize) {
        logger.info("copyAgentId started. durationDays={}, maxIteration={}, batchSize={}", durationDays, maxIteration, batchSize);
        StopWatch stopWatch = new StopWatch("copyAgentId");
        stopWatch.start("Copy agentId from agentInfo to v2");
        long fromTimestamp;
        if (durationDays <= 0) {
            // copy all
            fromTimestamp = 0;
        } else {
            fromTimestamp = System.currentTimeMillis() - Duration.ofDays(durationDays).toMillis();
        }

        String previousAgentId = null;
        long previousAgentStartTime = 0;
        int iteration = 0;
        while (iteration < maxIteration) {
            List<AgentInfoBo> agentInfoBoList = agentInfoDao.getAgentInfoBo(batchSize, fromTimestamp, previousAgentId, previousAgentStartTime).stream()
                    .filter(Objects::nonNull)
                    .toList();
            logger.info("iteration={}, fetched agentInfo size={}", iteration, agentInfoBoList.size());
            if (agentInfoBoList.isEmpty()) {
                break;
            }
            copyAgentInfoBo(agentInfoBoList);

            iteration++;
            AgentInfoBo lastAgentInfo = agentInfoBoList.get(agentInfoBoList.size() - 1);
            previousAgentId = lastAgentInfo.getAgentId();
            previousAgentStartTime = lastAgentInfo.getStartTime();
        }
        if (iteration == maxIteration) {
            logger.warn("copyAgentId stopped by iteration limit: {}", iteration);
        }
        stopWatch.stop();
        logger.info(stopWatch.prettyPrint());
    }

    @Override
    public void copyAgentId(int serviceTypeCode) {
        logger.info("copyAgentId started for serviceTypeCode={}", serviceTypeCode);
        StopWatch stopWatch = new StopWatch("copyAgentId with serviceTypeCodes");
        stopWatch.start("select all applicationNames from v1");
        List<Application> applications = this.applicationIndexDao.selectAllApplicationNames();
        stopWatch.stop();

        stopWatch.start("copy agentIds with serviceTypeCode");
        for (Application application : applications) {
            if (application.getServiceTypeCode() == serviceTypeCode) {
                copyAgentId(application.getApplicationName(), application.getServiceTypeCode());
            }
        }
        stopWatch.stop();
        logger.info(stopWatch.prettyPrint());
    }

    @Override
    public void copyAgentId(String applicationName) {
        logger.info("copyAgentId started for applicationName={}", applicationName);
        List<String> agentIds = this.applicationIndexDao.selectAgentIds(applicationName);
        batchCopyAgentId(agentIds, 40);
    }

    private void copyAgentId(String applicationName, int serviceTypeCode) {
        logger.info("copyAgentId started for applicationName={}, serviceTypeCode={}", applicationName, serviceTypeCode);
        List<String> agentIds = this.applicationIndexDao.selectAgentIds(applicationName, serviceTypeCode);
        batchCopyAgentId(agentIds, 40);
    }

    private void batchCopyAgentId(List<String> agentIds, int agentIdBatchSize) {
        logger.info("batchCopyAgentId started. agentIds size={}, agentIdBatchSize={}", agentIds.size(), agentIdBatchSize);
        for (int i = 0; i < agentIds.size(); i += agentIdBatchSize) {
            int end = Math.min(i + agentIdBatchSize, agentIds.size());
            List<String> agentIdBatch = agentIds.subList(i, end);

            List<AgentInfoBo> agentInfoBoList = agentInfoDao.getAgentInfoBo(agentIdBatch).stream()
                    .filter(Objects::nonNull)
                    .toList();
            copyAgentInfoBo(agentInfoBoList);
        }
    }

    private void copyAgentInfoBo(List<AgentInfoBo> agentInfoBoList) {
        List<SimpleAgentKey> keys = agentInfoBoList.stream()
                .map(bo -> new SimpleAgentKey(bo.getAgentId(), bo.getStartTime()))
                .toList();
        List<Optional<AgentStatus>> currentAgentStatus = agentLifeCycleDao.getCurrentAgentStatus(keys);
        try {
            for (int i = 0; i < agentInfoBoList.size(); i++) {
                AgentInfoBo agentInfoBo = agentInfoBoList.get(i);
                AgentStatus agentStatus = currentAgentStatus.get(i).orElse(null);
                agentIdDao.insert(ServiceUid.DEFAULT_SERVICE_UID_CODE, agentInfoBo.getApplicationName(), agentInfoBo.getServiceTypeCode(), agentInfoBo.getAgentId(),
                        agentInfoBo.getStartTime(), agentInfoBo.getAgentName(), agentStatus);
            }
        } catch (Exception e) {
            logger.error("Failed to insert agentInfoBo batch. batchSize={}", agentInfoBoList.size(), e);
        }
    }
}
