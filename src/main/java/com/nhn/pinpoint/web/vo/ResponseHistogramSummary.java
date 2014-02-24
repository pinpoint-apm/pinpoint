package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.TimeHistogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;

/**
 * @author emeroad
 */
public class ResponseHistogramSummary {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private final Application application;

    private final Histogram total;

    // key는 agentId이다.
    private Map<String, Histogram> agentHistogramMap = new HashMap<String, Histogram>();

    public ResponseHistogramSummary(Application application) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        this.application = application;
        this.total = new Histogram(application.getServiceType());
    }

    public void addApplicationLevelHistogram(Histogram histogram) {
        if (histogram == null) {
            throw new NullPointerException("histogram must not be null");
        }
        this.total.add(histogram);
    }

    public Histogram getTotal() {
        return total;
    }

    public void addLinkHistogram(Histogram linkHistogram) {
        if (linkHistogram == null) {
            throw new NullPointerException("histogram must not be null");
        }
        this.total.addUncheckType(linkHistogram);
    }

    public Map<String, Histogram> getAgentHistogramMap() {
        return agentHistogramMap;
    }

    public void createResponseHistogram(List<ResponseTime> responseHistogramList) {
        createApplicationLevelResponseTime(responseHistogramList);
        createAgentLevelResponseTime(responseHistogramList);

        createApplicationLevelTimeSeriesResponseTime(responseHistogramList);
        createAgentLevelTimeSeriesResponseTime(responseHistogramList);
    }

    private void createApplicationLevelTimeSeriesResponseTime(List<ResponseTime> responseHistogramList) {

    }

    private void createAgentLevelTimeSeriesResponseTime(List<ResponseTime> responseHistogramList) {
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
                TimeHistogram timeHistogram = new TimeHistogram(histogram.getServiceType(), responseTime.getTimeStamp());
                timeHistogram.getHistogram().add(histogram);
                histogramList.add(timeHistogram);
            }
        }
        sortList(agentLevelMap);
        for (Map.Entry<String, List<TimeHistogram>> agentListEntry : agentLevelMap.entrySet()) {
            String agentName = agentListEntry.getKey();
            logger.debug("------------agentName:{}", agentName);
            List<TimeHistogram> value = agentListEntry.getValue();
            for (TimeHistogram histogram : value) {
                logger.debug("time:{} histogram:{}", histogram.getTimeStamp(), histogram);
            }
        }


    }

    private void sortList(Map<String, List<TimeHistogram>> agentLevelMap) {
        Collection<List<TimeHistogram>> values = agentLevelMap.values();
        for (List<TimeHistogram> value : values) {
            Collections.sort(value, TimeHistogram.ASC_COMPARATOR);
        }
    }

    private void createAgentLevelResponseTime(List<ResponseTime> responseHistogramList) {

        for (ResponseTime responseTime : responseHistogramList) {
            for (Map.Entry<String, Histogram> entry : responseTime.getAgentHistogram()) {
                addAgentLevelHistogram(entry.getKey(), entry.getValue());
            }
        }
    }

    public void addAgentLevelHistogram(String agentId, Histogram histogram) {
        Histogram agentHistogram = this.agentHistogramMap.get(agentId);
        if (agentHistogram == null) {
            agentHistogram = new Histogram(application.getServiceType());
            this.agentHistogramMap.put(agentId, agentHistogram);
        }
        agentHistogram.add(histogram);
    }

    private void createApplicationLevelResponseTime(List<ResponseTime> responseHistogram) {
        for (ResponseTime responseTime : responseHistogram) {
            final Collection<Histogram> histogramList = responseTime.getResponseHistogramList();
            for (Histogram histogram : histogramList) {
                this.addApplicationLevelHistogram(histogram);
            }
        }
    }
}
