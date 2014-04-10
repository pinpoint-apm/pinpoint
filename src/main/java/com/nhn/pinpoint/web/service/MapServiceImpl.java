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
import com.nhn.pinpoint.web.applicationmap.ApplicationMap;

/**
 * @author netspider
 * @author emeroad
 */
@Service
public class MapServiceImpl implements MapService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AgentInfoService agentInfoService;

    @Autowired
    private MapResponseDao mapResponseDao;

    @Autowired
    private MapStatisticsCalleeDao mapStatisticsCalleeDao;

    @Autowired
    private MapStatisticsCallerDao mapStatisticsCallerDao;

    @Autowired
    private HostApplicationMapDao hostApplicationMapDao;


    /**
     * callerApplicationName이 호출한 callee를 모두 조회
     *
     * @param callerApplication
     * @param range
     * @param linkVisitChecker
     * @return
     */
    private LinkDataDuplexMap selectCaller(Application callerApplication, Range range, LinkVisitChecker linkVisitChecker) {
        // 이미 조회된 구간이면 skip
        if (linkVisitChecker.visitCaller(callerApplication)) {
            return new LinkDataDuplexMap();
        }

        LinkDataMap caller = mapStatisticsCallerDao.selectCaller(callerApplication, range);
        if (logger.isDebugEnabled()) {
            logger.debug("Found Caller. count={}, caller={}", caller.size(), callerApplication);
        }

        final LinkDataDuplexMap resultCaller = new LinkDataDuplexMap();
        for (LinkData link : caller.getLinkDataList()) {
            link = checkRpcCallAccepted(link, range);

            resultCaller.addSourceLinkData(link);

            final Application toApplication = link.getToApplication();
            // terminal, unknowncloud 인 경우에는 skip
            if (toApplication.getServiceType().isTerminal() || toApplication.getServiceType().isUnknown()) {
                continue;
            }

            logger.debug("     Find subCaller of {}", toApplication);
            LinkDataDuplexMap callerSub = selectCaller(toApplication, range, linkVisitChecker);
            logger.debug("     Found subCaller. count={}, caller={}", callerSub.size(), toApplication);

            resultCaller.addLinkDataDuplexMap(callerSub);

            // 찾아진 녀석들에 대한 caller도 찾는다.
            for (LinkData eachCaller : callerSub.getSourceLinkDataList()) {
                logger.debug("     Find callee of {}", eachCaller.getFromApplication());
                LinkDataDuplexMap calleeSub = selectCallee(eachCaller.getFromApplication(), range, linkVisitChecker);
                logger.debug("     Found subCallee. count={}, callee={}", calleeSub.size(), eachCaller.getFromApplication());
                resultCaller.addLinkDataDuplexMap(calleeSub);
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
    private LinkDataDuplexMap selectCallee(Application calleeApplication, Range range, LinkVisitChecker linkVisitChecker) {
        // 이미 조회된 구간이면 skip
        if (linkVisitChecker.visitCallee(calleeApplication)) {
            return new LinkDataDuplexMap();
        }

        final LinkDataMap callee = mapStatisticsCalleeDao.selectCallee(calleeApplication, range);
        logger.debug("Found Callee. count={}, callee={}", callee.size(), calleeApplication);

        final LinkDataDuplexMap calleeSet = new LinkDataDuplexMap();
        for (LinkData stat : callee.getLinkDataList()) {
            calleeSet.addTargetLinkData(stat);

            // 나를 부른 application을 찾아야 하기 떄문에 to를 입력.
            LinkDataDuplexMap calleeSub = selectCallee(stat.getFromApplication(), range, linkVisitChecker);
            calleeSet.addLinkDataDuplexMap(calleeSub);

            // 찾아진 녀석들에 대한 callee도 찾는다.
            for (LinkData eachCallee : calleeSub.getTargetLinkDataList()) {
                // terminal이면 skip
                final Application eachCalleeToApplication = eachCallee.getToApplication();
                if (eachCalleeToApplication.getServiceType().isTerminal() || eachCalleeToApplication.getServiceType().isUnknown()) {
                    continue;
                }
                LinkDataDuplexMap callerSub = selectCaller(eachCalleeToApplication, range, linkVisitChecker);
                calleeSet.addLinkDataDuplexMap(callerSub);
            }
        }

        return calleeSet;
    }

    private LinkData checkRpcCallAccepted(LinkData stat, Range range) {
        // rpc client의 목적지가 agent가 설치되어 application name이 존재한다면 replace.
        final Application toApplication = stat.getToApplication();
        if (toApplication.getServiceType().isRpcClient()) {
            logger.debug("Find applicationName:{} {}", toApplication, range);
            final Application app = hostApplicationMapDao.findApplicationName(toApplication.getName(), range);
            if (app != null) {
                logger.debug("Application info replaced. {} => {}", stat, app);
                Application acceptedApplication = new Application(app.getName(), app.getServiceType());

                final LinkData acceptedLinkData = new LinkData(stat.getFromApplication(), acceptedApplication, stat.getLinkCallDataMap());
                return acceptedLinkData;
            } else {
                Application unknown = new Application(toApplication.getName(), ServiceType.UNKNOWN);
                LinkData unknownLinkData = new LinkData(stat.getFromApplication(), unknown, stat.getLinkCallDataMap());
                return unknownLinkData;
            }
        }
        return stat;
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
        LinkDataDuplexMap caller = selectCaller(sourceApplication, range, linkVisitChecker);
        logger.debug("Result of finding caller {}", caller);

        LinkDataDuplexMap callee = selectCallee(sourceApplication, range, linkVisitChecker);
        logger.debug("Result of finding callee {}", callee);

        LinkDataDuplexMap linkDataDuplexMap = new LinkDataDuplexMap();
        linkDataDuplexMap.addLinkDataDuplexMap(caller);
        linkDataDuplexMap.addLinkDataDuplexMap(callee);

        ApplicationMapBuilder builder = new ApplicationMapBuilder(range);
        ApplicationMap map = builder.build(linkDataDuplexMap, agentInfoService, this.mapResponseDao);

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

        List<LinkDataMap> list = selectLink(sourceApplication, destinationApplication, range);
        logger.debug("Fetched statistics data size={}", list.size());

        MapResponseHistogramSummary responseHistogramSummary = new MapResponseHistogramSummary(range);
        for (LinkDataMap entry : list) {
            for (LinkData linkData : entry.getLinkDataList()) {
                CallHistogramList sourceList = linkData.getSourceList();
                Collection<CallHistogram> callHistogramList = sourceList.getCallHistogramList();
                for (CallHistogram histogram : callHistogramList) {
                    for (TimeHistogram timeHistogram : histogram.getTimeHistogram()) {
                        Application toApplication = linkData.getToApplication();
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

    private List<LinkDataMap> selectLink(Application sourceApplication, Application destinationApplication, Range range) {
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
