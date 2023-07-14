package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledApdexScore;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

public class AgentApdexScoreChart extends DefaultAgentChart<SampledApdexScore, Double> {

    public enum ApdexScoreChartType implements StatChartGroup.AgentChartType {
        APDEX_SCORE
    }

    private static final ChartGroupBuilder<SampledApdexScore, AgentStatPoint<Double>> BUILDER = newChartBuilder();

    static ChartGroupBuilder<SampledApdexScore, AgentStatPoint<Double>> newChartBuilder() {
        ChartGroupBuilder<SampledApdexScore, AgentStatPoint<Double>> builder = new ChartGroupBuilder<>(SampledApdexScore.UNCOLLECTED_POINT_CREATOR);
        builder.addPointFunction(ApdexScoreChartType.APDEX_SCORE, SampledApdexScore::getApdexScore);

        return builder;
    }

    public AgentApdexScoreChart(TimeWindow timeWindow, List<SampledApdexScore> statList) {
        super(timeWindow, statList, BUILDER);
    }
}
