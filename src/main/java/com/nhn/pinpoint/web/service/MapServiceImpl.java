package com.nhn.pinpoint.web.service;

import java.util.*;

import com.nhn.pinpoint.web.applicationmap.ApplicationMapBuilder;
import com.nhn.pinpoint.web.applicationmap.rawdata.*;
import com.nhn.pinpoint.web.dao.*;
import com.nhn.pinpoint.web.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.applicationmap.ApplicationMap;

/**
 * @author netspider
 * @author emeroad
 */
@Service
public class MapServiceImpl implements MapService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationIndexDao applicationIndexDao;

    @Autowired
    private AgentInfoDao agentInfoDao;

    @Autowired
    private MapResponseDao mapResponseDao;

    @Autowired
    private MapStatisticsCalleeDao mapStatisticsCalleeDao;

    @Autowired
    private MapStatisticsCallerDao mapStatisticsCallerDao;

    @Autowired
    private HostApplicationMapDao hostApplicationMapDao;

    private Set<AgentInfoBo> selectAgents(String applicationId) {
        if (applicationId == null) {
            throw new NullPointerException("applicationId must not be null");
        }

        List<String> agentIds = applicationIndexDao.selectAgentIds(applicationId);
        Set<AgentInfoBo> agentSet = new HashSet<AgentInfoBo>();
        for (String agentId : agentIds) {
            // TODO 조회 시간대에 따라서 agent info row timestamp를 변경하여 조회해야하는지는 모르겠음.
            AgentInfoBo info = agentInfoDao.findAgentInfoBeforeStartTime(agentId, System.currentTimeMillis());
            agentSet.add(info);
        }
        return agentSet;
    }

    /**
     * callerApplicationName이 호출한 callee를 모두 조회
     *
     * @param callerApplication
     * @param range
     * @param linkVisitChecker
     * @return
     */
    private LinkStatisticsDataSet selectCaller(Application callerApplication, Range range, LinkVisitChecker linkVisitChecker) {
        // 이미 조회된 구간이면 skip
        if (linkVisitChecker.visitCaller(callerApplication)) {
            return new LinkStatisticsDataSet();
        }

        LinkStatisticsData caller = mapStatisticsCallerDao.selectCaller(callerApplication, range);
        if (logger.isDebugEnabled()) {
            logger.debug("Found Caller. count={}, caller={}", caller.size(), callerApplication);
        }

        final LinkStatisticsDataSet resultCaller = new LinkStatisticsDataSet();
        for (LinkStatistics link : caller.getLinkStatData()) {
            final AcceptedLinkStatistics isAccepted = getRpcCallAccepted(link, range);
            link = isAccepted.getLinkStatistics();
            // replaced된 녀석은 CLIENT이기 때문에 callee검색용도로만 사용하고 map에 추가하지 않는다.
            if (!isAccepted.isAccepted()) {
                fillAdditionalInfo(link, range);
//                resultCaller.addSourceLinkStatistics(link);
            }
            resultCaller.addSourceLinkStatistics(link);

            final Application toApplication = link.getToApplication();
            // terminal, unknowncloud 인 경우에는 skip
            if (toApplication.getServiceType().isTerminal() || toApplication.getServiceType().isUnknown()) {
                continue;
            }

            logger.debug("     Find subCaller of {}", toApplication);
            LinkStatisticsDataSet callerSub = selectCaller(toApplication, range, linkVisitChecker);
            logger.debug("     Found subCaller. count={}, caller={}", callerSub.size(), toApplication);

            resultCaller.addLinkStatisticsDataSet(callerSub);

            // 찾아진 녀석들에 대한 caller도 찾는다.
            for (LinkStatistics eachCaller : callerSub.getSourceLinkStatData()) {
                logger.debug("     Find callee of {}", eachCaller.getFromApplication());
                LinkStatisticsDataSet calleeSub = selectCallee(eachCaller.getFromApplication(), range, linkVisitChecker);
                logger.debug("     Found subCallee. count={}, callee={}", calleeSub.size(), eachCaller.getFromApplication());
                resultCaller.addLinkStatisticsDataSet(calleeSub);
            }
        }

        return resultCaller;
    }

    /**
     * callee applicationname을 호출한 caller 조회.
     *
     * @param calleeApplication
     * @param range
     * @return
     */
    private LinkStatisticsDataSet selectCallee(Application calleeApplication, Range range, LinkVisitChecker linkVisitChecker) {
        // 이미 조회된 구간이면 skip
        if (linkVisitChecker.visitCallee(calleeApplication)) {
            return new LinkStatisticsDataSet();
        }

        final LinkStatisticsData callee = mapStatisticsCalleeDao.selectCallee(calleeApplication, range);
        logger.debug("Found Callee. count={}, callee={}", callee.size(), calleeApplication);

        final LinkStatisticsDataSet calleeSet = new LinkStatisticsDataSet();
        for (LinkStatistics stat : callee.getLinkStatData()) {
            fillAdditionalInfo(stat, range);
            calleeSet.addTargetLinkStatistics(stat);

            // 나를 부른 application을 찾아야 하기 떄문에 to를 입력.
            LinkStatisticsDataSet calleeSub = selectCallee(stat.getFromApplication(), range, linkVisitChecker);
            calleeSet.addLinkStatisticsDataSet(calleeSub);

            // 찾아진 녀석들에 대한 callee도 찾는다.
            for (LinkStatistics eachCallee : calleeSub.getTargetLinkStatData()) {
                // terminal이면 skip
                final Application eachCalleeToApplication = eachCallee.getToApplication();
                if (eachCalleeToApplication.getServiceType().isTerminal() || eachCalleeToApplication.getServiceType().isUnknown()) {
                    continue;
                }
                LinkStatisticsDataSet callerSub = selectCaller(eachCalleeToApplication, range, linkVisitChecker);
                calleeSet.addLinkStatisticsDataSet(callerSub);
            }
        }

        return calleeSet;
    }

    private AcceptedLinkStatistics getRpcCallAccepted(LinkStatistics stat, Range range) {
        // rpc client의 목적지가 agent가 설치되어 application name이 존재한다면 replace.
        final Application toApplication = stat.getToApplication();
        if (toApplication.getServiceType().isRpcClient()) {
            logger.debug("Find applicationName {} {}", toApplication, range);
            final Application app = hostApplicationMapDao.findApplicationName(toApplication.getName(), range);
            if (app != null) {
                logger.debug("Application info replaced. {} => {}", stat, app);
                Application acceptedApplication = new Application(app.getName(), app.getServiceType());
                LinkStatistics linkStat = new LinkStatistics(stat.getFromApplication(), acceptedApplication, stat.getToAgentSet(), stat.getCallDataMap());
                return new AcceptedLinkStatistics(true, linkStat);
            } else {
                Application unknown = new Application(toApplication.getName(), ServiceType.UNKNOWN);
                LinkStatistics unknownLinkStat = new LinkStatistics(stat.getFromApplication(), unknown, stat.getToAgentSet(), stat.getCallDataMap());
                return new AcceptedLinkStatistics(false, unknownLinkStat);
            }
        }
        return new AcceptedLinkStatistics(false, stat);
    }

    public class AcceptedLinkStatistics {
        private final boolean isAccepted;
        private final LinkStatistics linkStatistics;

        public AcceptedLinkStatistics(boolean isAccepted, LinkStatistics linkStatistics) {
            this.isAccepted = isAccepted;
            this.linkStatistics = linkStatistics;
        }

        public boolean isAccepted() {
            return isAccepted;
        }

        public LinkStatistics getLinkStatistics() {
            return linkStatistics;
        }
    }

    private void fillAdditionalInfo(LinkStatistics stat, Range range) {
        final ServiceType toServiceType = stat.getToApplication().getServiceType();
        if (toServiceType.isTerminal() || toServiceType.isUnknown()) {
            return;
        }

        Set<AgentInfoBo> agentSet = selectAgents(stat.getToApplication().getName());
        if (agentSet.isEmpty()) {
            return;
        }

        // destination이 WAS이고 agent가 설치되어있으면 agentSet이 존재한다.
        stat.addToAgentSet(agentSet);

        logger.debug("Fill agent info. {}, {}", stat.getToApplication().getName(), agentSet);
    }

    /**
     * 메인화면에서 사용. 시간별로 TimeSlot을 조회하여 서버 맵을 그릴 때 사용한다.
     */
    @Override
    public ApplicationMap selectApplicationMap(Application sourceApplication, Range range) {
        if (sourceApplication == null) {
            throw new NullPointerException("sourceApplication must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        logger.debug("SelectApplicationMap");

        StopWatch watch = new StopWatch("applicationMapWatch");
        watch.start();

        LinkVisitChecker linkVisitChecker = new LinkVisitChecker();
        LinkStatisticsDataSet caller = selectCaller(sourceApplication, range, linkVisitChecker);
        logger.debug("Result of finding caller {}", caller);

        LinkStatisticsDataSet callee = selectCallee(sourceApplication, range, linkVisitChecker);
        logger.debug("Result of finding callee {}", callee);

        LinkStatisticsDataSet data = new LinkStatisticsDataSet();
        data.addLinkStatisticsDataSet(caller);
        data.addLinkStatisticsDataSet(callee);

        ApplicationMapBuilder builder = new ApplicationMapBuilder(range);
        ApplicationMap map = builder.build(data);
        // 이걸 builder쪽에 넣어야 될듯한데.
        map.appendResponseTime(range, this.mapResponseDao);

        watch.stop();
        logger.info("Fetch applicationmap elapsed. {}ms", watch.getLastTaskTimeMillis());

        return map;
    }


    @Override
    public ResponseHistogramSummary linkStatistics(Application sourceApplication, Application destinationApplication, Range range) {
        if (sourceApplication == null) {
            throw new NullPointerException("sourceApplication must not be null");
        }
        if (destinationApplication == null) {
            throw new NullPointerException("destinationApplication must not be null");
        }

        List<LinkStatisticsData> list = selectLink(sourceApplication, destinationApplication, range);
        logger.debug("Fetched statistics data={}", list);

        MapResponseHistogramSummary responseHistogramSummary = new MapResponseHistogramSummary(range);
        for (LinkStatisticsData entry : list) {
            for (LinkStatistics linkStatistics : entry.getLinkStatData()) {
                CallHistogramList sourceList = linkStatistics.getSourceList();
                Collection<CallHistogram> callHistogramList = sourceList.getCallHistogramList();
                for (CallHistogram histogram : callHistogramList) {
                    for (TimeHistogram timeHistogram : histogram.getTimeHistogram()) {
                        Application toApplication = linkStatistics.getToApplication();
                        if (toApplication.getServiceType().isRpcClient()) {
                            toApplication = new Application(toApplication.getName(), ServiceType.UNKNOWN);
                        }
                        responseHistogramSummary.addLinkHistogram(toApplication, histogram.getId(), timeHistogram);
                    }
                }
            }
        }
        responseHistogramSummary.build();
        List<ResponseTime> responseTimeList = responseHistogramSummary.getResponseTimeList(destinationApplication);
        final ResponseHistogramSummary histogramSummary = new ResponseHistogramSummary(destinationApplication, range, responseTimeList);
        return histogramSummary;
    }

    private List<LinkStatisticsData> selectLink(Application sourceApplication, Application destinationApplication, Range range) {
        if (sourceApplication.getServiceType().isUser()) {
            logger.debug("Find 'client -> any' link statistics");
            // client는 applicatinname + servicetype.client로 기록된다.
            // 그래서 src, dest가 둘 다 dest로 같음.
            Application userApplication = new Application(destinationApplication.getName(), sourceApplication.getServiceTypeCode());
            return mapStatisticsCallerDao.selectCallerStatistics(userApplication, destinationApplication, range);
        } else if (destinationApplication.getServiceType().isWas()) {
            logger.debug("Find 'any -> was' link statistics");
            // destination이 was인 경우에는 중간에 client event가 끼어있기 때문에 callee에서
            // caller가
            // 같은녀석을 찾아야 한다.
            return mapStatisticsCalleeDao.selectCalleeStatistics(sourceApplication, destinationApplication, range);
        } else {
            logger.debug("Find 'was -> terminal' link statistics");
            // 일반적으로 was -> terminal 간의 통계정보 조회.
            return mapStatisticsCallerDao.selectCallerStatistics(sourceApplication, destinationApplication, range);
        }
    }
}
