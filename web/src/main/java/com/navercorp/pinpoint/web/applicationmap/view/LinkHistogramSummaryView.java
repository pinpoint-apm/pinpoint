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

import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.link.LinkHistogramSummary;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.util.List;
import java.util.Objects;

public class LinkHistogramSummaryView {
    private final LinkHistogramSummary linkHistogramSummary;
    private final TimeWindow timeWindow;
    private final TimeHistogramView timeHistogramView;

    public LinkHistogramSummaryView(LinkHistogramSummary linkHistogramSummary,
                                    TimeWindow timeWindow,
                                    TimeHistogramView timeHistogramView) {
        this.linkHistogramSummary = Objects.requireNonNull(linkHistogramSummary, "linkHistogramSummary");
        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
        this.timeHistogramView = Objects.requireNonNull(timeHistogramView, "timeHistogramView");
    }

    public String getKey() {
        return linkHistogramSummary.getLinkName().getName();
    }

    public Histogram getHistogram() {
        return linkHistogramSummary.getHistogram();
    }

    public List<TimeHistogramViewModel> getTimeSeriesHistogram() {
        ApplicationTimeHistogram histogram = linkHistogramSummary.getLinkApplicationTimeHistogram();
        return timeHistogramView.build(histogram);
    }

    public List<Long> getTimestamp() {
        return timeWindow.getTimeseriesWindows();
    }

    public ResponseTimeStatics getResponseStatistics() {
        Histogram histogram = linkHistogramSummary.getHistogram();
        return ResponseTimeStatics.fromHistogram(histogram);
    }
}
