package com.navercorp.pinpoint.otlp.web.service;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSampler;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.metric.common.dao.TableNameManager;
import com.navercorp.pinpoint.metric.common.util.TimeUtils;
import com.navercorp.pinpoint.otlp.common.model.MetricPoint;
import com.navercorp.pinpoint.otlp.common.model.MetricType;
import com.navercorp.pinpoint.otlp.common.util.DoubleUncollectedDataCreator;
import com.navercorp.pinpoint.otlp.common.util.TimeSeriesBuilder;
import com.navercorp.pinpoint.otlp.common.web.defined.PrimaryForFieldAndTagRelation;
import com.navercorp.pinpoint.otlp.common.web.definition.property.AggregationFunction;
import com.navercorp.pinpoint.otlp.common.web.definition.property.ChartType;
import com.navercorp.pinpoint.otlp.common.web.model.MetricValue;
import com.navercorp.pinpoint.otlp.web.config.pinot.OtlpMetricPinotTableProperties;
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
import java.util.stream.Collectors;

@Service
public class OtlpMetricWebServiceImpl implements OtlpMetricWebService {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private static final String EMPTY_STRING = "";

    @Deprecated
    public static final OtlpChartView EMPTY_CHART = OtlpChartViewBuilder.EMPTY_CHART_VIEW;
    @Deprecated
    private final TimeWindowSampler DEFAULT_TIME_WINDOW_SAMPLER = new TimeWindowSlotCentricSampler(30000L, 200);

    @NotNull private final OtlpMetricDao otlpMetricDao;
    @NotNull private final TableNameManager doubleTopicNameManager;
    @NotNull private final TableNameManager longTopicNameManager;


    public OtlpMetricWebServiceImpl(@Valid OtlpMetricDao otlpMetricDao, OtlpMetricPinotTableProperties otlpMetricPinotTableProperties) {
        this.otlpMetricDao = Objects.requireNonNull(otlpMetricDao, "otlpMetricDao");

        Objects.requireNonNull(otlpMetricPinotTableProperties, "otlpMetricWebProperties");
        this.doubleTopicNameManager = new TableNameManager(otlpMetricPinotTableProperties.getDoubleTopicPrefix(), otlpMetricPinotTableProperties.getDoubleTopicPaddingLength(), otlpMetricPinotTableProperties.getDoubleTopicCount());
        this.longTopicNameManager = new TableNameManager(otlpMetricPinotTableProperties.getLongTopicPrefix(), otlpMetricPinotTableProperties.getLongTopicPaddingLength(), otlpMetricPinotTableProperties.getLongTopicCount());
    }

    @Deprecated
    @Override
    public List<String> getMetricGroupList(String tenantId, String serviceName, String applicationName, String agentId) {
        return otlpMetricDao.getMetricGroups(tenantId, serviceName, applicationName, agentId);
    }

    @Deprecated
    @Override
    public List<String> getMetricList(String tenantId, String serviceName, String applicationName, String agentId, String metricGroupName) {
        return otlpMetricDao.getMetrics(tenantId, serviceName, applicationName, agentId, metricGroupName);
    }

    @Deprecated
    @Override
    public List<String> getTags(String tenantId, String serviceName, String applicationName, String agentId, String metricGroupName, String metricName) {
        return otlpMetricDao.getTags(tenantId, serviceName, applicationName, agentId, metricGroupName, metricName);
    }

