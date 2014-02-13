package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author emeroad
 */
public class ResponseHistogramSummary {

    private final Application application;

    private final Histogram total;

    private Map<String, Histogram> agentHistogram = new HashMap<String, Histogram>();

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

    public void setAgentHistogram(Map<String, Histogram> agentHistogram) {
        this.agentHistogram = agentHistogram;
    }

    public Map<String, Histogram> getAgentHistogram() {
        return agentHistogram;
    }

    public void createResponseHistogram(List<RawResponseTime> responseHistogram) {
        createApplicationLevelResponseTime(responseHistogram);
        createAgentLevelResponseTime(responseHistogram);
    }

    private void createAgentLevelResponseTime(List<RawResponseTime> responseHistogram) {

        for (RawResponseTime rawResponseTime : responseHistogram) {
            Set<Map.Entry<String, Histogram>> agentHistogramEntry = rawResponseTime.getAgentHistogram();
            for (Map.Entry<String, Histogram> entry : agentHistogramEntry) {
                Histogram agentHistogram = this.agentHistogram.get(entry.getKey());
                if (agentHistogram == null) {
                    agentHistogram = new Histogram(application.getServiceType());
                    this.agentHistogram.put(entry.getKey(), agentHistogram);
                }
                agentHistogram.add(entry.getValue());
            }
        }
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
