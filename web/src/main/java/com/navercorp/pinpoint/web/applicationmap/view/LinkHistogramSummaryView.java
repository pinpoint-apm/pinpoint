package com.navercorp.pinpoint.web.applicationmap.view;

import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.link.LinkHistogramSummary;

import java.util.List;
import java.util.Objects;

public class LinkHistogramSummaryView {
    private final LinkHistogramSummary linkHistogramSummary;
    private final TimeHistogramFormat format;

    public LinkHistogramSummaryView(LinkHistogramSummary linkHistogramSummary, TimeHistogramFormat format) {
        this.linkHistogramSummary = Objects.requireNonNull(linkHistogramSummary, "linkHistogramSummary");
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
        return histogram.createViewModel(format);
    }
}
