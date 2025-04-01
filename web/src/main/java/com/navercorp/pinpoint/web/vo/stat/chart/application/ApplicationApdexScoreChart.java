package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;
import java.util.function.LongFunction;

public class ApplicationApdexScoreChart extends DefaultApplicationChart<ApplicationStatPoint> {

    private static final LongFunction<ApplicationStatPoint> UNCOLLECTED_POINT
            = new ApplicationStatPoint.UncollectedCreator(-1D);

    public enum ApdexScoreChartType implements StatChartGroup.ApplicationChartType {
        APDEX_SCORE
    }

    private static final ChartGroupBuilder<ApplicationStatPoint, ApplicationStatPoint> BUILDER = newChartBuilder();

    static ChartGroupBuilder<ApplicationStatPoint, ApplicationStatPoint> newChartBuilder() {
        ChartGroupBuilder<ApplicationStatPoint, ApplicationStatPoint> builder = new ChartGroupBuilder<>(UNCOLLECTED_POINT);
        builder.addPointFunction(ApdexScoreChartType.APDEX_SCORE, ApplicationApdexScoreChart::newApdexScorePoint);
        return builder;
    }

    public ApplicationApdexScoreChart(TimeWindow timeWindow, List<ApplicationStatPoint> statList) {
        super(timeWindow, statList, BUILDER);
    }

    private static ApplicationStatPoint newApdexScorePoint(ApplicationStatPoint apdexScore) {
        return apdexScore;
    }
}
