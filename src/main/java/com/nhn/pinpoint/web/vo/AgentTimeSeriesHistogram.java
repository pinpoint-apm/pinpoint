package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.TimeHistogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
public class AgentTimeSeriesHistogram {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Application application;

    private Map<String, List<TimeHistogram>> histogramMap = Collections.emptyMap();

    public AgentTimeSeriesHistogram(Application application) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        this.application = application;
    }

    public void build(List<ResponseTime> responseHistogramList) {
        Map<String, List<TimeHistogram>> agentLevelMap = new HashMap<String, List<TimeHistogram>>();
        for (ResponseTime responseTime : responseHistogramList) {
            Set<Map.Entry<String,Histogram>> agentHistogram = responseTime.getAgentHistogram();
            for (Map.Entry<String, Histogram> agentEntry : agentHistogram) {
                List<TimeHistogram> histogramList = agentLevelMap.get(agentEntry.getKey());
                if (histogramList == null) {
                    histogramList = new ArrayList<TimeHistogram>();
                    agentLevelMap.put(agentEntry.getKey(), histogramList);
                }
                Histogram histogram = agentEntry.getValue();

                TimeHistogram timeHistogram = new TimeHistogram(application.getServiceType(), responseTime.getTimeStamp());
                timeHistogram.getHistogram().add(histogram);
                histogramList.add(timeHistogram);
            }
        }
        sortList(agentLevelMap);
        this.histogramMap = agentLevelMap;

        if (logger.isDebugEnabled()) {
            for (Map.Entry<String, List<TimeHistogram>> agentListEntry : agentLevelMap.entrySet()) {
                String agentName = agentListEntry.getKey();
                logger.debug("agentName:{}", agentName);
                List<TimeHistogram> value = agentListEntry.getValue();
                for (TimeHistogram histogram : value) {
                    logger.debug("histogram:{}", histogram);
                }
            }
        }

    }

    private void sortList(Map<String, List<TimeHistogram>> agentLevelMap) {
        Collection<List<TimeHistogram>> values = agentLevelMap.values();
        for (List<TimeHistogram> value : values) {
            Collections.sort(value, TimeHistogram.ASC_COMPARATOR);
        }
    }
}
