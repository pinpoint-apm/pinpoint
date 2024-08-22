package com.navercorp.pinpoint.otlp.web.service;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.otlp.common.web.definition.property.AggregationFunction;
import com.navercorp.pinpoint.otlp.common.web.definition.property.ChartType;
import com.navercorp.pinpoint.otlp.web.view.MetricDataView;
import com.navercorp.pinpoint.otlp.web.view.legacy.OtlpChartView;
import com.navercorp.pinpoint.otlp.web.vo.MetricData;

import java.util.List;

public interface OtlpMetricWebService {
    List<String> getMetricGroupList(String tenantId, String serviceId, String applicationName, String agentId);

    List<String> getMetricList(String tenantId, String serviceId, String applicationName, String agentId, String metricGroupName);

    List<String> getTags(String tenantId, String serviceId, String applicationName, String agentId, String metricGroupName, String metricName);

    OtlpChartView getMetricChartData(String tenantId, String serviceId, String applicationName, String agentId, String metricGroupName, String metricName, String tag, long from, long to);

    MetricData getMetricData(String tenantId, String serviceId, String applicationName, String agentId, String metricGroupName, String metricName, String tag, List<String> fieldNameList, ChartType chartType, AggregationFunction aggregationFunction, TimeWindow timeWindow);
}
