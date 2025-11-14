package com.navercorp.pinpoint.web.applicationmap.view;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.FixedTimeWindowSampler;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindows;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApdexScore;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ApdexScoreSlotViewBuilder {
    private static final long MIN_INTERVAL = 60_000L; // 1 minute
    private static final int MAX_SLOTS = 24;

    private final Range range;
    private final Application application;
    private final List<TimeHistogram> histogramList;

    public ApdexScoreSlotViewBuilder(Range range, Application application, List<TimeHistogram> histogramList) {
        this.range = Objects.requireNonNull(range, "range");
        this.application = Objects.requireNonNull(application, "application");
        this.histogramList = Objects.requireNonNull(histogramList, "histogramList");
    }

    public List<Double> build() {
        TimeWindow timeWindow = calculateTimeWindow(range);
        List<Histogram> sumHistograms = createInitialHistogram(timeWindow, application.getServiceType());
        for (TimeHistogram timeHistogram : histogramList) {
            if (timeHistogram.getTotalCount() != 0) {
                final int index = timeWindow.getWindowIndex(timeHistogram.getTimeStamp());
                if (0 <= index && index < sumHistograms.size()) {
                    sumHistograms.get(index).add(timeHistogram);
                }
            }
        }
        return sumHistograms.stream()
                .map((histogram) -> {
                    if (histogram.getTotalCount() == 0) {
                        return -1D;
                    }
                    return ApdexScore.calculateApdexScore(histogram);
                })
                .collect(Collectors.toList());
    }

    private TimeWindow calculateTimeWindow(Range range) {
        long interval = range.durationMillis() / MAX_SLOTS;
        if (interval < MIN_INTERVAL) {
            return new TimeWindow(range, new FixedTimeWindowSampler(MIN_INTERVAL));
        }
        return new TimeWindow(range, new FixedTimeWindowSampler(interval));
    }

    private List<Histogram> createInitialHistogram(TimeWindow timeWindow, ServiceType serviceType) {
        List<Histogram> sumHistograms = TimeWindows.createInitialPoints(timeWindow, (timestamp) -> new Histogram(serviceType));
        if (sumHistograms.size() > MAX_SLOTS) {
            return sumHistograms.subList(0, MAX_SLOTS);
        }
        return sumHistograms;
    }
}
