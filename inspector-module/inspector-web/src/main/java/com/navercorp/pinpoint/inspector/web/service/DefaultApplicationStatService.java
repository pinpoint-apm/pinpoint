package com.navercorp.pinpoint.inspector.web.service;

import com.navercorp.pinpoint.inspector.web.dao.ApplicationStatDao;
import com.navercorp.pinpoint.inspector.web.definition.AggregationFunction;
import com.navercorp.pinpoint.inspector.web.definition.Mappings;
import com.navercorp.pinpoint.inspector.web.definition.MetricDefinition;
import com.navercorp.pinpoint.inspector.web.definition.YMLInspectorManager;
import com.navercorp.pinpoint.inspector.web.definition.metric.MetricPostProcessor;
import com.navercorp.pinpoint.inspector.web.definition.metric.MetricPreProcessor;
import com.navercorp.pinpoint.inspector.web.definition.metric.MetricProcessorManager;
import com.navercorp.pinpoint.inspector.web.definition.metric.field.Field;
import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricData;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricGroupData;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricValue;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.model.TimeWindow;
import com.navercorp.pinpoint.metric.common.model.chart.AvgMinMaxMetricPoint;
import com.navercorp.pinpoint.metric.common.model.chart.AvgMinMetricPoint;
import com.navercorp.pinpoint.metric.common.model.chart.MinMaxMetricPoint;
import com.navercorp.pinpoint.metric.common.model.chart.Point;
import com.navercorp.pinpoint.metric.common.model.chart.SystemMetricPoint;
import com.navercorp.pinpoint.metric.common.util.DoubleUncollectedDataCreator;
import com.navercorp.pinpoint.metric.common.util.TimeSeriesBuilder;
import com.navercorp.pinpoint.metric.common.util.TimeUtils;
import com.navercorp.pinpoint.metric.common.util.UncollectedDataCreator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class DefaultApplicationStatService implements ApplicationStatService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final YMLInspectorManager ymlInspectorManager;
    private final MetricProcessorManager metricProcessorManager;
    private final ApplicationStatDao applicationStatDao;
    private final ApplicationStatDao applicationStatDaoV2;

    public DefaultApplicationStatService(@Qualifier("pinotApplicationStatDaoV2")ApplicationStatDao applicationStatDaoV2, @Qualifier("pinotApplicationStatDao")ApplicationStatDao applicationStatDao, @Qualifier("applicationInspectorDefinition") Mappings applicationInspectorDefinition, MetricProcessorManager metricProcessorManager) {
        this.applicationStatDao = Objects.requireNonNull(applicationStatDao, "applicationStatDao");
        this.applicationStatDaoV2 = Objects.requireNonNull(applicationStatDaoV2, "applicationStatDaoV2");
        Objects.requireNonNull(applicationInspectorDefinition, "applicationInspectorDefinition");
        this.ymlInspectorManager = new YMLInspectorManager(applicationInspectorDefinition);
        this.metricProcessorManager = Objects.requireNonNull(metricProcessorManager, "metricProcessorManager");
    }

    @Override
    public InspectorMetricData selectApplicationStat(InspectorDataSearchKey inspectorDataSearchKey, TimeWindow timeWindow) {
        MetricDefinition metricDefinition = ymlInspectorManager.findElementOfBasicGroup(inspectorDataSearchKey.getMetricDefinitionId());

        List<QueryResult> queryResults;
        if (inspectorDataSearchKey.getVersion() == 2) {
            queryResults =  selectAll2(inspectorDataSearchKey, metricDefinition);
        } else {
            queryResults =  selectAll(inspectorDataSearchKey, metricDefinition);
        }


        List<InspectorMetricValue> metricValueList = new ArrayList<>(queryResults.size());
        try {
            for (QueryResult result : queryResults) {
                Class<?> resultType = result.resultType();
                if (resultType.equals(AvgMinMaxMetricPoint.class)) {
                    List<AvgMinMaxMetricPoint<Double>> doubleList = (List<AvgMinMaxMetricPoint<Double>>) result.future().get();
                    metricValueList.addAll(splitAvgMinMax(timeWindow, result.field(), doubleList, DoubleUncollectedDataCreator.UNCOLLECTED_DATA_CREATOR));
                } else if (resultType.equals(AvgMinMetricPoint.class)) {
                    List<AvgMinMetricPoint<Double>> doubleList = (List<AvgMinMetricPoint<Double>>) result.future().get();
                    metricValueList.addAll(splitAvgMin(timeWindow, result.field(), doubleList, DoubleUncollectedDataCreator.UNCOLLECTED_DATA_CREATOR));
                }else if (resultType.equals(MinMaxMetricPoint.class)) {
                    List<MinMaxMetricPoint<Double>> doubleList = (List<MinMaxMetricPoint<Double>>) result.future().get();
                    metricValueList.addAll(splitMinMax(timeWindow, result.field(), doubleList, DoubleUncollectedDataCreator.UNCOLLECTED_DATA_CREATOR));
                } else if (resultType.equals(SystemMetricPoint.class)) {
                    List<SystemMetricPoint<Double>> doubleList = (List<SystemMetricPoint<Double>>) result.future().get();
                    metricValueList.add(createInspectorMetricValue(timeWindow, result.field(), doubleList, DoubleUncollectedDataCreator.UNCOLLECTED_DATA_CREATOR));
                } else {
                    throw new RuntimeException("not support result type : " + result.resultType());
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        List<InspectorMetricValue> processedMetricValueList = postprocessMetricData(metricDefinition, metricValueList);
        List<Long> timeStampList = TimeUtils.createTimeStampList(timeWindow);
        return new InspectorMetricData(metricDefinition.getTitle(), timeStampList, processedMetricValueList);
    }

    @Override
    public InspectorMetricGroupData selectApplicationStatWithGrouping(InspectorDataSearchKey inspectorDataSearchKey, TimeWindow timeWindow) {
        MetricDefinition metricDefinition = ymlInspectorManager.findElementOfBasicGroup(inspectorDataSearchKey.getMetricDefinitionId());
        MetricDefinition newMetricDefinition = preProcess(inspectorDataSearchKey, metricDefinition);

        List<QueryResult> queryResults;
        if (inspectorDataSearchKey.getVersion() == 2) {
            queryResults =  selectAll2(inspectorDataSearchKey, newMetricDefinition);
        } else {
            queryResults =  selectAll(inspectorDataSearchKey, newMetricDefinition);
        }

        List<InspectorMetricValue> metricValueList = new ArrayList<>(newMetricDefinition.getFields().size());

        try {
            for (QueryResult result : queryResults) {
                Class<?> resultType = result.resultType();
                if (resultType.equals(AvgMinMaxMetricPoint.class)) {
                    List<AvgMinMaxMetricPoint<Double>> doubleList = (List<AvgMinMaxMetricPoint<Double>>) result.future().get();
                    metricValueList.addAll(splitAvgMinMax(timeWindow, result.field(), doubleList, DoubleUncollectedDataCreator.UNCOLLECTED_DATA_CREATOR));
                } else if (resultType.equals(AvgMinMetricPoint.class)) {
                    List<AvgMinMetricPoint<Double>> doubleList = (List<AvgMinMetricPoint<Double>>) result.future().get();
                    metricValueList.addAll(splitAvgMin(timeWindow, result.field(), doubleList, DoubleUncollectedDataCreator.UNCOLLECTED_DATA_CREATOR));
                }else if (resultType.equals(MinMaxMetricPoint.class)) {
                    List<MinMaxMetricPoint<Double>> doubleList = (List<MinMaxMetricPoint<Double>>) result.future().get();
                    metricValueList.addAll(splitMinMax(timeWindow, result.field(), doubleList, DoubleUncollectedDataCreator.UNCOLLECTED_DATA_CREATOR));
                } else if (resultType.equals(SystemMetricPoint.class)) {
                    List<SystemMetricPoint<Double>> doubleList = (List<SystemMetricPoint<Double>>) result.future().get();
                    metricValueList.add(createInspectorMetricValue(timeWindow, result.field(), doubleList, DoubleUncollectedDataCreator.UNCOLLECTED_DATA_CREATOR));
                } else {
                    throw new RuntimeException("not support result type : " + result.resultType());
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        List<InspectorMetricValue> processedMetricValueList = postprocessMetricData(newMetricDefinition, metricValueList);
        List<Long> timeStampList = TimeUtils.createTimeStampList(timeWindow);
        Map<List<Tag>,List<InspectorMetricValue>> metricValueGroups = groupingMetricValue(processedMetricValueList, metricDefinition);

        return new InspectorMetricGroupData(metricDefinition.getTitle(), timeStampList, metricValueGroups);
    }

    private Map<List<Tag>,List<InspectorMetricValue>> groupingMetricValue(List<InspectorMetricValue> processedMetricValueList, MetricDefinition metricDefinition) {
        switch (metricDefinition.getGroupingRule()) {
            case TAG:
                return processedMetricValueList.stream().collect(Collectors.groupingBy(InspectorMetricValue::getTagList));
            default:
                throw new UnsupportedOperationException("not supported grouping rule : " + metricDefinition.getGroupingRule());
        }
    }

    private MetricDefinition preProcess(InspectorDataSearchKey inspectorDataSearchKey, MetricDefinition metricDefinition) {
        MetricPreProcessor metricPreProcessor = metricProcessorManager.getPreProcessor(metricDefinition.getPreProcess());
        return metricPreProcessor.preProcess(inspectorDataSearchKey, metricDefinition);
    }

    private List<InspectorMetricValue> postprocessMetricData(MetricDefinition metricDefinition, List<InspectorMetricValue> metricValueList) {
        MetricPostProcessor postProcessor = metricProcessorManager.getPostProcessor(metricDefinition.getPostProcess());
        return postProcessor.postProcess(metricValueList);
    }

    private InspectorMetricValue createInspectorMetricValue(TimeWindow timeWindow, Field field,
                                                            List<SystemMetricPoint<Double>> sampledSystemMetricDataList,
                                                            UncollectedDataCreator<Double> uncollectedDataCreator) {
        TimeSeriesBuilder<Double> builder = new TimeSeriesBuilder<>(timeWindow, uncollectedDataCreator);
        List<SystemMetricPoint<Double>> filledSystemMetricDataList = builder.build(sampledSystemMetricDataList);

        List<Double> valueList = filledSystemMetricDataList.stream()
                .map(SystemMetricPoint::getYVal)
                .collect(Collectors.toList());

        return new InspectorMetricValue(field.getFieldAlias(), field.getTags(), field.getChartType(), field.getUnit(), valueList);
    }

    private List<InspectorMetricValue> splitMinMax(TimeWindow timeWindow, Field field, List<MinMaxMetricPoint<Double>> doubleList, UncollectedDataCreator<Double> uncollectedDataCreator) {
        TimeSeriesBuilder<Double> builder = new TimeSeriesBuilder<>(timeWindow, uncollectedDataCreator);
        List<MinMaxMetricPoint<Double>> filledSystemMetricDataList = builder.buildForMinMaxMetricPointList(doubleList);

        List<Double> minValueList = new ArrayList<>(filledSystemMetricDataList.size());
        List<Double> maxValueList = new ArrayList<>(filledSystemMetricDataList.size());

        for (MinMaxMetricPoint<Double> avgMinMaxMetricPoint : filledSystemMetricDataList) {
            minValueList.add(avgMinMaxMetricPoint.getMinValue());
            maxValueList.add(avgMinMaxMetricPoint.getMaxValue());
        }

        List<InspectorMetricValue> inspectorMetricValueList = new ArrayList<>(2);
        inspectorMetricValueList.add(new InspectorMetricValue("MIN", field.getTags(), field.getChartType(), field.getUnit(), minValueList));
        inspectorMetricValueList.add(new InspectorMetricValue("MAX", field.getTags(), field.getChartType(), field.getUnit(), maxValueList));
        return inspectorMetricValueList;
    }

    private Collection<InspectorMetricValue> splitAvgMin(TimeWindow timeWindow, Field field, List<AvgMinMetricPoint<Double>> doubleList, UncollectedDataCreator<Double> uncollectedDataCreator) {
        TimeSeriesBuilder<Double> builder = new TimeSeriesBuilder<>(timeWindow, uncollectedDataCreator);
        List<AvgMinMetricPoint<Double>> filledSystemMetricDataList = builder.buildForAvgMinMetricPointList(doubleList);

        List<Double> avgValueList = new ArrayList<>(filledSystemMetricDataList.size());
        List<Double> minValueList = new ArrayList<>(filledSystemMetricDataList.size());

        for (AvgMinMetricPoint<Double> avgMinMetricPoint : filledSystemMetricDataList) {
            avgValueList.add(avgMinMetricPoint.getAvgValue());
            minValueList.add(avgMinMetricPoint.getMinValue());

        }

        List<InspectorMetricValue> inspectorMetricValueList = new ArrayList<>(2);
        inspectorMetricValueList.add(new InspectorMetricValue("AVG", field.getTags(), field.getChartType(), field.getUnit(), avgValueList));
        inspectorMetricValueList.add(new InspectorMetricValue("MIN", field.getTags(), field.getChartType(), field.getUnit(), minValueList));
        return inspectorMetricValueList;
    }

    private List<InspectorMetricValue> splitAvgMinMax(TimeWindow timeWindow, Field field, List<AvgMinMaxMetricPoint<Double>> doubleList, UncollectedDataCreator<Double> uncollectedDataCreator) {
        TimeSeriesBuilder<Double> builder = new TimeSeriesBuilder<>(timeWindow, uncollectedDataCreator);
        List<AvgMinMaxMetricPoint<Double>> filledSystemMetricDataList = builder.buildForAvgMinMaxMetricPointList(doubleList);

        List<Double> avgValueList = new ArrayList<>(filledSystemMetricDataList.size());
        List<Double> minValueList = new ArrayList<>(filledSystemMetricDataList.size());
        List<Double> maxValueList = new ArrayList<>(filledSystemMetricDataList.size());

        for (AvgMinMaxMetricPoint<Double> avgMinMaxMetricPoint : filledSystemMetricDataList) {
            avgValueList.add(avgMinMaxMetricPoint.getAvgValue());
            minValueList.add(avgMinMaxMetricPoint.getMinValue());
            maxValueList.add(avgMinMaxMetricPoint.getMaxValue());
        }

        List<InspectorMetricValue> inspectorMetricValueList = new ArrayList<>(3);
        inspectorMetricValueList.add(new InspectorMetricValue("AVG", field.getTags(), field.getChartType(), field.getUnit(), avgValueList));
        inspectorMetricValueList.add(new InspectorMetricValue("MIN", field.getTags(), field.getChartType(), field.getUnit(), minValueList));
        inspectorMetricValueList.add(new InspectorMetricValue("MAX", field.getTags(), field.getChartType(), field.getUnit(), maxValueList));
        return inspectorMetricValueList;
    }

    private List<QueryResult> selectAll(InspectorDataSearchKey inspectorDataSearchKey, MetricDefinition metricDefinition) {
        List<QueryResult> invokeList = new ArrayList<>();

        for (Field field : metricDefinition.getFields()) {
            CompletableFuture<? extends List<? extends Point>> doubleFuture = null;
            Class<?> resultType;

            if (AggregationFunction.AVG_MIN_MAX.equals(field.getAggregationFunction())) {
                doubleFuture = applicationStatDao.selectStatAvgMinMax(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
                resultType = AvgMinMaxMetricPoint.class;
            } else if (AggregationFunction.AVG_MIN.equals(field.getAggregationFunction())) {
                doubleFuture = applicationStatDao.selectStatAvgMin(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
                resultType = AvgMinMetricPoint.class;
            } else if (AggregationFunction.MIN_MAX.equals(field.getAggregationFunction())) {
                doubleFuture = applicationStatDao.selectStatMinMax(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
                resultType = MinMaxMetricPoint.class;
            } else if (AggregationFunction.SUM.equals(field.getAggregationFunction())) {
                doubleFuture = applicationStatDao.selectStatSum(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
                resultType = SystemMetricPoint.class;
            } else if (AggregationFunction.MAX.equals(field.getAggregationFunction())) {
                doubleFuture = applicationStatDao.selectStatMax(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
                resultType = SystemMetricPoint.class;
            } else {
                throw new RuntimeException("not support aggregation function : " + field.getAggregationFunction());
            }

            invokeList.add(new QueryResult(doubleFuture, field, resultType));
        }

        return invokeList;
    }

    private List<QueryResult> selectAll2(InspectorDataSearchKey inspectorDataSearchKey, MetricDefinition metricDefinition) {
        List<QueryResult> invokeList = new ArrayList<>();

        for (Field field : metricDefinition.getFields()) {
            CompletableFuture<? extends List<? extends Point>> doubleFuture = null;
            Class<?> resultType;

            if (AggregationFunction.AVG_MIN_MAX.equals(field.getAggregationFunction())) {
                doubleFuture = applicationStatDaoV2.selectStatAvgMinMax(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
                resultType = AvgMinMaxMetricPoint.class;
            } else if (AggregationFunction.AVG_MIN.equals(field.getAggregationFunction())) {
                doubleFuture = applicationStatDaoV2.selectStatAvgMin(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
                resultType = AvgMinMetricPoint.class;
            } else if (AggregationFunction.MIN_MAX.equals(field.getAggregationFunction())) {
                doubleFuture = applicationStatDaoV2.selectStatMinMax(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
                resultType = MinMaxMetricPoint.class;
            } else if (AggregationFunction.SUM.equals(field.getAggregationFunction())) {
                doubleFuture = applicationStatDaoV2.selectStatSum(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
                resultType = SystemMetricPoint.class;
            } else if (AggregationFunction.MAX.equals(field.getAggregationFunction())) {
                doubleFuture = applicationStatDaoV2.selectStatMax(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
                resultType = SystemMetricPoint.class;
            } else {
                throw new RuntimeException("not support aggregation function : " + field.getAggregationFunction());
            }

            invokeList.add(new QueryResult(doubleFuture, field, resultType));
        }

        return invokeList;
    }

    // TODO : (minwoo) It seems that this can also be integrated into one with the com.navercorp.pinpoint.inspector.web.service.DefaultAgentStatService.QueryResult.
    private record QueryResult(CompletableFuture<? extends List<? extends Point>> future, Field field, Class<?> resultType) {

    }
}
