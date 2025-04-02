package com.navercorp.pinpoint.uristat.web.model;

import com.navercorp.pinpoint.metric.web.view.TimeseriesChartType;

import java.util.List;

public class UriStatChartValue {
    private long timestamp;
    private List<Double> values;
    private String version;
    private TimeseriesChartType chartType;
    private String unit;

    public UriStatChartValue() {
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<Double> getValues() {
        return values;
    }

    public void setValues(List<Double> values) {
        this.values = values;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public TimeseriesChartType getChartType() {
        return chartType;
    }

    public void setChartType(TimeseriesChartType chartType) {
        this.chartType = chartType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
