package com.navercorp.pinpoint.uristat.web.model;

import java.util.Arrays;
import java.util.List;

public class UriStatChartValue {
    private final long timestamp;
    private final List<Double> values;
    private final String version;

    public UriStatChartValue(long timestamp, Double hist0, Double hist1, Double hist2, Double hist3,
                             Double hist4, Double hist5, Double hist6, Double hist7, String version) {
        this.timestamp = timestamp;
        this.values = Arrays.asList(hist0, hist1, hist2, hist3, hist4, hist5, hist6, hist7);
        this.version = version;
    }

    public UriStatChartValue(long timestamp, Double apdexRaw, Double count, String version) {
        Double apdex = (count == 0)? -1: (apdexRaw / count);
        this.timestamp = timestamp;
        this.values = Arrays.asList(apdex);
        this.version = version;
    }

    public UriStatChartValue(long timestamp, Double totalTime, Double maxTime, Double count, String version) {
        Double avgTime = (count == 0)? -1: (totalTime / count);
        this.timestamp = timestamp;
        this.values = Arrays.asList(avgTime, maxTime);
        this.version = version;
    }
    public long getTimestamp() {
        return timestamp;
    }

    public List<Double> getValues() {
        return values;
    }

    public String getVersion() {
        return version;
    }
}
