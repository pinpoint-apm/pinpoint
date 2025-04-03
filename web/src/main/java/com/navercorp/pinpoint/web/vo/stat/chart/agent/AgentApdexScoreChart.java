package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.navercorp.pinpoint.common.timeseries.point.DataPoint;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledApdexScore;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

public class AgentApdexScoreChart extends DefaultAgentChart<SampledApdexScore> {

    public enum ApdexScoreChartType implements StatChartGroup.AgentChartType {
        APDEX_SCORE
    }

    private static final ChartGroupBuilder<SampledApdexScore, DataPoint<Double>> BUILDER = newChartBuilder();

    static ChartGroupBuilder<SampledApdexScore, DataPoint<Double>> newChartBuilder() {
        ChartGroupBuilder<SampledApdexScore, DataPoint<Double>> builder = new ChartGroupBuilder<>(SampledApdexScore::newPoint);
        builder.addPointFunction(ApdexScoreChartType.APDEX_SCORE, SampledApdexScore::getApdexScore);

        return builder;
    }

    public AgentApdexScoreChart(TimeWindow timeWindow, List<SampledApdexScore> statList) {
        super(timeWindow, statList, BUILDER);
    }
}
