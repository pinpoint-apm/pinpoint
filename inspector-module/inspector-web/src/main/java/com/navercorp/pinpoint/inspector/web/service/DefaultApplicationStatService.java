package com.navercorp.pinpoint.inspector.web.service;

import com.navercorp.pinpoint.inspector.web.dao.ApplicationStatDao;
import com.navercorp.pinpoint.inspector.web.definition.AggregationFunction;
import com.navercorp.pinpoint.inspector.web.definition.Mappings;
import com.navercorp.pinpoint.inspector.web.definition.MetricDefinition;
import com.navercorp.pinpoint.inspector.web.definition.YMLInspectorManager;
import com.navercorp.pinpoint.inspector.web.definition.metric.field.Field;
import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricData;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricValue;
import com.navercorp.pinpoint.metric.common.model.TimeWindow;
import com.navercorp.pinpoint.metric.common.model.chart.AvgMinMaxMetricPoint;
import com.navercorp.pinpoint.metric.common.util.SystemMetricPoint;
import com.navercorp.pinpoint.metric.common.util.DoubleUncollectedDataCreator;
import com.navercorp.pinpoint.metric.common.util.TimeSeriesBuilder;
import com.navercorp.pinpoint.metric.common.util.TimeUtils;
import com.navercorp.pinpoint.metric.common.util.UncollectedDataCreator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class DefaultApplicationStatService implements ApplicationStatService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final YMLInspectorManager ymlInspectorManager;
    private final ApplicationStatDao applicationStatDao;

    public DefaultApplicationStatService(ApplicationStatDao applicationStatDao, @Qualifier("applicationInspectorDefinition") Mappings applicationInspectorDefinition) {
        this.applicationStatDao = Objects.requireNonNull(applicationStatDao, "applicationStatDao");
        Objects.requireNonNull(applicationInspectorDefinition, "applicationInspectorDefinition");
        this.ymlInspectorManager = new YMLInspectorManager(applicationInspectorDefinition);
    }

    @Override
    public InspectorMetricData selectApplicationStat(InspectorDataSearchKey inspectorDataSearchKey, TimeWindow timeWindow) {
        MetricDefinition metricDefinition = ymlInspectorManager.findElementOfBasicGroup(inspectorDataSearchKey.getMetricDefinitionId());

        List<QueryResult> queryResults =  selectAll(inspectorDataSearchKey, metricDefinition);

        List<InspectorMetricValue> inspectorMetricValueList = new ArrayList<>(queryResults.size());
        try {
            for (QueryResult result : queryResults) {
                Future<List<AvgMinMaxMetricPoint<Double>>> future = result.getFuture();
                List<AvgMinMaxMetricPoint<Double>> doubleList = future.get();
                inspectorMetricValueList.addAll(splitAvgMinMax(timeWindow, result.getField(), doubleList, DoubleUncollectedDataCreator.UNCOLLECTED_DATA_CREATOR));
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        List<Long> timeStampList = TimeUtils.createTimeStampList(timeWindow);
        return new InspectorMetricData(metricDefinition.getTitle(), timeStampList, inspectorMetricValueList);
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
            Future<List<AvgMinMaxMetricPoint<Double>>> doubleFuture = null;
            if (AggregationFunction.AVG_MIN_MAX.equals(field.getAggregationFunction())) {
                doubleFuture = applicationStatDao.selectAgentStatAvgMinMax(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
            } else {
                throw new RuntimeException("not support aggregation function : " + field.getAggregationFunction());
            }

            invokeList.add(new QueryResult(doubleFuture, field));
        }

        return invokeList;
    }

    // TODO : (minwoo) It seems that this can also be integrated into one with the com.navercorp.pinpoint.inspector.web.service.DefaultAgentStatService.QueryResult.
    private static class QueryResult {
        private final Future<List<AvgMinMaxMetricPoint<Double>>> future;
        private final Field field;

        public QueryResult(Future<List<AvgMinMaxMetricPoint<Double>>> future, Field field) {
            this.future = Objects.requireNonNull(future, "future");
            this.field = Objects.requireNonNull(field, "field");
        }

        public Future<List<AvgMinMaxMetricPoint<Double>>> getFuture() {
            return future;
        }

        public Field getField() {
            return field;
        }

    }
}
