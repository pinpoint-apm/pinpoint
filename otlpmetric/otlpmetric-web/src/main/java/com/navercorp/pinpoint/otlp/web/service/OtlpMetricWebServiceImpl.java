package com.navercorp.pinpoint.otlp.web.service;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSampler;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.otlp.common.definition.property.AggregationFunction;
import com.navercorp.pinpoint.otlp.common.definition.property.ChartType;
import com.navercorp.pinpoint.otlp.common.model.MetricType;
import com.navercorp.pinpoint.otlp.web.dao.OtlpMetricDao;
import com.navercorp.pinpoint.otlp.web.view.OtlpChartFieldView;
import com.navercorp.pinpoint.otlp.web.view.OtlpChartView;
import com.navercorp.pinpoint.otlp.web.view.OtlpChartViewBuilder;
import com.navercorp.pinpoint.otlp.web.vo.FieldAttribute;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricChartQueryParameter;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricChartResult;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricDataQueryParameter;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
public class OtlpMetricWebServiceImpl implements OtlpMetricWebService {
    private final Logger logger = LogManager.getLogger(this.getClass());
    public static final OtlpChartView EMPTY_CHART = OtlpChartViewBuilder.EMPTY_CHART_VIEW;
    private final TimeWindowSampler DEFAULT_TIME_WINDOW_SAMPLER = new TimeWindowSlotCentricSampler(30000L, 200);

    @NotNull private final OtlpMetricDao otlpMetricDao;


    public OtlpMetricWebServiceImpl(@Valid OtlpMetricDao otlpMetricDao) {
        this.otlpMetricDao = Objects.requireNonNull(otlpMetricDao, "otlpMetricDao");
    }

    @Deprecated
    @Override
    public List<String> getMetricGroupList(String tenantId, String serviceId, String applicationName, String agentId) {
        return otlpMetricDao.getMetricGroups(tenantId, serviceId, applicationName, agentId);
    }

    @Deprecated
    @Override
    public List<String> getMetricList(String tenantId, String serviceId, String applicationName, String agentId, String metricGroupName) {
        return otlpMetricDao.getMetrics(tenantId, serviceId, applicationName, agentId, metricGroupName);
    }

    @Deprecated
    @Override
    public List<String> getTags(String tenantId, String serviceId, String applicationId, String agentId, String metricGroupName, String metricName) {
        return otlpMetricDao.getTags(tenantId, serviceId, applicationId, agentId, metricGroupName, metricName);
    }


