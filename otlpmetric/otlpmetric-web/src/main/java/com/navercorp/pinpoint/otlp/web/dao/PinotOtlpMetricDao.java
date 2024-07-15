package com.navercorp.pinpoint.otlp.web.dao;

import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSampler;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.otlp.common.model.DataType;
import com.navercorp.pinpoint.otlp.web.view.OtlpChartView;
import com.navercorp.pinpoint.otlp.web.view.OtlpChartViewBuilder;
import com.navercorp.pinpoint.otlp.web.vo.*;
import com.navercorp.pinpoint.pinot.mybatis.PinotAsyncTemplate;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Repository
public class PinotOtlpMetricDao implements OtlpMetricDao {
    private final TimeWindowSampler DEFAULT_TIME_WINDOW_SAMPLER = new TimeWindowSlotCentricSampler(30000L, 200);
    public static final OtlpChartView EMPTY_CHART = OtlpChartViewBuilder.EMPTY_CHART_VIEW;
    private static final String NAMESPACE = PinotOtlpMetricDao.class.getName() + ".";
    private final SqlSessionTemplate syncTemplate;
    private final PinotAsyncTemplate asyncTemplate;

    public PinotOtlpMetricDao(@Qualifier("otlpMetricPinotSessionTemplate") SqlSessionTemplate syncTemplate,
                              @Qualifier("otlpMetricPinotAsyncTemplate") PinotAsyncTemplate asyncTemplate) {
        this.syncTemplate = Objects.requireNonNull(syncTemplate, "syncTemplate");
        this.asyncTemplate = Objects.requireNonNull(asyncTemplate, "asyncTemplate");
    }

    @Override
    public List<String> getMetricGroups(String tenantId, String serviceId, String applicationId, String agentId) {
        List<String> metricGroups = this.syncTemplate.selectList(NAMESPACE + "getMetricGroups",
                new OtlpMetricGroupsQueryParam(serviceId, applicationId, agentId));
        return metricGroups;
    }

    @Override
    public List<String> getMetrics(String tenantId, String serviceId, String applicationId, String agentId, String metricGroupName) {
        // TODO:
        // 1. Check mysql if metrics to be displayed are specified
        // 2. if no data in mysql, query pinot to get all metric names
        List<String> metrics = this.syncTemplate.selectList(NAMESPACE + "getMetricNames",
                new OtlpMetricNamesQueryParam(serviceId, applicationId, agentId, metricGroupName));
        return metrics;
    }

    @Override
    public List<String> getTags(String tenantId, String serviceId, String applicationId, String agentId, String metricGroupName, String metricName) {
        OtlpMetricDetailsQueryParam queryParam = new OtlpMetricDetailsQueryParam(serviceId, applicationId, agentId, metricGroupName, metricName, null);

        List<String> tags = this.syncTemplate.selectList(NAMESPACE + "getTags", queryParam);
        return tags;
    }

    @Override
    public List<FieldAttribute> getFields(String serviceId, String applicationId, String agentId, String metricGroupName, String metricName, String tag) {
        OtlpMetricDetailsQueryParam queryParam = new OtlpMetricDetailsQueryParam(serviceId, applicationId, agentId, metricGroupName, metricName, tag);
        return this.syncTemplate.selectList(NAMESPACE + "getFields", queryParam);
    }

    @Override
    public CompletableFuture<List<OtlpMetricChartResult>> getChartPoints(OtlpMetricChartQueryParameter chartQueryParameter) {
        if (chartQueryParameter.getDataType() == DataType.LONG) {
            return asyncTemplate.selectList(NAMESPACE + "getLongChartData", chartQueryParameter);
        } else {
            return asyncTemplate.selectList(NAMESPACE + "getDoubleChartData", chartQueryParameter);
        }
    }

    @Override
    public String getSummary(OtlpMetricChartQueryParameter chartQueryParameter) {
        if (chartQueryParameter.getDataType() == DataType.LONG) {
            return syncTemplate.selectOne(NAMESPACE + "getLongSummary", chartQueryParameter);
        } else {
            return syncTemplate.selectOne(NAMESPACE + "getDoubleSummary", chartQueryParameter);
        }
    }


}
