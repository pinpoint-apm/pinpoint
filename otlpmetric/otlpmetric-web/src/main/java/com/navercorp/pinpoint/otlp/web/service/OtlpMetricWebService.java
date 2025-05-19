package com.navercorp.pinpoint.otlp.web.service;

import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.otlp.common.web.defined.PrimaryForFieldAndTagRelation;
import com.navercorp.pinpoint.otlp.common.web.definition.property.AggregationFunction;
import com.navercorp.pinpoint.otlp.common.web.definition.property.ChartType;
import com.navercorp.pinpoint.otlp.web.view.legacy.OtlpChartView;
import com.navercorp.pinpoint.otlp.common.web.vo.MetricData;

import java.util.List;

public interface OtlpMetricWebService {
    List<String> getMetricGroupList(String tenantId, String serviceName, String applicationName, String agentId);

    List<String> getMetricList(String tenantId, String serviceName, String applicationName, String agentId, String metricGroupName);

    List<String> getTags(String tenantId, String serviceName, String applicationName, String agentId, String metricGroupName, String metricName);

    OtlpChartView getMetricChartData(String tenantId, String serviceName, String applicationName, String agentId, String metricGroupName, String metricName, String tag, long from, long to);

    MetricData getMetricData(String tenantId, String serviceName, String applicationName, String agentId, String metricGroupName, String metricName, PrimaryForFieldAndTagRelation primaryForFieldAndTagRelation, List<String> tagGroupList, List<String> fieldNameList, ChartType chartType, AggregationFunction aggregationFunction, TimeWindow timeWindow);
}
