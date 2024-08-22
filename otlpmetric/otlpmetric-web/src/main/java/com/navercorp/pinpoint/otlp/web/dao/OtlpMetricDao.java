package com.navercorp.pinpoint.otlp.web.dao;

import com.navercorp.pinpoint.otlp.common.model.MetricPoint;
import com.navercorp.pinpoint.otlp.web.vo.FieldAttribute;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricChartQueryParameter;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricChartResult;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricDataQueryParameter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface OtlpMetricDao {
    List<String> getMetricGroups(String tenantId, String serviceId, String applicationId, String agentId);

    List<String> getMetrics(String tenantId, String serviceId, String applicationId, String agentId, String metricGroupName);

    List<String> getTags(String tenantId, String serviceId, String applicationId, String agentId, String metricGroupName, String metricName);

    List<FieldAttribute> getFields(String serviceId, String applicationId, String agentId, String metricGroupName, String metricName, String tag);

    List<FieldAttribute> getFields(String serviceId, String applicationId, String agentId, String metricGroupName, String metricName, String tag, List<String> fieldNameList);

    CompletableFuture<List<OtlpMetricChartResult>> getChartPoints(OtlpMetricChartQueryParameter chartQueryParameter);

    CompletableFuture<List<MetricPoint>> getChartPoints(OtlpMetricDataQueryParameter chartQueryParameter);

    String getSummary(OtlpMetricChartQueryParameter chartQueryParameter);

}