    @Deprecated
    @Override
    public OtlpChartView getMetricChartData(String tenantId, String serviceName, String applicationName, String agentId, String metricGroupName, String metricName, String tag, long from, long to) {
        List<FieldAttribute> fields = otlpMetricDao.getFields(serviceName, applicationName, agentId, metricGroupName, metricName, tag);
        if (fields.size() == 0) {
            return EMPTY_CHART;
        }

        List<String> tags = List.of(tag.split(","));

        MetricType chartType = fields.get(0).metricType();
        OtlpChartViewBuilder chartViewBuilder = OtlpChartViewBuilder.newBuilder(chartType);

        OtlpMetricChartQueryParameter.Builder builder =
                new OtlpMetricChartQueryParameter.Builder()
                        .setServiceName(serviceName)
                        .setApplicationName(applicationName)
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
                logger.warn("Failed to get OTLP metric data for applicationName: {}, metric: {}.{}.{}", applicationName, metricGroupName, metricName, key.fieldName());
            }
        }
        return chartViewBuilder.legacyBuild();
    }

    @Deprecated
    private OtlpMetricChartQueryParameter setupQueryParameter(OtlpMetricChartQueryParameter.Builder builder, FieldAttribute field) {
        return builder.setFieldName(field.fieldName())
                .setAggregationFunction(field.aggregationFunction())
                .setDataType(field.dataType())
                .setVersion(field.version())
                .build();
    }

    public MetricData getMetricData(String tenantId, String serviceName, String applicationName, String agentId, String metricGroupName, String metricName, PrimaryForFieldAndTagRelation primaryForFieldAndTagRelation, List<String> tagGroupList, List<String> fieldNameList, ChartType chartType, AggregationFunction aggregationFunction, TimeWindow timeWindow) {
        List<FieldAttribute> fields = otlpMetricDao.getFields(serviceName, applicationName, agentId, metricGroupName, metricName, tagGroupList, fieldNameList);

        if(fields.size() == 0) {
            return createEmptyMetricData(timeWindow, chartType);
        }

        OtlpMetricDataQueryParameter.Builder builder =
                new OtlpMetricDataQueryParameter.Builder()
                        .setServiceName(serviceName)
                        .setApplicationName(applicationName)
                        .setDoubleTableNameManager(doubleTopicNameManager)
                        .setLongTableNameManager(longTopicNameManager)
                        .setAgentId(agentId)
                        .setMetricGroupName(metricGroupName)
                        .setMetricName(metricName)
                        .setAggregationFunction(aggregationFunction)
                        .setTimeWindow(timeWindow);
        List<QueryResult<CompletableFuture<List<MetricPoint>>, OtlpMetricDataQueryParameter>> queryResult = new ArrayList<>();

        List<OtlpMetricDataQueryParameter> chartQueryParameterList = setupQueryParameterList(builder, fields, tagGroupList, primaryForFieldAndTagRelation);
        for (OtlpMetricDataQueryParameter chartQueryParameter : chartQueryParameterList) {
            CompletableFuture<List<MetricPoint>> chartPoints = otlpMetricDao.getChartPoints(chartQueryParameter);
            queryResult.add(new QueryResult<>(chartPoints, chartQueryParameter));
        }

        List<Long> timeStampList = TimeUtils.createTimeStampList(timeWindow);
        // TODO: (minwoo) set unit data
        MetricData metricData = new MetricData(timeStampList, chartType, fields.get(0).unit());

            for (QueryResult<CompletableFuture<List<MetricPoint>>, OtlpMetricDataQueryParameter> result : queryResult) {
                CompletableFuture<List<MetricPoint>> future = result.future();
                OtlpMetricDataQueryParameter chartQueryParameter = result.key();
                try {
                    List<MetricPoint> meticDataList = future.get();
                    addMetricValue(timeWindow, meticDataList, metricData, getLegendName(chartQueryParameter, primaryForFieldAndTagRelation), chartQueryParameter.getVersion());
                } catch(Exception e){
                    logger.warn("Failed to get OTLP metric data for applicationName: {}, metricGroup: {}, metricName {}, chartQueryParameter {}", applicationName, metricGroupName, metricName, chartQueryParameter, e);
                }
            }

        return metricData;
    }

    private MetricData createEmptyMetricData(TimeWindow timeWindow, ChartType chartType) {
        return new MetricData(TimeUtils.createTimeStampList(timeWindow), chartType, EMPTY_STRING, "There is no metadata for the metric query.");
    }

    private String getLegendName(OtlpMetricDataQueryParameter chartQueryParameter, PrimaryForFieldAndTagRelation primaryForFieldAndTagRelation) {
        switch (primaryForFieldAndTagRelation) {
            case FIELD:
                return chartQueryParameter.getRawTags();
            case TAG:
                return chartQueryParameter.getFieldName();
            default:
                throw new IllegalArgumentException("Unknown primaryForFieldAndTagRelation: " + primaryForFieldAndTagRelation);
        }
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

    private List<OtlpMetricDataQueryParameter> setupQueryParameterList(OtlpMetricDataQueryParameter.Builder builder, List<FieldAttribute> fields, List<String> tagGroupList, PrimaryForFieldAndTagRelation primaryForFieldAndTagRelation) {
        switch (primaryForFieldAndTagRelation) {
            case FIELD:
                return setupQueryParameter(builder, fields.get(0), tagGroupList);
            case TAG:
                return setupQueryParameter(builder, fields, tagGroupList.get(0));
            default:
                throw new IllegalArgumentException("Unknown primaryForFieldAndTagRelation: " + primaryForFieldAndTagRelation);
        }

    }

    private List<OtlpMetricDataQueryParameter> setupQueryParameter(OtlpMetricDataQueryParameter.Builder builder, List<FieldAttribute> fields, String tagGroup) {
        List<OtlpMetricDataQueryParameter> queryParameterList = new ArrayList<>();

        for (FieldAttribute field : fields) {
            OtlpMetricDataQueryParameter otlpMetricDataQueryParameter = builder.setFieldName(field.fieldName())
                    .setDataType(field.dataType())
                    .setVersion(field.version())
                    .setTags(tagGroup)
                    .build();
            queryParameterList.add(otlpMetricDataQueryParameter);
        }

        return queryParameterList;
    }

    private List<OtlpMetricDataQueryParameter> setupQueryParameter(OtlpMetricDataQueryParameter.Builder builder, FieldAttribute field, List<String> tagGroupList) {
        List<OtlpMetricDataQueryParameter> queryParameterList = new ArrayList<>();

        for (String tagGroup : tagGroupList) {
            OtlpMetricDataQueryParameter otlpMetricDataQueryParameter = builder.setFieldName(field.fieldName())
                    .setDataType(field.dataType())
                    .setVersion(field.version())
                    .setTags(tagGroup)
                    .build();
            queryParameterList.add(otlpMetricDataQueryParameter);
        }

        return queryParameterList;
    }

    @Deprecated
    // TODO: duplicate record
    private record LegacyQueryResult<E, K>(CompletableFuture<List<OtlpMetricChartResult>> future, K key) {}

    private record QueryResult<E, K>(CompletableFuture<List<MetricPoint>> future, K key) {}
}
