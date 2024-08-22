package com.navercorp.pinpoint.otlp.web.service;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSampler;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.metric.common.model.chart.SystemMetricPoint;
import com.navercorp.pinpoint.metric.common.util.TimeUtils;
import com.navercorp.pinpoint.otlp.common.model.MetricType;
import com.navercorp.pinpoint.otlp.common.model.MetricPoint;
import com.navercorp.pinpoint.otlp.common.util.DoubleUncollectedDataCreator;
import com.navercorp.pinpoint.otlp.common.util.TimeSeriesBuilder;
import com.navercorp.pinpoint.otlp.common.web.definition.property.AggregationFunction;
import com.navercorp.pinpoint.otlp.common.web.definition.property.ChartType;
import com.navercorp.pinpoint.otlp.common.web.model.MetricValue;
import com.navercorp.pinpoint.otlp.web.dao.OtlpMetricDao;
import com.navercorp.pinpoint.otlp.web.view.legacy.OtlpChartFieldView;
import com.navercorp.pinpoint.otlp.web.view.legacy.OtlpChartView;
import com.navercorp.pinpoint.otlp.web.view.legacy.OtlpChartViewBuilder;
import com.navercorp.pinpoint.otlp.web.vo.FieldAttribute;
import com.navercorp.pinpoint.otlp.web.vo.MetricData;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricChartQueryParameter;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricChartResult;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricDataQueryParameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class OtlpMetricWebServiceImpl implements OtlpMetricWebService {
    private final Logger logger = LogManager.getLogger(this.getClass());
    @Deprecated
    public static final OtlpChartView EMPTY_CHART = OtlpChartViewBuilder.EMPTY_CHART_VIEW;
    @Deprecated
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


    @Deprecated
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
        List<LegacyQueryResult<CompletableFuture<List<OtlpMetricChartResult>>, FieldAttribute>> legacyQueryResult = new ArrayList<>();

        for (FieldAttribute field : fields) {
            OtlpMetricChartQueryParameter chartQueryParameter = setupQueryParameter(builder, field);
            CompletableFuture<List<OtlpMetricChartResult>> chartPoints = otlpMetricDao.getChartPoints(chartQueryParameter);
            legacyQueryResult.add(new LegacyQueryResult<>(chartPoints, field));
        }

        for (LegacyQueryResult<CompletableFuture<List<OtlpMetricChartResult>>, FieldAttribute> result : legacyQueryResult) {
            CompletableFuture<List<OtlpMetricChartResult>> future = result.future();
            FieldAttribute key = result.key();

            try {
                List<OtlpMetricChartResult> otlpMetricChartResults = future.get();
                if (otlpMetricChartResults != null) {
                    OtlpChartFieldView chartFieldView = chartViewBuilder.add(key, otlpMetricChartResults);
                    if ((chartViewBuilder.hasSummaryField()) && (chartFieldView != null)) {
                        OtlpMetricChartQueryParameter chartQueryParameter = setupQueryParameter(builder, key);
                        String value = otlpMetricDao.getSummary(chartQueryParameter);
                        chartFieldView.setSummaryField(key.aggregationFunction().getAggregationFunctionName(), value);
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to get OTLP metric data for applicationID: {}, metric: {}.{}.{}", applicationId, metricGroupName, metricName, key.fieldName());
            }
        }
        return chartViewBuilder.legacyBuild();
    }

    @Override
    public MetricData getMetricData(String tenantId, String serviceId, String applicationName, String agentId, String metricGroupName, String metricName, String tags, List<String> fieldNameList, ChartType chartType, AggregationFunction aggregationFunction, TimeWindow timeWindow) {
        List<FieldAttribute> fields = otlpMetricDao.getFields(serviceId, applicationName, agentId, metricGroupName, metricName, tags, fieldNameList);

        OtlpMetricDataQueryParameter.Builder builder =
                new OtlpMetricDataQueryParameter.Builder()
                        .setServiceId(serviceId)
                        .setApplicationId(applicationName)
                        .setAgentId(agentId)
                        .setMetricGroupName(metricGroupName)
                        .setMetricName(metricName)
                        .setTags(tags)
                        .setAggregationFunction(aggregationFunction)
                        .setTimeWindow(timeWindow);
        List<QueryResult<CompletableFuture<List<MetricPoint>>, FieldAttribute>> queryResult = new ArrayList<>();

        for (FieldAttribute field : fields) {
            OtlpMetricDataQueryParameter chartQueryParameter = setupQueryParameter(builder, field);
            CompletableFuture<List<MetricPoint>> chartPoints = otlpMetricDao.getChartPoints(chartQueryParameter);
            queryResult.add(new QueryResult<>(chartPoints, field));
        }

        List<Long> timeStampList = TimeUtils.createTimeStampList(timeWindow);
        // TODO: (minwoo) set unit data
        MetricData metricData = new MetricData(timeStampList, chartType, fields.get(0).unit());

            for (QueryResult<CompletableFuture<List<MetricPoint>>, FieldAttribute> result : queryResult) {
                CompletableFuture<List<MetricPoint>> future = result.future();
                FieldAttribute fieldAttribute = result.key();
                try {
                    List<MetricPoint> meticDataList = future.get();
                    addMetricValue(timeWindow, meticDataList, metricData, fieldAttribute.fieldName(), fieldAttribute.version());
                } catch(Exception e){
                    logger.warn("Failed to get OTLP metric data for applicationID: {}, metricGroup: {}, metricName {}, FieldAttribute", applicationName, metricGroupName, metricName, fieldAttribute, e);
                }
            }

        return metricData;
    }

    private void addMetricValue(TimeWindow timeWindow, List<MetricPoint> meticDataList, MetricData metricData, String legendName, String version){
        TimeSeriesBuilder timeSeriesBuilder = new TimeSeriesBuilder(timeWindow, DoubleUncollectedDataCreator.UNCOLLECTED_DATA_CREATOR);
        List<MetricPoint> metricPointList = timeSeriesBuilder.build(meticDataList);
        List<Number> valueList = metricPointList.stream().map(MetricPoint::getYVal).collect(Collectors.toList());

        metricData.addMetricValue(new MetricValue(legendName, valueList, version));

//                if (otlpMetricChartResults != null) {
        // TODO : (minwoo) Get summary data
        //                    if ((metricData.hasSummaryField()) && (chartFieldView != null)) {


//                        OtlpMetricDataQueryParameter chartQueryParameter = setupQueryParameter(builder, key);
//                        String value = otlpMetricDao.getSummary(chartQueryParameter);
//                        chartFieldView.setSummaryField(key.aggreFunc().name(), value);
//                    }
//                }
    }


    @Deprecated
    private OtlpMetricChartQueryParameter setupQueryParameter(OtlpMetricChartQueryParameter.Builder builder, FieldAttribute field) {
        return builder.setFieldName(field.fieldName())
                .setAggregationFunction(field.aggregationFunction())
                .setDataType(field.dataType())
                .setVersion(field.version())
                .build();
    }

    private OtlpMetricDataQueryParameter setupQueryParameter(OtlpMetricDataQueryParameter.Builder builder, FieldAttribute field) {
        return builder.setFieldName(field.fieldName())
                .setDataType(field.dataType())
                .setVersion(field.version())
                .build();
    }


    @Deprecated
    // TODO: duplicate record
    private record LegacyQueryResult<E, K>(CompletableFuture<List<OtlpMetricChartResult>> future, K key) {}

    private record QueryResult<E, K>(CompletableFuture<List<MetricPoint>> future, K key) {}
}
