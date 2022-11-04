package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.InspectorData;
import com.navercorp.pinpoint.web.vo.stat.chart.InspectorDataBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

public class ApplicationApdexScoreChart extends DefaultApplicationChart<DoubleApplicationStatPoint, DoubleApplicationStatPoint> {

    private static final Point.UncollectedPointCreator<DoubleApplicationStatPoint> UNCOLLECTED_POINT
            = new DoubleApplicationStatPoint.UncollectedCreator(-1D);

    public enum ApdexScoreChartType implements StatChartGroup.ApplicationChartType {
        APDEX_SCORE
    }

    private static final ChartGroupBuilder<DoubleApplicationStatPoint, DoubleApplicationStatPoint> BUILDER = newChartBuilder();
    private static final InspectorDataBuilder<DoubleApplicationStatPoint, DoubleApplicationStatPoint> INSPECTOR_VIEW_DATA_BUILDER = newViewBuilder();

    static ChartGroupBuilder<DoubleApplicationStatPoint, DoubleApplicationStatPoint> newChartBuilder() {
        ChartGroupBuilder<DoubleApplicationStatPoint, DoubleApplicationStatPoint> builder = new ChartGroupBuilder<>(UNCOLLECTED_POINT);
        builder.addPointFunction(ApdexScoreChartType.APDEX_SCORE, ApplicationApdexScoreChart::newApdexScorePoint);
        return builder;
    }

    static InspectorDataBuilder<DoubleApplicationStatPoint, DoubleApplicationStatPoint> newViewBuilder() {
        InspectorDataBuilder<DoubleApplicationStatPoint, DoubleApplicationStatPoint> builder = new InspectorDataBuilder<>(UNCOLLECTED_POINT, "applicationApdexScore", "noUnit");
        builder.addValueFunction("min", p -> p.getDoubleFieldBo().getMin());
        builder.addValueFunction("minAgentId", p -> p.getDoubleFieldBo().getMinAgentId());
        builder.addValueFunction("max", p -> p.getDoubleFieldBo().getMax());
        builder.addValueFunction("maxAgentId", p -> p.getDoubleFieldBo().getMaxAgentId());
        builder.addValueFunction("avg", p -> p.getDoubleFieldBo().getAvg());
        builder.addPointFunction(ApdexScoreChartType.APDEX_SCORE, ApplicationApdexScoreChart::newApdexScorePoint);

        return builder;
    }

    public ApplicationApdexScoreChart(TimeWindow timeWindow, List<DoubleApplicationStatPoint> statList) {
        super(timeWindow, statList, BUILDER);
    }

    private static DoubleApplicationStatPoint newApdexScorePoint(DoubleApplicationStatPoint apdexScore) {
        return apdexScore;
    }

    public InspectorData getInspectorData(TimeWindow timeWindow, List<DoubleApplicationStatPoint> statList) {
        return INSPECTOR_VIEW_DATA_BUILDER.build(timeWindow, statList);
    }
}
