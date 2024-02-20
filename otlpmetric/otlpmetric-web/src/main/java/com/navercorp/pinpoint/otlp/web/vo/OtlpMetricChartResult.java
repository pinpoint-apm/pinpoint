package com.navercorp.pinpoint.otlp.web.vo;

public record OtlpMetricChartResult (
        long eventTime,
        String version,
        Number value
){

}