    @Override
    public OtlpChartView getMetricChartData(String tenantId, String serviceId, String applicationId, String agentId, String metricGroupName, String metricName, String tag, long from, long to) {
        List<FieldAttribute> fields = otlpMetricDao.getFields(serviceId, applicationId, agentId, metricGroupName, metricName, tag);
        if (fields.size() == 0) {
            return EMPTY_CHART;
        }

        List<String> tags = List.of(tag.split(","));

        MetricType chartType = fields.get(0).metricType();
        OtlpChartViewBuilder chartViewBuilder = OtlpChartViewBuilder.newBuilder(chartType);

        OtlpMetricChartQueryParameter.Builder builder =
                new OtlpMetricChartQueryParameter.Builder()
                        .setServiceId(serviceId)
                        .setApplicationId(applicationId)
                        .setAgentId(agentId)
                        .setMetricGroupName(metricGroupName)
                        .setMetricName(metricName)
                        .setTags(tags)
                        .setTimeWindow(new TimeWindow(Range.between(from, to), DEFAULT_TIME_WINDOW_SAMPLER));
        List<QueryResult<CompletableFuture<List<OtlpMetricChartResult>>, FieldAttribute>> queryResult = new ArrayList<>();

        for (FieldAttribute field : fields) {
            OtlpMetricChartQueryParameter chartQueryParameter = setupQueryParameter(builder, field);
            CompletableFuture<List<OtlpMetricChartResult>> chartPoints = otlpMetricDao.getChartPoints(chartQueryParameter);
            queryResult.add(new QueryResult<>(chartPoints, field));
        }

        for (QueryResult<CompletableFuture<List<OtlpMetricChartResult>>, FieldAttribute> result : queryResult) {
            CompletableFuture<List<OtlpMetricChartResult>> future = result.future();
            FieldAttribute key = result.key();

            try {
                List<OtlpMetricChartResult> otlpMetricChartResults = future.get();
                if (otlpMetricChartResults != null) {
                    OtlpChartFieldView chartFieldView = chartViewBuilder.add(key, otlpMetricChartResults);
                    if ((chartViewBuilder.hasSummaryField()) && (chartFieldView != null)) {
                        OtlpMetricChartQueryParameter chartQueryParameter = setupQueryParameter(builder, key);
                        String value = otlpMetricDao.getSummary(chartQueryParameter);
                        chartFieldView.setSummaryField(key.aggreFunc().name(), value);
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to get OTLP metric data for applicationID: {}, metric: {}.{}.{}", applicationId, metricGroupName, metricName, key.fieldName());
            }
        }
        return chartViewBuilder.build();
    }

    @Override
    public OtlpChartView getMetricData(String tenantId, String serviceId, String applicationName, String agentId, String metricGroupName, String metricName, String tags, List<String> fieldNameList, long from, long to, ChartType chartType, AggregationFunction aggregationFunction) {
        // TODO : (minwoo)
        // 3 Check for unnecessary parameters
        // 4 Process data based on aggregationfunction
        // 5 Generate view data based on the chartType parameter entered by the user
        List<FieldAttribute> fields = otlpMetricDao.getFields(serviceId, applicationName, agentId, metricGroupName, metricName, tags, fieldNameList);

        OtlpMetricDataQueryParameter.Builder builder =
                new OtlpMetricDataQueryParameter.Builder()
                        .setServiceId(serviceId)
                        .setApplicationId(applicationName)
                        .setAgentId(agentId)
                        .setMetricGroupName(metricGroupName)
                        .setMetricName(metricName)
                        .setTags(tags)
                        .setTimeWindow(new TimeWindow(Range.between(from, to), DEFAULT_TIME_WINDOW_SAMPLER));
        List<QueryResult<CompletableFuture<List<OtlpMetricChartResult>>, FieldAttribute>> queryResult = new ArrayList<>();

        for (FieldAttribute field : fields) {
            OtlpMetricDataQueryParameter chartQueryParameter = setupQueryParameter(builder, field);
            CompletableFuture<List<OtlpMetricChartResult>> chartPoints = otlpMetricDao.getChartPoints(chartQueryParameter);
            queryResult.add(new QueryResult<>(chartPoints, field));
        }

        // TODO : (minwoo) using the chartType of a parameter
        MetricType metricType = fields.get(0).metricType();
        OtlpChartViewBuilder chartViewBuilder = OtlpChartViewBuilder.newBuilder(metricType);

        for (QueryResult<CompletableFuture<List<OtlpMetricChartResult>>, FieldAttribute> result : queryResult) {
            CompletableFuture<List<OtlpMetricChartResult>> future = result.future();
            FieldAttribute key = result.key();

            try {
                List<OtlpMetricChartResult> otlpMetricChartResults = future.get();
                if (otlpMetricChartResults != null) {
                    OtlpChartFieldView chartFieldView = chartViewBuilder.add(key, otlpMetricChartResults);
                    if ((chartViewBuilder.hasSummaryField()) && (chartFieldView != null)) {
                    // TODO : (minwoo) Get summary data

//                        OtlpMetricDataQueryParameter chartQueryParameter = setupQueryParameter(builder, key);
//                        String value = otlpMetricDao.getSummary(chartQueryParameter);
//                        chartFieldView.setSummaryField(key.aggreFunc().name(), value);
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to get OTLP metric data for applicationID: {}, metric: {}.{}.{}", applicationName, metricGroupName, metricName, key.fieldName());
            }
        }

        return chartViewBuilder.build();

    }


    @Deprecated
    private OtlpMetricChartQueryParameter setupQueryParameter(OtlpMetricChartQueryParameter.Builder builder, FieldAttribute field) {
        return builder.setFieldName(field.fieldName())
                .setAggreFunc(field.aggreFunc())
                .setDataType(field.dataType())
                .setVersion(field.version())
                .build();
    }

    private OtlpMetricDataQueryParameter setupQueryParameter(OtlpMetricDataQueryParameter.Builder builder, FieldAttribute field) {
        return builder.setFieldName(field.fieldName())
                .setAggreFunc(field.aggreFunc())
                .setDataType(field.dataType())
                .setVersion(field.version())
                .build();
    }


    // TODO: duplicate record
    private record QueryResult<E, K>(CompletableFuture<List<OtlpMetricChartResult>> future, K key) {}

}
