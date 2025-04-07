package com.navercorp.pinpoint.web.applicationmap.view;

import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.List;
import java.util.Objects;

public class TimeHistogramBuilder {

    private final TimeHistogramFormat format;

    public TimeHistogramBuilder(TimeHistogramFormat format) {
        this.format = Objects.requireNonNull(format, "format");
    }

    public List<TimeHistogramViewModel> build(Application application, List<TimeHistogram> histogramList) {
        return switch (format) {
            case V1 -> new ResponseTimeViewModelBuilder(application, histogramList).build();
            case V2 -> new LoadTimeViewModelBuilder(histogramList).build();
            case V3 -> new TimeseriesHistogramViewModelBuilder(application, histogramList).build();
        };
    }
}
