package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledApdexScore;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.InspectorData;
import com.navercorp.pinpoint.web.vo.stat.chart.InspectorDataBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

public class AgentApdexScoreChart extends DefaultAgentChart<SampledApdexScore, DoubleAgentStatPoint> {

    public enum ApdexScoreChartType implements StatChartGroup.AgentChartType {
        APDEX_SCORE
    }

    private static final ChartGroupBuilder<SampledApdexScore, DoubleAgentStatPoint> BUILDER = newChartBuilder();
    private static final InspectorDataBuilder<SampledApdexScore, DoubleAgentStatPoint> INSPECTOR_VIEW_DATA_BUILDER = newViewBuilder();

    static ChartGroupBuilder<SampledApdexScore, DoubleAgentStatPoint> newChartBuilder() {
        ChartGroupBuilder<SampledApdexScore, DoubleAgentStatPoint> builder = new ChartGroupBuilder<>(SampledApdexScore.UNCOLLECTED_POINT_CREATOR);
        builder.addPointFunction(ApdexScoreChartType.APDEX_SCORE, SampledApdexScore::getApdexScore);

        return builder;
    }

    static InspectorDataBuilder<SampledApdexScore, DoubleAgentStatPoint> newViewBuilder() {
        InspectorDataBuilder<SampledApdexScore, DoubleAgentStatPoint> builder = new InspectorDataBuilder<>(SampledApdexScore.UNCOLLECTED_POINT_CREATOR, "agentApdexScore", "noUnit");
        builder.addValueFunction("min", DoubleAgentStatPoint::getMin);
        builder.addValueFunction("max", DoubleAgentStatPoint::getMax);
        builder.addValueFunction("avg", DoubleAgentStatPoint::getAvg);
        builder.addValueFunction("sum", DoubleAgentStatPoint::getSum);
        builder.addPointFunction(ApdexScoreChartType.APDEX_SCORE, SampledApdexScore::getApdexScore);

        return builder;
    }

    public AgentApdexScoreChart(TimeWindow timeWindow, List<SampledApdexScore> statList) {
        super(timeWindow, statList, BUILDER);
    }

    public InspectorData getInspectorData(TimeWindow timeWindow, List<SampledApdexScore> statList) {
        return INSPECTOR_VIEW_DATA_BUILDER.build(timeWindow, statList);
    }
}
