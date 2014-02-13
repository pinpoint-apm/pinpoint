package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author emeroad
 */
public class ResponseHistogramSummary {

    private final Application application;

    private final Histogram total;

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

    public void createResponseHistogram(List<RawResponseTime> responseHistogramList) {
        createApplicationLevelResponseTime(responseHistogramList);
        createAgentLevelResponseTime(responseHistogramList);
    }

    private void createAgentLevelResponseTime(List<RawResponseTime> responseHistogramList) {

        for (RawResponseTime rawResponseTime : responseHistogramList) {
            for (Map.Entry<String, Histogram> entry : rawResponseTime.getAgentHistogram()) {
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

    private void createApplicationLevelResponseTime(List<RawResponseTime> responseHistogram) {
        for (RawResponseTime rawResponseTime : responseHistogram) {
            final List<Histogram> histogramList = rawResponseTime.getResponseHistogramList();
            for (Histogram histogram : histogramList) {
                this.addApplicationLevelHistogram(histogram);
            }
        }
    }
}
