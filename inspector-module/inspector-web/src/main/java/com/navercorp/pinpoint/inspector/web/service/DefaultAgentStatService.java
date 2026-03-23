/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.inspector.web.service;

import com.navercorp.pinpoint.common.timeseries.array.DoubleArray;
import com.navercorp.pinpoint.common.timeseries.point.DataPoint;
import com.navercorp.pinpoint.common.timeseries.point.DoubleDataPoint;
import com.navercorp.pinpoint.common.timeseries.point.Points;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.inspector.web.dao.model.AgentStatPoint;
import com.navercorp.pinpoint.inspector.web.dao.AgentStatDao;
import com.navercorp.pinpoint.inspector.web.definition.AggregationFunction;
import com.navercorp.pinpoint.inspector.web.definition.Mappings;
import com.navercorp.pinpoint.inspector.web.definition.MetricDefinition;
import com.navercorp.pinpoint.inspector.web.definition.YMLInspectorManager;
import com.navercorp.pinpoint.inspector.web.definition.metric.MetricPostProcessor;
import com.navercorp.pinpoint.inspector.web.definition.metric.MetricPreProcessor;
import com.navercorp.pinpoint.inspector.web.definition.metric.MetricProcessorManager;
import com.navercorp.pinpoint.inspector.web.definition.metric.field.Field;
import com.navercorp.pinpoint.inspector.web.definition.metric.field.FieldPostProcessor;
import com.navercorp.pinpoint.inspector.web.definition.metric.field.FieldProcessorManager;
import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricData;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricGroupData;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricValue;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.util.PointCreator;
import com.navercorp.pinpoint.metric.common.util.TagUtils;
import com.navercorp.pinpoint.metric.common.util.TimeSeriesBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author minwoo.jung
 */
@Service
public class DefaultAgentStatService implements AgentStatService {

    private static final int MAX_AGENT_IDS = 20;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentStatDao agentStatDao;
    private final YMLInspectorManager ymlInspectorManager;
    private final MetricProcessorManager metricProcessorManager;
    private final FieldProcessorManager fieldProcessorManager;

    public DefaultAgentStatService(@Qualifier("pinotAgentStatDao") AgentStatDao agentStatDao,
                                   @Qualifier("agentInspectorDefinition") Mappings agentInspectorDefinition,
                                   MetricProcessorManager metricProcessorManager,
                                   FieldProcessorManager fieldProcessorManager) {
        this.agentStatDao = Objects.requireNonNull(agentStatDao, "agentStatDao");
        Objects.requireNonNull(agentInspectorDefinition, "agentInspectorDefinition");
        this.ymlInspectorManager = new YMLInspectorManager(agentInspectorDefinition);
        this.metricProcessorManager = Objects.requireNonNull(metricProcessorManager, "metricProcessorManager");
        this.fieldProcessorManager = Objects.requireNonNull(fieldProcessorManager, "fieldProcessorManager");
    }

