package com.navercorp.pinpoint.otlp.web.vo;

public class OtlpMetricChartSummary {
    private final String dataType;
    private final String value;

    public OtlpMetricChartSummary(String dataType, String value) {
        this.dataType = dataType;
        this.value = value;
    }

    public String getDataType() {
        return dataType;
    }

    public String getValue() {
        return value;
    }
}
