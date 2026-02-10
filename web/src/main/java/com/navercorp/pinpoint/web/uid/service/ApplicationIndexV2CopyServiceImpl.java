package com.navercorp.pinpoint.web.uid.service;

import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.dao.AgentInfoDao;
import com.navercorp.pinpoint.web.dao.ApplicationDao;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.mapper.Timestamped;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Service
public class ApplicationIndexV2CopyServiceImpl implements ApplicationIndexV2CopyService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationIndexDao applicationIndexDao;
    private final AgentInfoDao agentInfoDao;

    private final ApplicationDao applicationDao;
    private final AgentIdDao agentIdDao;

    public ApplicationIndexV2CopyServiceImpl(ApplicationIndexDao applicationIndexDao,
                                             AgentInfoDao agentInfoDao,
                                             ApplicationDao applicationDao,
                                             AgentIdDao agentIdDao) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.agentInfoDao = Objects.requireNonNull(agentInfoDao, "agentInfoDao");
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
            applicationDao.insert(ServiceUid.DEFAULT.getUid(), application.getName(), application.getServiceTypeCode());
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
            List<Timestamped<AgentInfoBo>> timestampedList = agentInfoDao.getAgentInfo(batchSize, fromTimestamp, previousAgentId, previousAgentStartTime);
            logger.info("iteration={}, fetched agentInfo size={}", iteration, timestampedList.size());
            if (timestampedList.isEmpty()) {
                break;
            }
            for (Timestamped<AgentInfoBo> timestamped : timestampedList) {
                AgentInfoBo agentInfoBo = timestamped.getValue();
                agentIdDao.insert(ServiceUid.DEFAULT.getUid(), agentInfoBo.getApplicationName(), agentInfoBo.getServiceTypeCode(), agentInfoBo.getAgentId(), agentInfoBo.getStartTime(), agentInfoBo.getHostName(),
                        timestamped.getTimestamp());
            }

            iteration++;
            AgentInfoBo lastAgentInfo = timestampedList.get(timestampedList.size() - 1).getValue();
            previousAgentId = lastAgentInfo.getAgentId();
            previousAgentStartTime = lastAgentInfo.getStartTime();
        }
        if (iteration >= maxIteration) {
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
                copyAgentId(application.getName());
            }
        }
        stopWatch.stop();
        logger.info(stopWatch.prettyPrint());
    }

    @Override
    public void copyAgentId(String applicationName) {
        logger.info("copyAgentId started for applicationName={}", applicationName);
        List<String> agentIds = this.applicationIndexDao.selectAgentIds(applicationName);
        long toTimestamp = System.currentTimeMillis();
        List<AgentInfo> agentInfos = this.agentInfoDao.getSimpleAgentInfos(agentIds, toTimestamp);
        for (AgentInfo agentInfo : agentInfos) {
            insertAgentInfo(agentInfo, agentInfo.getStartTimestamp());
        }
    }

    private void insertAgentInfo(AgentInfo agentInfo, long timestamp) {
        if (agentInfo == null) {
            return;
        }
        this.agentIdDao.insert(ServiceUid.DEFAULT.getUid(), agentInfo.getApplicationName(), agentInfo.getServiceTypeCode(), agentInfo.getAgentId(), agentInfo.getStartTimestamp(), agentInfo.getAgentName(),
                timestamp);
    }
}
