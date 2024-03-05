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

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.inspector.web.definition.Mappings;
import com.navercorp.pinpoint.inspector.web.definition.MetricDefinition;
import com.navercorp.pinpoint.inspector.web.definition.YMLInspectorManager;
import com.navercorp.pinpoint.inspector.web.definition.metric.field.Field;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricData;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricValue;
import com.navercorp.pinpoint.metric.common.model.TagUtils;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.service.ApdexScoreService;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowSampler;
import com.navercorp.pinpoint.web.util.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentApdexScoreChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.application.ApplicationApdexScoreChart;
import com.navercorp.pinpoint.web.vo.stat.chart.application.ApplicationStatPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.application.DefaultStatChartGroup;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author minwoo-jung
 */
@Service
public class DefaultApdexStatService implements ApdexStatService {

    private static final TimeWindowSampler APDEX_SCORE_TIME_WINDOW_SAMPLER = new TimeWindowSlotCentricSampler(60 * 1000, 200);

    private final ApplicationFactory applicationFactory;

    private ApdexScoreService apdexScoreService;

    private final YMLInspectorManager agentYmlInspectorManager;
    private final YMLInspectorManager appYmlInspectorManager;

    public DefaultApdexStatService(ApplicationFactory applicationFactory, ApdexScoreService apdexScoreService, @Qualifier("agentInspectorDefinition") Mappings agentInspectorDefinition,  @Qualifier("applicationInspectorDefinition") Mappings applicationInspectorDefinition) {
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

        AgentApdexScoreChart agentApdexScoreChart = (AgentApdexScoreChart) apdexScoreService.selectAgentChart(application, range, timeWindow, agentId);

        return convertToInspectorMetricData(metricDefinition, agentApdexScoreChart);
    }

    @Override
    public InspectorMetricData selectApplicationStat(String applicationName, String serviceTypeName, String metricDefinitionId, long from, long to) {
        MetricDefinition metricDefinition = appYmlInspectorManager.findElementOfBasicGroup(metricDefinitionId);
        final Range range = Range.between(from, to);
        TimeWindow timeWindow = new TimeWindow(range, APDEX_SCORE_TIME_WINDOW_SAMPLER);
        Application application = applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);
        ApplicationApdexScoreChart applicationApdexScoreChart = (ApplicationApdexScoreChart) apdexScoreService.selectApplicationChart(application, range, timeWindow);

        return convertToInspectorMetricData(metricDefinition, applicationApdexScoreChart);
    }

    private InspectorMetricData convertToInspectorMetricData(MetricDefinition metricDefinition, ApplicationApdexScoreChart applicationApdexScoreChart) {
        DefaultStatChartGroup<ApplicationStatPoint<Double>> statChartGroup = (DefaultStatChartGroup)applicationApdexScoreChart.getCharts();
        Map<StatChartGroup.ChartType, Chart<ApplicationStatPoint<Double>>> chartDatas = statChartGroup.getCharts();
        Collection<Chart<ApplicationStatPoint<Double>>> values = chartDatas.values();
        List<Double> avgValueList = new ArrayList<>(values.size());
        List<Double> minValueList = new ArrayList<>(values.size());
        List<Double> maxValueList = new ArrayList<>(values.size());
        List<Long> timestampList = new ArrayList<>(values.size());

        for (Chart<ApplicationStatPoint<Double>> chartData : values) {
            List<ApplicationStatPoint<Double>> points = chartData.getPoints();
            for (ApplicationStatPoint<Double> point : points) {
                timestampList.add(point.getXVal());
                avgValueList.add(point.getYValForAvg());
                minValueList.add(point.getYValForMin());
                maxValueList.add(point.getYValForMax());
            }
        }

        Field field = metricDefinition.getFields().get(0);
        List<InspectorMetricValue> metricValueList = new ArrayList<>(3);
        metricValueList.add(new InspectorMetricValue("AVG", field.getTags(), field.getChartType(), field.getUnit(), avgValueList));
        metricValueList.add(new InspectorMetricValue("MIN", field.getTags(), field.getChartType(), field.getUnit(), minValueList));
        metricValueList.add(new InspectorMetricValue("MAX", field.getTags(), field.getChartType(), field.getUnit(), maxValueList));

        return new InspectorMetricData(metricDefinition.getTitle(), timestampList, metricValueList);
    }

    private InspectorMetricData convertToInspectorMetricData(MetricDefinition metricDefinition, AgentApdexScoreChart agentApdexScoreChart) {
        DefaultStatChartGroup<AgentStatPoint<Double>> statChartGroup = (DefaultStatChartGroup)agentApdexScoreChart.getCharts();
        Map<StatChartGroup.ChartType, Chart<AgentStatPoint<Double>>> chartDatas = statChartGroup.getCharts();
        Collection<Chart<AgentStatPoint<Double>>> values = chartDatas.values();
        List<Double> avgValueList = new ArrayList<>(values.size());
        List<Long> timestampList = new ArrayList<>(values.size());

        for (Chart<AgentStatPoint<Double>> chartData : values) {
            List<AgentStatPoint<Double>> points = chartData.getPoints();
            for (AgentStatPoint<Double> point : points) {
                timestampList.add(point.getXVal());
                avgValueList.add(point.getAvgYVal());
            }
        }

        List<InspectorMetricValue> metricValueList = new ArrayList<>(1);
        Field field = metricDefinition.getFields().get(0);
        InspectorMetricValue apdexMetricValue = new InspectorMetricValue(field.getFieldAlias(), TagUtils.defaultTags(null), field.getChartType(), field.getUnit(), avgValueList);
        metricValueList.add(apdexMetricValue);

        return new InspectorMetricData(metricDefinition.getTitle(), timestampList, metricValueList);
    }
}
