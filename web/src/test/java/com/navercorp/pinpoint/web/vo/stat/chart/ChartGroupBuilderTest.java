package com.navercorp.pinpoint.web.vo.stat.chart;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.timewindow.FixedTimeWindowSampler;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.stat.SampledApdexScore;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentApdexScoreChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ChartGroupBuilderTest {

    @Test
    void build() {
        ChartGroupBuilder<SampledApdexScore, AgentStatPoint<Double>> builder = new ChartGroupBuilder<>(SampledApdexScore.UNCOLLECTED_POINT_CREATOR);

        builder.addPointFunction(AgentApdexScoreChart.ApdexScoreChartType.APDEX_SCORE, SampledApdexScore::getApdexScore);

        TimeWindow timeWindow = new TimeWindow(Range.between(0, 1000), new FixedTimeWindowSampler(1000));
        AgentStatPoint<Double> point1 = new AgentStatPoint<>(0, (double)100);
        AgentStatPoint<Double> point2 = new AgentStatPoint<>(1000, (double)200);
        StatChartGroup<AgentStatPoint<Double>> chart = builder.build(timeWindow, List.of(new SampledApdexScore(point1), new SampledApdexScore(point2)));

        Chart<AgentStatPoint<Double>> points = chart.getCharts().get(AgentApdexScoreChart.ApdexScoreChartType.APDEX_SCORE);
        assertThat(points.getPoints())
                .hasSize(2)
                .containsExactly(point1, point2);
    }
}