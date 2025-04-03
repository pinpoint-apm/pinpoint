package com.navercorp.pinpoint.web.vo.stat.chart;

import com.navercorp.pinpoint.common.timeseries.point.DataPoint;
import com.navercorp.pinpoint.common.timeseries.point.Points;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.FixedTimeWindowSampler;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.stat.SampledApdexScore;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentApdexScoreChart;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ChartGroupBuilderTest {

    @Test
    void build() {
        ChartGroupBuilder<SampledApdexScore, DataPoint<Double>> builder = new ChartGroupBuilder<>(SampledApdexScore::newPoint);

        builder.addPointFunction(AgentApdexScoreChart.ApdexScoreChartType.APDEX_SCORE, SampledApdexScore::getApdexScore);

        TimeWindow timeWindow = new TimeWindow(Range.between(0, 1000), new FixedTimeWindowSampler(1000));
        DataPoint<Double> point1 = Points.ofDouble(0, 100);
        DataPoint<Double> point2 = Points.ofDouble(1000, 200);
        StatChartGroup<DataPoint<Double>> chart = builder.build(timeWindow, List.of(new SampledApdexScore(point1), new SampledApdexScore(point2)));

        Chart<DataPoint<Double>> points = chart.getCharts().get(AgentApdexScoreChart.ApdexScoreChartType.APDEX_SCORE);
        assertThat(points.getPoints())
                .hasSize(2)
                .containsExactly(point1, point2);
    }
}