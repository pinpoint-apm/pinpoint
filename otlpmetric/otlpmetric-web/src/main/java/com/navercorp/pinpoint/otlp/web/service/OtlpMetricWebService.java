package com.navercorp.pinpoint.otlp.web.service;

import com.navercorp.pinpoint.otlp.web.view.OtlpChartView;

import java.util.List;

public interface OtlpMetricWebService {
    List<String> getMetricGroupList(String tenantId, String serviceId, String applicationName, String agentId);

    List<String> getMetricList(String tenantId, String serviceId, String applicationName, String agentId, String metricGroupName);

    List<String> getTags(String tenantId, String serviceId, String applicationName, String agentId, String metricGroupName, String metricName);

    OtlpChartView getMetricChartData(String tenantId, String serviceId, String applicationName, String agentId, String metricGroupName, String metricName, String tag, long from, long to);
}
