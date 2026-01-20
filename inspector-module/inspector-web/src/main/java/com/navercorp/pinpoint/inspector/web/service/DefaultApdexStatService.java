/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.inspector.web.service;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import com.navercorp.pinpoint.common.timeseries.point.DataPoint;
import com.navercorp.pinpoint.common.timeseries.point.Points;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowSampler;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.inspector.web.definition.Mappings;
import com.navercorp.pinpoint.inspector.web.definition.MetricDefinition;
import com.navercorp.pinpoint.inspector.web.definition.YMLInspectorManager;
import com.navercorp.pinpoint.inspector.web.definition.metric.field.Field;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricData;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricValue;
import com.navercorp.pinpoint.metric.common.util.TagUtils;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.service.ApdexScoreService;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentApdexScoreChart;
import com.navercorp.pinpoint.web.vo.stat.chart.application.ApplicationApdexScoreChart;
import com.navercorp.pinpoint.web.vo.stat.chart.application.ApplicationStatPoint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * @author minwoo-jung
 */
@Service
public class DefaultApdexStatService implements ApdexStatService {

    private static final TimeWindowSampler APDEX_SCORE_TIME_WINDOW_SAMPLER = new TimeWindowSlotCentricSampler(60 * 1000, 200);
    public static final String MIN = "MIN";
    public static final String AVG = "AVG";
    public static final String MAX = "MAX";

    private final ApplicationFactory applicationFactory;

    private final ApdexScoreService apdexScoreService;

    private final YMLInspectorManager agentYmlInspectorManager;
    private final YMLInspectorManager appYmlInspectorManager;

    public DefaultApdexStatService(ApplicationFactory applicationFactory,
                                   ApdexScoreService apdexScoreService,
                                   @Qualifier("agentInspectorDefinition") Mappings agentInspectorDefinition,
                                   @Qualifier("applicationInspectorDefinition") Mappings applicationInspectorDefinition) {
        Objects.requireNonNull(agentInspectorDefinition, "agentInspectorDefinition");
        this.agentYmlInspectorManager = new YMLInspectorManager(agentInspectorDefinition);
        Objects.requireNonNull(applicationInspectorDefinition, "applicationInspectorDefinition");
        this.appYmlInspectorManager = new YMLInspectorManager(agentInspectorDefinition);
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
        this.apdexScoreService = Objects.requireNonNull(apdexScoreService, "apdexScoreService");
    }

    @Override
    public InspectorMetricData selectAgentStat(String applicationName, String serviceTypeName, String metricDefinitionId, String agentId, long from, long to) {
        MetricDefinition metricDefinition = agentYmlInspectorManager.findElementOfBasicGroup(metricDefinitionId);

        final Range range = Range.between(from, to);
        TimeWindow timeWindow = new TimeWindow(range, APDEX_SCORE_TIME_WINDOW_SAMPLER);
        Application application = applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);

        AgentApdexScoreChart agentApdexScoreChart = (AgentApdexScoreChart) apdexScoreService.selectAgentChart(application, timeWindow, agentId);

        return convertToInspectorMetricData(metricDefinition, agentApdexScoreChart);
    }

    @Override
    public InspectorMetricData selectApplicationStat(String applicationName, String serviceTypeName, String metricDefinitionId, long from, long to) {
        MetricDefinition metricDefinition = appYmlInspectorManager.findElementOfBasicGroup(metricDefinitionId);
        final Range range = Range.between(from, to);
        TimeWindow timeWindow = new TimeWindow(range, APDEX_SCORE_TIME_WINDOW_SAMPLER);
        Application application = applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);
        ApplicationApdexScoreChart applicationApdexScoreChart = (ApplicationApdexScoreChart) apdexScoreService.selectApplicationChart(application, timeWindow);

        return convertToInspectorMetricData(metricDefinition, applicationApdexScoreChart);
    }

    private InspectorMetricData convertToInspectorMetricData(MetricDefinition metricDefinition, ApplicationApdexScoreChart applicationApdexScoreChart) {
        StatChartGroup<ApplicationStatPoint> statChartGroup = applicationApdexScoreChart.getCharts();
        Map<StatChartGroup.ChartType, Chart<ApplicationStatPoint>> chartDatas = statChartGroup.getCharts();
        Collection<Chart<ApplicationStatPoint>> values = chartDatas.values();

        final int size = getFirstChartSize(values, (c) -> c.getPoints().size());
        if (size  == 0) {
            return new InspectorMetricData(metricDefinition.getTitle(), List.of(), List.of());
        }
        final double[] avgValueList = new double[size];
        final double[] minValueList = new double[size];
        final double[] maxValueList = new double[size];
        final long[] timestampList = new long[size];

        int i = 0;
        for (Chart<ApplicationStatPoint> chartData : values) {
            List<ApplicationStatPoint> points = chartData.getPoints();
            for (ApplicationStatPoint point : points) {
                timestampList[i] = point.getTimestamp();
                avgValueList[i] = point.getYValForAvg();
                minValueList[i] = point.getYValForMin();
                maxValueList[i] = point.getYValForMax();
                i++;
            }
        }

        Field field = metricDefinition.getFields().get(0);
        List<InspectorMetricValue> metricValueList = List.of(
            new InspectorMetricValue(MIN, field.getTags(), field.getChartType(), field.getUnit(), Doubles.asList(minValueList)),
            new InspectorMetricValue(AVG, field.getTags(), field.getChartType(), field.getUnit(), Doubles.asList(avgValueList)),
            new InspectorMetricValue(MAX, field.getTags(), field.getChartType(), field.getUnit(), Doubles.asList(maxValueList))
        );

        return new InspectorMetricData(metricDefinition.getTitle(), Longs.asList(timestampList), metricValueList);
    }

    private <T> int getFirstChartSize(Collection<T> values, ToIntFunction<T> function) {
        Iterator<T> iterator = values.iterator();
        if (iterator.hasNext()) {
            T element = iterator.next();
            return function.applyAsInt(element);
        } else {
            return 0;
        }
    }

    private InspectorMetricData convertToInspectorMetricData(MetricDefinition metricDefinition, AgentApdexScoreChart agentApdexScoreChart) {
        StatChartGroup<DataPoint<Double>> statChartGroup = agentApdexScoreChart.getCharts();
        Map<StatChartGroup.ChartType, Chart<DataPoint<Double>>> chartDatas = statChartGroup.getCharts();
        Collection<Chart<DataPoint<Double>>> values = chartDatas.values();

        final int size = getFirstChartSize(values, (c) -> c.getPoints().size());
        if (size  == 0) {
            return new InspectorMetricData(metricDefinition.getTitle(), List.of(), List.of());
        }
        final double[] avgValueList = new double[size];
        final long[] timestampList = new long[size];

        int i = 0;
        for (Chart<DataPoint<Double>> chartData : values) {
            List<DataPoint<Double>> points = chartData.getPoints();
            for (DataPoint<Double> point : points) {
                timestampList[i] = point.getTimestamp();
                avgValueList[i] = Points.asDouble(point);
                i++;
            }
        }

        Field field = metricDefinition.getFields().get(0);
        InspectorMetricValue apdexMetricValue = new InspectorMetricValue(field.getFieldAlias(), TagUtils.defaultTags(null), field.getChartType(), field.getUnit(), Doubles.asList(avgValueList));
        List<InspectorMetricValue> metricValueList = List.of(apdexMetricValue);

        return new InspectorMetricData(metricDefinition.getTitle(), Longs.asList(timestampList), metricValueList);
    }
}