    @Override
    public InspectorMetricData selectAgentStat(InspectorDataSearchKey inspectorDataSearchKey, TimeWindow timeWindow) {
        MetricDefinition metricDefinition = ymlInspectorManager.findElementOfBasicGroup(inspectorDataSearchKey.getMetricDefinitionId());

        List<QueryResult> queryResults = selectAll(inspectorDataSearchKey, metricDefinition);

        List<InspectorMetricValue> metricValueList = new ArrayList<>(metricDefinition.getFields().size());

        try {
            for (QueryResult result : queryResults) {
                CompletableFuture<List<DataPoint<Double>>> future = result.future();
                List<DataPoint<Double>> doubleList = future.get();

                InspectorMetricValue doubleMetricValue = createInspectorMetricValue(timeWindow, result.field(), doubleList);
                metricValueList.add(doubleMetricValue);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        List<InspectorMetricValue> processedMetricValueList = postprocessMetricData(metricDefinition, metricValueList);
        List<Long> timeStampList = timeWindow.getTimeseriesWindows();
        return new InspectorMetricData(metricDefinition.getTitle(), timeStampList, processedMetricValueList);
    }

    @Override
    public List<DataPoint<Double>> selectAgentStatUnconvertedTime(InspectorDataSearchKey inspectorDataSearchKey, TimeWindow timeWindow) {
        MetricDefinition metricDefinition = ymlInspectorManager.findElementOfBasicGroup(inspectorDataSearchKey.getMetricDefinitionId());

        QueryResult queryResult = selectOneField(inspectorDataSearchKey, metricDefinition);

        try {
            CompletableFuture<List<DataPoint<Double>>> future = queryResult.future();
            List<DataPoint<Double>> doubleList = future.get();

            return postprocessFieldData(queryResult.field(), doubleList);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public InspectorMetricGroupData selectAgentStatWithGrouping(InspectorDataSearchKey inspectorDataSearchKey, TimeWindow timeWindow) {
        MetricDefinition metricDefinition = ymlInspectorManager.findElementOfBasicGroup(inspectorDataSearchKey.getMetricDefinitionId());
        MetricDefinition newMetricDefinition = preProcess(inspectorDataSearchKey, metricDefinition);
        List<InspectorMetricValue> metricValueList = new ArrayList<>(newMetricDefinition.getFields().size());

        List<QueryResult> queryResults = selectAll(inspectorDataSearchKey, newMetricDefinition);

        try {
            for (QueryResult result : queryResults) {
                CompletableFuture<List<DataPoint<Double>>> future = result.future();
                List<DataPoint<Double>> doubleList = future.get();

                InspectorMetricValue doubleMetricValue = createInspectorMetricValue(timeWindow, result.field(), doubleList);
                metricValueList.add(doubleMetricValue);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        List<InspectorMetricValue> processedMetricValueList = postprocessMetricData(newMetricDefinition, metricValueList);
        List<Long> timeStampList = timeWindow.getTimeseriesWindows();
        Map<List<Tag>, List<InspectorMetricValue>> metricValueGroups = groupingMetricValue(processedMetricValueList, metricDefinition);

        return new InspectorMetricGroupData(metricDefinition.getTitle(), timeStampList, metricValueGroups);
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
                                                            List<DataPoint<Double>> sampledSystemMetricDataList) {

        List<DataPoint<Double>> postProcessedDataList = postprocessFieldData(field, sampledSystemMetricDataList);

        TimeSeriesBuilder builder = new TimeSeriesBuilder(timeWindow);
        List<DataPoint<Double>> filledSystemMetricDataList = builder.buildDoubleMetric(PointCreator::doublePoint, postProcessedDataList);

        List<Double> valueList = DoubleArray.asList(filledSystemMetricDataList, Points::asDouble);

        return new InspectorMetricValue(field.getFieldAlias(), field.getTags(), field.getChartType(), field.getUnit(), valueList);
    }

    private List<DataPoint<Double>> postprocessFieldData(Field field, List<DataPoint<Double>> sampledSystemMetricDataList) {
        FieldPostProcessor postProcessor = fieldProcessorManager.getPostProcessor(field.getPostProcess());
        return postProcessor.postProcess(sampledSystemMetricDataList);
    }

    private List<QueryResult> selectAll(InspectorDataSearchKey inspectorDataSearchKey, MetricDefinition metricDefinition) {
        List<QueryResult> invokeList = new ArrayList<>();

        for (Field field : metricDefinition.getFields()) {
            //TODO : (minwoo) Consolidate dao calls into one
            CompletableFuture<List<DataPoint<Double>>> doubleFuture = null;
            if (AggregationFunction.AVG.equals(field.getAggregationFunction())) {
                doubleFuture = agentStatDao.selectAgentStatAvg(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
            } else if (AggregationFunction.MAX.equals(field.getAggregationFunction())) {
                doubleFuture = agentStatDao.selectAgentStatMax(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
            } else if (AggregationFunction.SUM.equals(field.getAggregationFunction())) {
                doubleFuture = agentStatDao.selectAgentStatSum(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
            } else {
                throw new IllegalArgumentException("Unknown aggregation function : " + field.getAggregationFunction());
            }
            invokeList.add(new QueryResult(doubleFuture, field));
        }

        return invokeList;
    }

    private QueryResult selectOneField(InspectorDataSearchKey inspectorDataSearchKey, MetricDefinition metricDefinition) {
        Field field = metricDefinition.getFields().stream().findFirst().get();
        CompletableFuture<List<DataPoint<Double>>> doubleFuture = agentStatDao.selectAgentStat(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
        return new QueryResult(doubleFuture, field);
    }


    @Override
    public InspectorMetricGroupData selectAgentStatGroupedByAgentId(
            String tenantId, String applicationName, List<String> agentIds,
            String metricDefinitionId, TimeWindow timeWindow) {

        if (agentIds.size() > MAX_AGENT_IDS) {
            throw new IllegalArgumentException("agentIds size exceeds max limit. size: " + agentIds.size() + ", max: " + MAX_AGENT_IDS);
        }

        MetricDefinition metricDefinition = ymlInspectorManager.findElementOfBasicGroup(metricDefinitionId);

        // One query per field (M queries) instead of N×M queries
        List<BatchQueryResult> batchResults = selectAllByAgentIds(tenantId, applicationName, agentIds, metricDefinition, timeWindow);

        CompletableFuture<?>[] allFutures = batchResults.stream()
                .map(BatchQueryResult::future)
                .toArray(CompletableFuture[]::new);

        try {
            CompletableFuture.allOf(allFutures).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }

        Map<List<Tag>, List<InspectorMetricValue>> metricValueGroups = new LinkedHashMap<>();
        for (String agentId : agentIds) {
            List<InspectorMetricValue> metricValueList = new ArrayList<>(batchResults.size());

            for (BatchQueryResult batchResult : batchResults) {
                List<AgentStatPoint> allPoints = batchResult.future().join();
                List<DataPoint<Double>> dataPoints = allPoints.stream()
                        .filter(p -> agentId.equals(p.getAgentId()))
                        .map(p -> (DataPoint<Double>) new DoubleDataPoint(p.getAvgTime(), p.getAvgValue()))
                        .collect(Collectors.toList());
                metricValueList.add(createInspectorMetricValue(timeWindow, batchResult.field(), dataPoints));
            }

            List<InspectorMetricValue> processedList = postprocessMetricData(metricDefinition, metricValueList);
            metricValueGroups.put(List.of(new Tag("agentId", agentId)), processedList);
        }

        List<Long> timeStampList = timeWindow.getTimeseriesWindows();
        return new InspectorMetricGroupData(metricDefinition.getTitle(), timeStampList, metricValueGroups);
    }

    private List<BatchQueryResult> selectAllByAgentIds(String tenantId, String applicationName, List<String> agentIds,
                                                       MetricDefinition metricDefinition, TimeWindow timeWindow) {
        List<BatchQueryResult> results = new ArrayList<>();
        for (Field field : metricDefinition.getFields()) {
            CompletableFuture<List<AgentStatPoint>> future;
            if (AggregationFunction.AVG.equals(field.getAggregationFunction())) {
                future = agentStatDao.selectAgentStatAvgByAgentIds(tenantId, applicationName, agentIds, metricDefinition.getMetricName(), field, timeWindow);
            } else if (AggregationFunction.MAX.equals(field.getAggregationFunction())) {
                future = agentStatDao.selectAgentStatMaxByAgentIds(tenantId, applicationName, agentIds, metricDefinition.getMetricName(), field, timeWindow);
            } else if (AggregationFunction.SUM.equals(field.getAggregationFunction())) {
                future = agentStatDao.selectAgentStatSumByAgentIds(tenantId, applicationName, agentIds, metricDefinition.getMetricName(), field, timeWindow);
            } else {
                throw new IllegalArgumentException("Unknown aggregation function : " + field.getAggregationFunction());
            }
            results.add(new BatchQueryResult(future, field));
        }
        return results;
    }

    //TODO : (minwoo) It seems that this can also be integrated into one with the metric side.
    private record QueryResult(CompletableFuture<List<DataPoint<Double>>> future, Field field) {
    }

    private record BatchQueryResult(CompletableFuture<List<AgentStatPoint>> future, Field field) {
    }

}
