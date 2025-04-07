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
        if (TimeHistogramFormat.V1 == format) {
            return new ResponseTimeViewModelBuilder(application, histogramList).build();
        }
        return new LoadTimeViewModelBuilder(histogramList).build();
    }
}
