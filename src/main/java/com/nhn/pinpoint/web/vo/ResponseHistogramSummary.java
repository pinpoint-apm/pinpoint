package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.web.applicationmap.rawdata.ResponseHistogram;

import java.util.HashMap;
import java.util.Map;

/**
 * @author emeroad
 */
public class ResponseHistogramSummary {

    private final Application application;

    private final ResponseHistogram total;

    private Map<String, ResponseHistogram> agentHistogram;

    public ResponseHistogramSummary(Application application) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        this.application = application;
        this.total = new ResponseHistogram(application.getServiceType());
    }

    public void addTotal(ResponseHistogram histogram) {
        if (histogram == null) {
            throw new NullPointerException("histogram must not be null");
        }
        this.total.add(histogram);
    }

    public ResponseHistogram getTotal() {
        return total;
    }

    public void addLinkHistogram(ResponseHistogram linkHistogram) {
        if (linkHistogram == null) {
            throw new NullPointerException("histogram must not be null");
        }
        this.total.addUncheckType(linkHistogram);
    }

    public void setAgentHistogram(Map<String, ResponseHistogram> agentHistogram) {
        this.agentHistogram = agentHistogram;
    }

    public Map<String, ResponseHistogram> getAgentHistogram() {
        return agentHistogram;
    }
}
