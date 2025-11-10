/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

public class ApdexScoreSlotViewBuilder {
    public static final long MIN_INTERVAL = 60_000L; // 1 minute
    public static final int MAX_SLOTS = 24;
    public static final double UNCOLLECTED_VALUE = -1D;

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
                    if (histogram.getTotalCount() != 0) {
                        return ApdexScore.calculateApdexScore(histogram);
                    }
                    return UNCOLLECTED_VALUE;
                })
                .toList();
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
