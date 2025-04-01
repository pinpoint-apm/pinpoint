package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledApdexScore;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

public class AgentApdexScoreChart extends DefaultAgentChart<SampledApdexScore> {

    public enum ApdexScoreChartType implements StatChartGroup.AgentChartType {
        APDEX_SCORE
    }

    private static final ChartGroupBuilder<SampledApdexScore, AgentStatPoint> BUILDER = newChartBuilder();

    static ChartGroupBuilder<SampledApdexScore, AgentStatPoint> newChartBuilder() {
        ChartGroupBuilder<SampledApdexScore, AgentStatPoint> builder = new ChartGroupBuilder<>(SampledApdexScore::newPoint);
        builder.addPointFunction(ApdexScoreChartType.APDEX_SCORE, SampledApdexScore::getApdexScore);

        return builder;
    }

    public AgentApdexScoreChart(TimeWindow timeWindow, List<SampledApdexScore> statList) {
        super(timeWindow, statList, BUILDER);
    }
}
