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
import com.navercorp.pinpoint.metric.common.model.TimeWindow;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
* @author minwoo.jung
 */
@Service
public class DefaultAgentStatService implements AgentStatService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentStatDao agentStatDaoV2;
    private final AgentStatDao agentStatDao;
    private final YMLInspectorManager ymlInspectorManager;
    private final MetricProcessorManager metricProcessorManager;
    private final FieldProcessorManager fieldProcessorManager;

    public DefaultAgentStatService(@Qualifier("pinotAgentStatDaoV2")AgentStatDao agentStatDaoV2, @Qualifier("pinotAgentStatDao")AgentStatDao agentStatDao, @Qualifier("agentInspectorDefinition")Mappings agentInspectorDefinition, MetricProcessorManager metricProcessorManager, FieldProcessorManager fieldProcessorManager) {
        this.agentStatDao = Objects.requireNonNull(agentStatDao, "agentStatDao");
        this.agentStatDaoV2 = Objects.requireNonNull(agentStatDaoV2, "agentStatDaoV2");
        Objects.requireNonNull(agentInspectorDefinition, "agentInspectorDefinition");
        this.ymlInspectorManager = new YMLInspectorManager(agentInspectorDefinition);
        this.metricProcessorManager = Objects.requireNonNull(metricProcessorManager, "metricProcessorManager");
        this.fieldProcessorManager = Objects.requireNonNull(fieldProcessorManager, "fieldProcessorManager");
    }

    @Override
    public InspectorMetricData selectAgentStat(InspectorDataSearchKey inspectorDataSearchKey, TimeWindow timeWindow){
        MetricDefinition metricDefinition = ymlInspectorManager.findElementOfBasicGroup(inspectorDataSearchKey.getMetricDefinitionId());

        List<QueryResult> queryResults;
        if (inspectorDataSearchKey.getVersion() == 2) {
            queryResults = selectAll2(inspectorDataSearchKey, metricDefinition);
        } else {
            queryResults = selectAll(inspectorDataSearchKey, metricDefinition);
        }

        List<InspectorMetricValue> metricValueList = new ArrayList<>(metricDefinition.getFields().size());

        try {
            for (QueryResult result : queryResults) {
                CompletableFuture<List<SystemMetricPoint<Double>>> future = result.future();
                List<SystemMetricPoint<Double>> doubleList = future.get();

                InspectorMetricValue doubleMetricValue = createInspectorMetricValue(timeWindow, result.field(), doubleList, DoubleUncollectedDataCreator.UNCOLLECTED_DATA_CREATOR);
                metricValueList.add(doubleMetricValue);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        List<InspectorMetricValue> processedMetricValueList = postprocessMetricData(metricDefinition, metricValueList);
        List<Long> timeStampList = TimeUtils.createTimeStampList(timeWindow);
        return new InspectorMetricData(metricDefinition.getTitle(), timeStampList, processedMetricValueList);
    }

    public InspectorMetricGroupData selectAgentStatWithGrouping(InspectorDataSearchKey inspectorDataSearchKey, TimeWindow timeWindow){
        MetricDefinition metricDefinition = ymlInspectorManager.findElementOfBasicGroup(inspectorDataSearchKey.getMetricDefinitionId());
        MetricDefinition newMetricDefinition = preProcess(inspectorDataSearchKey, metricDefinition);

        List<QueryResult> queryResults;
        if (inspectorDataSearchKey.getVersion() == 2) {
            queryResults = selectAll2(inspectorDataSearchKey, newMetricDefinition);
        } else {
            queryResults = selectAll(inspectorDataSearchKey, newMetricDefinition);
        }


        List<InspectorMetricValue> metricValueList = new ArrayList<>(newMetricDefinition.getFields().size());

        try {
            for (QueryResult result : queryResults) {
                CompletableFuture<List<SystemMetricPoint<Double>>> future = result.future();
                List<SystemMetricPoint<Double>> doubleList = future.get();

                InspectorMetricValue doubleMetricValue = createInspectorMetricValue(timeWindow, result.field(), doubleList, DoubleUncollectedDataCreator.UNCOLLECTED_DATA_CREATOR);
                metricValueList.add(doubleMetricValue);
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

        FieldPostProcessor postProcessor = fieldProcessorManager.getPostProcessor(field.getPostProcess());
        List<SystemMetricPoint<Double>> postProcessedDataList = postProcessor.postProcess(sampledSystemMetricDataList);

        TimeSeriesBuilder<Double> builder = new TimeSeriesBuilder<>(timeWindow, uncollectedDataCreator);
        List<SystemMetricPoint<Double>> filledSystemMetricDataList = builder.build(postProcessedDataList);

        List<Double> valueList = filledSystemMetricDataList.stream()
                .map(SystemMetricPoint::getYVal)
                .collect(Collectors.toList());

        return new InspectorMetricValue(field.getFieldAlias(), field.getTags(), field.getChartType(), field.getUnit(), valueList);
    }

    private List<QueryResult> selectAll(InspectorDataSearchKey inspectorDataSearchKey, MetricDefinition metricDefinition) {
        List<QueryResult> invokeList = new ArrayList<>();

        for (Field field : metricDefinition.getFields()) {
            //TODO : (minwoo) Consolidate dao calls into one
            CompletableFuture<List<SystemMetricPoint<Double>>> doubleFuture = null;
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

    private List<QueryResult> selectAll2(InspectorDataSearchKey inspectorDataSearchKey, MetricDefinition metricDefinition) {
        List<QueryResult> invokeList = new ArrayList<>();

        for (Field field : metricDefinition.getFields()) {
            //TODO : (minwoo) Consolidate dao calls into one
            CompletableFuture<List<SystemMetricPoint<Double>>> doubleFuture = null;
            if (AggregationFunction.AVG.equals(field.getAggregationFunction())) {
                doubleFuture = agentStatDaoV2.selectAgentStatAvg(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
            } else if (AggregationFunction.MAX.equals(field.getAggregationFunction())) {
                doubleFuture = agentStatDaoV2.selectAgentStatMax(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
            } else if (AggregationFunction.SUM.equals(field.getAggregationFunction())) {
                doubleFuture = agentStatDaoV2.selectAgentStatSum(inspectorDataSearchKey, metricDefinition.getMetricName(), field);
            } else {
                throw new IllegalArgumentException("Unknown aggregation function : " + field.getAggregationFunction());
            }
            invokeList.add(new QueryResult(doubleFuture, field));
        }

        return invokeList;
    }

    //TODO : (minwoo) It seems that this can also be integrated into one with the metric side.
    private record QueryResult(CompletableFuture<List<SystemMetricPoint<Double>>> future, Field field) {
    }

}
