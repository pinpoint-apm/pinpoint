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

    private final Histogram applicationHistogram;

    // key는 agentId이다.
    private Map<String, Histogram> agentHistogramMap = new HashMap<String, Histogram>();

    private ApplicationTimeSeriesHistogram applicationTimeSeriesHistogram;

    private AgentTimeSeriesHistogram agentTimeSeriesHistogram;

    public ResponseHistogramSummary(Application application) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        this.application = application;
        this.applicationHistogram = new Histogram(application.getServiceType());

        this.applicationTimeSeriesHistogram = new ApplicationTimeSeriesHistogram(application);
        this.agentTimeSeriesHistogram = new AgentTimeSeriesHistogram(application);
    }

    public void addApplicationLevelHistogram(Histogram histogram) {
        if (histogram == null) {
            throw new NullPointerException("histogram must not be null");
        }
        this.applicationHistogram.add(histogram);
    }

    public Histogram getApplicationHistogram() {
        return applicationHistogram;
    }

    public void addLinkHistogram(Histogram linkHistogram) {
        if (linkHistogram == null) {
            throw new NullPointerException("histogram must not be null");
        }
        this.applicationHistogram.addUncheckType(linkHistogram);
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

        ApplicationTimeSeriesHistogram histogram = new ApplicationTimeSeriesHistogram(application);
        histogram.build(responseHistogramList);

        this.applicationTimeSeriesHistogram = histogram;

    }

    private void createAgentLevelTimeSeriesResponseTime(List<ResponseTime> responseHistogramList) {
        AgentTimeSeriesHistogram histogram = new AgentTimeSeriesHistogram(application);
        histogram.build(responseHistogramList);

        this.agentTimeSeriesHistogram = histogram;
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
            final Collection<Histogram> histogramList = responseTime.getAgentResponseHistogramList();
            for (Histogram histogram : histogramList) {
                this.addApplicationLevelHistogram(histogram);
            }
        }
    }
}
