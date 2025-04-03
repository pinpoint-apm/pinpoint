package com.navercorp.pinpoint.inspector.web.service;

import com.google.common.primitives.Doubles;
import com.navercorp.pinpoint.common.server.timeseries.Point;
import com.navercorp.pinpoint.common.server.util.array.DoubleArray;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
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
import com.navercorp.pinpoint.metric.common.model.chart.AvgMinMaxMetricPoint;
import com.navercorp.pinpoint.metric.common.model.chart.AvgMinMetricPoint;
import com.navercorp.pinpoint.metric.common.model.chart.DoubleSystemMetricPoint;
import com.navercorp.pinpoint.metric.common.model.chart.MinMaxMetricPoint;
import com.navercorp.pinpoint.metric.common.model.chart.SystemMetricPoint;
import com.navercorp.pinpoint.metric.common.util.PointCreator;
import com.navercorp.pinpoint.metric.common.util.TimeSeriesBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class DefaultApplicationStatService implements ApplicationStatService {

    private static final String MIN = "MIN";
    private static final String AVG = "AVG";
    private static final String MAX = "MAX";

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final YMLInspectorManager ymlInspectorManager;
    private final MetricProcessorManager metricProcessorManager;
    private final ApplicationStatDao applicationStatDao;

    public DefaultApplicationStatService(@Qualifier("pinotApplicationStatDao") ApplicationStatDao applicationStatDao, @Qualifier("applicationInspectorDefinition") Mappings applicationInspectorDefinition, MetricProcessorManager metricProcessorManager) {
        this.applicationStatDao = Objects.requireNonNull(applicationStatDao, "applicationStatDao");
        Objects.requireNonNull(applicationInspectorDefinition, "applicationInspectorDefinition");
        this.ymlInspectorManager = new YMLInspectorManager(applicationInspectorDefinition);
        this.metricProcessorManager = Objects.requireNonNull(metricProcessorManager, "metricProcessorManager");
    }

    @Override
    public InspectorMetricData selectApplicationStat(InspectorDataSearchKey inspectorDataSearchKey, TimeWindow timeWindow) {
        MetricDefinition metricDefinition = ymlInspectorManager.findElementOfBasicGroup(inspectorDataSearchKey.getMetricDefinitionId());

        List<QueryResult> queryResults = selectAll2(inspectorDataSearchKey, metricDefinition);
        List<InspectorMetricValue> metricValueList = new ArrayList<>(queryResults.size());

        try {
            for (QueryResult result : queryResults) {
                Class<?> resultType = result.resultType();
                if (resultType.equals(AvgMinMaxMetricPoint.class)) {
                    List<AvgMinMaxMetricPoint> doubleList = (List<AvgMinMaxMetricPoint>) await(result.future());
                    metricValueList.addAll(splitAvgMinMax(timeWindow, result.field(), doubleList));
                } else if (resultType.equals(AvgMinMetricPoint.class)) {
                    List<AvgMinMetricPoint> doubleList = (List<AvgMinMetricPoint>) await(result.future());
                    metricValueList.addAll(splitAvgMin(timeWindow, result.field(), doubleList));
                } else if (resultType.equals(MinMaxMetricPoint.class)) {
                    List<MinMaxMetricPoint> doubleList = (List<MinMaxMetricPoint>) await(result.future());
                    metricValueList.addAll(splitMinMax(timeWindow, result.field(), doubleList));
                } else if (resultType.equals(DoubleSystemMetricPoint.class)) {
                    List<SystemMetricPoint<Double>> doubleList = (List<SystemMetricPoint<Double>>) await(result.future());
                    metricValueList.add(createInspectorMetricValue(timeWindow, result.field(), doubleList));
                } else {
                    throw new RuntimeException("not support result type : " + result.resultType());
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        List<InspectorMetricValue> processedMetricValueList = postprocessMetricData(metricDefinition, metricValueList);
        processedMetricValueList = sortingMetricValueList(processedMetricValueList);
        List<Long> timeStampList = timeWindow.getTimeseriesWindows();
        return new InspectorMetricData(metricDefinition.getTitle(), timeStampList, processedMetricValueList);
    }

    private List<? extends Point> await(CompletableFuture<? extends List<? extends Point>> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    private List<InspectorMetricValue> sortingMetricValueList(List<InspectorMetricValue> processedMetricValueList) {
        InspectorMetricValue[] sortedMetricValues = new InspectorMetricValue[3];

        for (InspectorMetricValue value : processedMetricValueList) {
            switch (value.getFieldName()) {
                case MIN:
                    sortedMetricValues[0] = value;
                    break;
                case AVG:
                    sortedMetricValues[1] = value;
                    break;
                case MAX:
                    sortedMetricValues[2] = value;
                    break;
                default:
                    throw new RuntimeException("not supported field name : " + value.getFieldName());
            }
        }

        return Arrays.asList(sortedMetricValues);
    }

    @Override
    public InspectorMetricGroupData selectApplicationStatWithGrouping(InspectorDataSearchKey inspectorDataSearchKey, TimeWindow timeWindow) {
        MetricDefinition metricDefinition = ymlInspectorManager.findElementOfBasicGroup(inspectorDataSearchKey.getMetricDefinitionId());
        MetricDefinition newMetricDefinition = preProcess(inspectorDataSearchKey, metricDefinition);

        List<QueryResult> queryResults = selectAll2(inspectorDataSearchKey, newMetricDefinition);
        List<InspectorMetricValue> metricValueList = new ArrayList<>(newMetricDefinition.getFields().size());

        for (QueryResult result : queryResults) {
            Class<?> resultType = result.resultType();
            if (resultType.equals(AvgMinMaxMetricPoint.class)) {
                List<AvgMinMaxMetricPoint> doubleList = (List<AvgMinMaxMetricPoint>) await(result.future());
                metricValueList.addAll(splitAvgMinMax(timeWindow, result.field(), doubleList));
            } else if (resultType.equals(AvgMinMetricPoint.class)) {
                List<AvgMinMetricPoint> doubleList = (List<AvgMinMetricPoint>) await(result.future());
                metricValueList.addAll(splitAvgMin(timeWindow, result.field(), doubleList));
            } else if (resultType.equals(MinMaxMetricPoint.class)) {
                List<MinMaxMetricPoint> doubleList = (List<MinMaxMetricPoint>) await(result.future());
                metricValueList.addAll(splitMinMax(timeWindow, result.field(), doubleList));
            } else if (resultType.equals(DoubleSystemMetricPoint.class)) {
                List<SystemMetricPoint<Double>> doubleList = (List<SystemMetricPoint<Double>>) await(result.future());
                metricValueList.add(createInspectorMetricValue(timeWindow, result.field(), doubleList));
            } else {
                throw new RuntimeException("not support result type : " + result.resultType());
            }
        }


        List<InspectorMetricValue> processedMetricValueList = postprocessMetricData(newMetricDefinition, metricValueList);
        List<Long> timeStampList = timeWindow.getTimeseriesWindows();
        Map<List<Tag>, List<InspectorMetricValue>> metricValueGroups = groupingMetricValue(processedMetricValueList, metricDefinition);
        metricValueGroups = sortingMetricValueGroups(metricValueGroups);
        return new InspectorMetricGroupData(metricDefinition.getTitle(), timeStampList, metricValueGroups);
    }

    private Map<List<Tag>, List<InspectorMetricValue>> sortingMetricValueGroups(Map<List<Tag>, List<InspectorMetricValue>> metricValueGroups) {
        return metricValueGroups.entrySet().stream()
                .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> sortingMetricValueList(entry.getValue())
                        )
                );
    }

    private Map<List<Tag>, List<InspectorMetricValue>> groupingMetricValue(List<InspectorMetricValue> processedMetricValueList, MetricDefinition metricDefinition) {
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
                                                            List<SystemMetricPoint<Double>> sampledSystemMetricDataList) {
        TimeSeriesBuilder builder = new TimeSeriesBuilder(timeWindow);
        List<SystemMetricPoint<Double>> filledSystemMetricDataList = builder.buildDoubleMetric(PointCreator::doublePoint, sampledSystemMetricDataList);

        List<Double> valueList = DoubleArray.asList(filledSystemMetricDataList, DoubleSystemMetricPoint::getRawY);

        return newInspectorMetric(field.getFieldAlias(), field, valueList);
    }

    private List<InspectorMetricValue> splitMinMax(TimeWindow timeWindow, Field field, List<MinMaxMetricPoint> doubleList) {
        TimeSeriesBuilder builder = new TimeSeriesBuilder(timeWindow);
        List<MinMaxMetricPoint> filledSystemMetricDataList = builder.buildForMinMaxMetricPointList(PointCreator::createMinMaxMetricPoint, doubleList);

        final int size = filledSystemMetricDataList.size();
        double[] minValueList = new double[size];
        double[] maxValueList = new double[size];

        int i = 0;
        for (MinMaxMetricPoint avgMinMaxMetricPoint : filledSystemMetricDataList) {
            minValueList[i] = avgMinMaxMetricPoint.getMinValue();
            maxValueList[i] = avgMinMaxMetricPoint.getMaxValue();
            i++;
        }

        return List.of(
                newInspectorMetric(MIN, field, Doubles.asList(minValueList)),
                newInspectorMetric(MAX, field, Doubles.asList(maxValueList))
        );
    }

    private Collection<InspectorMetricValue> splitAvgMin(TimeWindow timeWindow, Field field, List<AvgMinMetricPoint> doubleList) {
        TimeSeriesBuilder builder = new TimeSeriesBuilder(timeWindow);
        List<AvgMinMetricPoint> filledSystemMetricDataList = builder.buildForAvgMinMetricPointList(PointCreator::createAvgMinMetricPoint, doubleList);

        final int size = filledSystemMetricDataList.size();
        final double[] avgValueList = new double[size];
        final double[] minValueList = new double[size];

        int i =0;
        for (AvgMinMetricPoint avgMinMetricPoint : filledSystemMetricDataList) {
            avgValueList[i] = avgMinMetricPoint.getAvgValue();
            minValueList[i] = avgMinMetricPoint.getMinValue();
            i++;
        }

        return List.of(
                newInspectorMetric(MIN, field, Doubles.asList(minValueList)),
                newInspectorMetric(AVG, field, Doubles.asList(avgValueList))
        );
    }

    private InspectorMetricValue newInspectorMetric(String fieldName, Field field, List<Double> minValueList) {
        return new InspectorMetricValue(fieldName, field.getTags(), field.getChartType(), field.getUnit(), minValueList);
    }

    private List<InspectorMetricValue> splitAvgMinMax(TimeWindow timeWindow, Field field, List<AvgMinMaxMetricPoint> doubleList) {
        TimeSeriesBuilder builder = new TimeSeriesBuilder(timeWindow);
        List<AvgMinMaxMetricPoint> filledSystemMetricDataList = builder.buildForAvgMinMaxMetricPointList(PointCreator::createAvgMinMaxMetricPoint, doubleList);

        final int size = filledSystemMetricDataList.size();
        final double[] avgValueList = new double[size];
        final double[] minValueList = new double[size];
        final double[] maxValueList = new double[size];

        int i = 0;
        for (AvgMinMaxMetricPoint avgMinMaxMetricPoint : filledSystemMetricDataList) {
            avgValueList[i] = avgMinMaxMetricPoint.getAvgValue();
            minValueList[i] = avgMinMaxMetricPoint.getMinValue();
            maxValueList[i] = avgMinMaxMetricPoint.getMaxValue();
            i++;
        }

        return List.of(
                newInspectorMetric(MIN, field, Doubles.asList(minValueList)),
                newInspectorMetric(AVG, field, Doubles.asList(avgValueList)),
                newInspectorMetric(MAX, field, Doubles.asList(maxValueList))
        );
    }

    private List<QueryResult> selectAll2(InspectorDataSearchKey inspectorDataSearchKey, MetricDefinition metricDefinition) {
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
                resultType = DoubleSystemMetricPoint.class;
            } else if (AggregationFunction.MAX.equals(field.getAggregationFunction())) {
                doubleFuture = applicationStatDao.selectStatMax(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
                resultType = DoubleSystemMetricPoint.class;
            } else {
                throw new RuntimeException("not support aggregation function : " + field.getAggregationFunction());
            }

            invokeList.add(new QueryResult(doubleFuture, field, resultType));
        }

        return invokeList;
    }

    // TODO : (minwoo) It seems that this can also be integrated into one with the com.navercorp.pinpoint.inspector.web.service.DefaultAgentStatService.QueryResult.
    private record QueryResult(CompletableFuture<? extends List<? extends Point>> future, Field field,
                               Class<?> resultType) {

    }
}
