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
import com.navercorp.pinpoint.web.vo.stat.chart.agent.IntAgentStatPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class InspectorViewTest {

    private enum TestChartType implements StatChartGroup.AgentChartType {
        TEST_CHART_TYPE
    }

    private static class TestAgentStatDataPoint implements SampledAgentStatDataPoint {

        public static final Point.UncollectedPointCreator<IntAgentStatPoint> UNCOLLECTED_POINT_CREATOR = UncollectedPointCreatorFactory.createIntPointCreator(-1);

        private final IntAgentStatPoint agentStatPoint;

        public TestAgentStatDataPoint(IntAgentStatPoint agentStatPoint) {
            this.agentStatPoint = agentStatPoint;
        }

        public IntAgentStatPoint getAgentStatPoint() {
            return agentStatPoint;
        }
    }

    @Test
    public void inspectorViewTest() {
        String title = "testTitle";
        String unit = "testUnit";
        Map<String, Function<IntAgentStatPoint, ?>> valueFunctionMap = new HashMap<>();
        valueFunctionMap.put("function1", IntAgentStatPoint::getMin);
        valueFunctionMap.put("function2", IntAgentStatPoint::getMin);
        valueFunctionMap.put("function3", IntAgentStatPoint::getMin);
        Range range = Range.between(0, 1000 * 60);
        TimeWindow timeWindow = new TimeWindow(range, new TimeWindowSlotCentricSampler());
        List<TestAgentStatDataPoint> testAgentStatDataPoints = createTestAgentStatDataPoint();

        InspectorDataBuilder<TestAgentStatDataPoint, IntAgentStatPoint> inspectorDataBuilder = new InspectorDataBuilder<>(TestAgentStatDataPoint.UNCOLLECTED_POINT_CREATOR, title, unit);
        for (Map.Entry<String, Function<IntAgentStatPoint, ?>> e : valueFunctionMap.entrySet()) {
            inspectorDataBuilder.addValueFunction(e.getKey(), e.getValue());
        }
        inspectorDataBuilder.addPointFunction(TestChartType.TEST_CHART_TYPE, TestAgentStatDataPoint::getAgentStatPoint);

        InspectorData inspectorData = inspectorDataBuilder.build(timeWindow, testAgentStatDataPoints);
        InspectorView inspectorView = new InspectorView(inspectorData);

        Assertions.assertEquals(inspectorView.getTitle(), title);
        Assertions.assertEquals(inspectorView.getUnit(), unit);
        Assertions.assertEquals(inspectorView.getTimestamp().size(), timeWindow.getWindowRangeCount());
        Assertions.assertEquals(inspectorView.getMetricValueGroups().size(), TestChartType.values().length);
        InspectorView.InspectorValueGroupView inspectorValueGroupView = inspectorView.getMetricValueGroups().get(0);
        Assertions.assertEquals(inspectorValueGroupView.getMetricValues().size(), valueFunctionMap.size());
    }

    private List<TestAgentStatDataPoint> createTestAgentStatDataPoint() {
        List<TestAgentStatDataPoint> testAgentStatDataPoints = new ArrayList<>();
        testAgentStatDataPoints.add(new TestAgentStatDataPoint(IntAgentStatPoint.ofSingle(0, 1)));
        testAgentStatDataPoints.add(new TestAgentStatDataPoint(IntAgentStatPoint.ofSingle(1000 * 60, 2)));

        return testAgentStatDataPoints;
    }
}
