package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

public class ApplicationApdexScoreChart extends DefaultApplicationChart<DoubleApplicationStatPoint, Double> {

    private static final Point.UncollectedPointCreator<ApplicationStatPoint<Double>> UNCOLLECTED_POINT
            = new DoubleApplicationStatPoint.UncollectedCreator(-1D);

    public enum ApdexScoreChartType implements StatChartGroup.ApplicationChartType {
        APDEX_SCORE
    }

    private static final ChartGroupBuilder<DoubleApplicationStatPoint, ApplicationStatPoint<Double>> BUILDER = newChartBuilder();

    static ChartGroupBuilder<DoubleApplicationStatPoint, ApplicationStatPoint<Double>> newChartBuilder() {
        ChartGroupBuilder<DoubleApplicationStatPoint, ApplicationStatPoint<Double>> builder = new ChartGroupBuilder<>(UNCOLLECTED_POINT);
        builder.addPointFunction(ApdexScoreChartType.APDEX_SCORE, ApplicationApdexScoreChart::newApdexScorePoint);
        return builder;
    }

    public ApplicationApdexScoreChart(TimeWindow timeWindow, List<DoubleApplicationStatPoint> statList) {
        super(timeWindow, statList, BUILDER);
    }

    private static ApplicationStatPoint<Double> newApdexScorePoint(DoubleApplicationStatPoint apdexScore) {
        return apdexScore;
    }
}
