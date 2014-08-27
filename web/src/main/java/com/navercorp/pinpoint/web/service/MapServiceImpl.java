package com.nhn.pinpoint.web.service;

import java.util.*;

import com.nhn.pinpoint.web.applicationmap.ApplicationMapBuilder;
import com.nhn.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.nhn.pinpoint.web.applicationmap.histogram.TimeHistogram;
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
        LinkDataSelector linkDataSelector = new LinkDataSelector(this.mapStatisticsCalleeDao, this.mapStatisticsCallerDao, hostApplicationMapDao);
        LinkDataDuplexMap linkDataDuplexMap = linkDataSelector.select(sourceApplication, range);

        ApplicationMapBuilder builder = new ApplicationMapBuilder(range);
        ApplicationMap map = builder.build(linkDataDuplexMap, agentInfoService, this.mapResponseDao);

        watch.stop();
        logger.info("Fetch applicationmap elapsed. {}ms", watch.getLastTaskTimeMillis());

        return map;
    }


    @Override
    @Deprecated
    public NodeHistogram linkStatistics(Application sourceApplication, Application destinationApplication, Range range) {
        if (sourceApplication == null) {
            throw new NullPointerException("sourceApplication must not be null");
        }
        if (destinationApplication == null) {
            throw new NullPointerException("destinationApplication must not be null");
        }

        List<LinkDataMap> list = selectLink(sourceApplication, destinationApplication, range);
        logger.debug("Fetched statistics data size={}", list.size());

        ResponseHistogramBuilder responseHistogramSummary = new ResponseHistogramBuilder(range);
        for (LinkDataMap entry : list) {
            for (LinkData linkData : entry.getLinkDataList()) {
                AgentHistogramList sourceList = linkData.getSourceList();
                Collection<AgentHistogram> agentHistogramList = sourceList.getAgentHistogramList();
                for (AgentHistogram histogram : agentHistogramList) {
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
        final NodeHistogram histogramSummary = new NodeHistogram(destinationApplication, range, responseTimeList);
        return histogramSummary;
    }

    @Deprecated
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
