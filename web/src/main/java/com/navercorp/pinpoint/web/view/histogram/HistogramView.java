package com.navercorp.pinpoint.web.view.histogram;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.view.TimeSeries.TimeSeriesView;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistogramView {
    private final Histogram histogram;
    private final List<TimeHistogram> timeHistograms;

    public HistogramView(Histogram histogram, List<TimeHistogram> timeHistograms) {
        this.histogram = histogram;
        this.timeHistograms = Objects.requireNonNull(timeHistograms, "timeHistograms");
    }

    @JsonProperty("histogram")
    public Histogram getHistogram() {
        return histogram;
    }

    @JsonProperty("responseSummary")
    public ResponseTimeStatics getResponseSummary() {
        if (histogram == null) {
            return null;
        }
        return ResponseTimeStatics.fromHistogram(histogram);
    }

    @JsonProperty("loadChart")
    public TimeSeriesView getLoadChart() {
        TimeHistogramChartBuilder builder = new TimeHistogramChartBuilder(timeHistograms);
        return builder.build(TimeHistogramType.load);
    }

    @JsonProperty("loadStatisticsChart")
    public TimeSeriesView getLoadStatisticsChart() {
        TimeHistogramChartBuilder builder = new TimeHistogramChartBuilder(timeHistograms);
        return builder.build(TimeHistogramType.loadStatistics);
    }
}
