package com.navercorp.pinpoint.web.view;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.UncollectedPointCreatorFactory;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.InspectorData;
import com.navercorp.pinpoint.web.vo.stat.chart.InspectorDataBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InspectorViewTest {

    private enum TestChartType implements StatChartGroup.AgentChartType {
        TEST_CHART_TYPE
    }

    private static class TestAgentStatDataPoint implements SampledAgentStatDataPoint {

        public static final Point.UncollectedPointCreator<AgentStatPoint<Integer>> UNCOLLECTED_POINT_CREATOR = UncollectedPointCreatorFactory.createIntPointCreator(-1);

        private final AgentStatPoint<Integer> agentStatPoint;

        public TestAgentStatDataPoint(AgentStatPoint<Integer> agentStatPoint) {
            this.agentStatPoint = agentStatPoint;
        }

        public AgentStatPoint<Integer> getAgentStatPoint() {
            return agentStatPoint;
        }
    }

    @Test
    public void inspectorViewTest() {
        String title = "testTitle";
        String unit = "testUnit";
        Map<String, Function<AgentStatPoint<Integer>, ?>> valueFunctionMap = new HashMap<>();
        valueFunctionMap.put("function1", AgentStatPoint::getMinYVal);
        valueFunctionMap.put("function2", AgentStatPoint::getMinYVal);
        valueFunctionMap.put("function3", AgentStatPoint::getMinYVal);
        Range range = Range.between(0, 1000 * 60);
        TimeWindow timeWindow = new TimeWindow(range, new TimeWindowSlotCentricSampler());
        List<TestAgentStatDataPoint> testAgentStatDataPoints = createTestAgentStatDataPoint();

        InspectorDataBuilder<TestAgentStatDataPoint, AgentStatPoint<Integer>> inspectorDataBuilder = new InspectorDataBuilder<>(TestAgentStatDataPoint.UNCOLLECTED_POINT_CREATOR, title, unit);
        for (Map.Entry<String, Function<AgentStatPoint<Integer>, ?>> e : valueFunctionMap.entrySet()) {
            inspectorDataBuilder.addValueFunction(e.getKey(), e.getValue());
        }
        inspectorDataBuilder.addPointFunction(TestChartType.TEST_CHART_TYPE, TestAgentStatDataPoint::getAgentStatPoint);

        InspectorData inspectorData = inspectorDataBuilder.build(timeWindow, testAgentStatDataPoints);
        InspectorView inspectorView = new InspectorView(inspectorData);

        assertEquals(inspectorView.getTitle(), title);
        assertEquals(inspectorView.getUnit(), unit);
        assertThat(inspectorView.getTimestamp()).hasSize((int) timeWindow.getWindowRangeCount());
        assertThat(inspectorView.getMetricValueGroups()).hasSameSizeAs(TestChartType.values());

        InspectorView.InspectorValueGroupView inspectorValueGroupView = inspectorView.getMetricValueGroups().get(0);
        assertThat(inspectorValueGroupView.getMetricValues()).hasSameSizeAs(valueFunctionMap.values());
    }

    private List<TestAgentStatDataPoint> createTestAgentStatDataPoint() {
        return List.of(
                new TestAgentStatDataPoint(new AgentStatPoint<>(0, 1)),
                new TestAgentStatDataPoint(new AgentStatPoint<>(1000 * 60, 2))
        );
    }
}
