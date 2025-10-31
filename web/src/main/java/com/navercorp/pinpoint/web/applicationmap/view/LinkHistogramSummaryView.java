package com.navercorp.pinpoint.web.applicationmap.view;

import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.link.LinkHistogramSummary;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.util.List;
import java.util.Objects;

public class LinkHistogramSummaryView {
    private final LinkHistogramSummary linkHistogramSummary;
    private final TimeWindow timeWindow;
    private final TimeHistogramFormat format;

    public LinkHistogramSummaryView(LinkHistogramSummary linkHistogramSummary,
                                    TimeWindow timeWindow,
                                    TimeHistogramFormat format) {
        this.linkHistogramSummary = Objects.requireNonNull(linkHistogramSummary, "linkHistogramSummary");
        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
        this.format = Objects.requireNonNull(format, "format");
    }

    public String getKey() {
        return linkHistogramSummary.getLinkName().getName();
    }

    public Histogram getHistogram() {
        return linkHistogramSummary.getHistogram();
    }

    public List<TimeHistogramViewModel> getTimeSeriesHistogram() {
        ApplicationTimeHistogram histogram = linkHistogramSummary.getLinkApplicationTimeHistogram();
        TimeHistogramBuilder builder = new TimeHistogramBuilder(format);
        return builder.build(histogram);
    }

    public List<Long> getTimestamp() {
        return timeWindow.getTimeseriesWindows();
    }

    public ResponseTimeStatics getResponseStatistics() {
        Histogram histogram = linkHistogramSummary.getHistogram();
        return ResponseTimeStatics.fromHistogram(histogram);
    }
}
