package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

public class ApplicationApdexScoreChart extends DefaultApplicationChart<ApplicationStatPoint> {
    public static final double UNKNOWN_SCORE = -1;
    public static final String UNKNOWN_AGENT = "unknown_agent_id";

    public enum ApdexScoreChartType implements StatChartGroup.ApplicationChartType {
        APDEX_SCORE
    }

    private static final ChartGroupBuilder<ApplicationStatPoint, ApplicationStatPoint> BUILDER = newChartBuilder();

    static ChartGroupBuilder<ApplicationStatPoint, ApplicationStatPoint> newChartBuilder() {
        ChartGroupBuilder<ApplicationStatPoint, ApplicationStatPoint> builder = new ChartGroupBuilder<>(ApplicationApdexScoreChart::newPoint);
        builder.addPointFunction(ApdexScoreChartType.APDEX_SCORE, ApplicationApdexScoreChart::newApdexScorePoint);
        return builder;
    }

    public ApplicationApdexScoreChart(TimeWindow timeWindow, List<ApplicationStatPoint> statList) {
        super(timeWindow, statList, BUILDER);
    }

    private static ApplicationStatPoint newApdexScorePoint(ApplicationStatPoint apdexScore) {
        return apdexScore;
    }

    private static ApplicationStatPoint newPoint(long timestamp) {
        return new ApplicationStatPoint(timestamp, UNKNOWN_SCORE,
                UNKNOWN_AGENT, UNKNOWN_SCORE,
                UNKNOWN_AGENT, UNKNOWN_SCORE);
    }
}
