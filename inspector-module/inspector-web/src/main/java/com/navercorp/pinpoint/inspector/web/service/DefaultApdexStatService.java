/*
 *
 *  * Copyright 2024 NAVER Corp.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
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

    private final YMLInspectorManager ymlInspectorManager;

    public DefaultApdexStatService(ApplicationFactory applicationFactory, ApdexScoreService apdexScoreService, @Qualifier("agentInspectorDefinition") Mappings agentInspectorDefinition) {
        Objects.requireNonNull(agentInspectorDefinition, "agentInspectorDefinition");
        this.ymlInspectorManager = new YMLInspectorManager(agentInspectorDefinition);
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
        this.apdexScoreService = Objects.requireNonNull(apdexScoreService, "apdexScoreService");
    }

    @Override
    public InspectorMetricData selectAgentStat(String applicationName, String serviceTypeName, String metricDefinitionId, String agentId, long from, long to) {
        MetricDefinition metricDefinition = ymlInspectorManager.findElementOfBasicGroup(metricDefinitionId);

        final Range range = Range.between(from, to);
        TimeWindow timeWindow = new TimeWindow(range, APDEX_SCORE_TIME_WINDOW_SAMPLER);
        Application application = applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);

        AgentApdexScoreChart agentApdexScoreChart = (AgentApdexScoreChart) apdexScoreService.selectAgentChart(application, range, timeWindow, agentId);

        return convertToInspectorMetricData(metricDefinition, agentApdexScoreChart);
    }

    private InspectorMetricData convertToInspectorMetricData(MetricDefinition metricDefinition, AgentApdexScoreChart agentApdexScoreChart) {
        DefaultStatChartGroup<AgentStatPoint<Double>> statChartGroup = (DefaultStatChartGroup)agentApdexScoreChart.getCharts();
        Map<StatChartGroup.ChartType, Chart<AgentStatPoint<Double>>> chartDatas = statChartGroup.getCharts();
        Collection<Chart<AgentStatPoint<Double>>> values = chartDatas.values();
        List<Double> avgList = new ArrayList<>(values.size());
        List<Long> timestampList = new ArrayList<>(values.size());

        for (Chart<AgentStatPoint<Double>> chartData : values) {
            List<AgentStatPoint<Double>> points = chartData.getPoints();
            for (AgentStatPoint<Double> point : points) {
                timestampList.add(point.getXVal());
                avgList.add(point.getAvgYVal());
            }
        }

        List<InspectorMetricValue> metricValueList = new ArrayList<>(1);
        Field field = metricDefinition.getFields().get(0);
        InspectorMetricValue apdexMetricValue = new InspectorMetricValue(field.getFieldAlias(), TagUtils.defaultTags(null), field.getChartType(), field.getUnit(), avgList);
        metricValueList.add(apdexMetricValue);

        return new InspectorMetricData(metricDefinition.getTitle(), timestampList, metricValueList);
    }
}
