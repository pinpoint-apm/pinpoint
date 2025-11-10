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

package com.navercorp.pinpoint.web.view.histogram;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.metric.web.view.TimeSeriesView;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApdexScore;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeName;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.util.List;
import java.util.Objects;

public class HistogramView {
    private final String key;
    private final Histogram histogram;
    private final List<TimeHistogram> sortedTimeHistograms;
    private final boolean includeTimestamp;

    public HistogramView(String key, Histogram histogram, List<TimeHistogram> timeHistograms) {
        this.key = Objects.requireNonNull(key, "key");
        this.histogram = Objects.requireNonNull(histogram, "histogram");
        this.sortedTimeHistograms = Objects.requireNonNull(timeHistograms, "timeHistograms");
        this.includeTimestamp = true;
    }

    public HistogramView(String key, Histogram histogram, List<TimeHistogram> timeHistograms, boolean includeTimestamp) {
        this.key = Objects.requireNonNull(key, "key");
        this.histogram = Objects.requireNonNull(histogram, "histogram");
        this.sortedTimeHistograms = Objects.requireNonNull(timeHistograms, "timeHistograms");
        this.includeTimestamp = includeTimestamp;
    }

    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    @JsonProperty("apdexScore")
    public ApdexScore getApdexScore() {
        return ApdexScore.newApdexScore(histogram);
    }

    @JsonProperty("histogram")
    public Histogram getHistogram() {
        return histogram;
    }

    @JsonProperty("responseSummary")
    public ResponseTimeStatics getResponseSummary() {
        return ResponseTimeStatics.fromHistogram(histogram);
    }

    @JsonProperty("loadChart")
    public TimeSeriesView getLoadChart() {
        TimeHistogramChartBuilder builder = new TimeHistogramChartBuilder(sortedTimeHistograms);
        if (!includeTimestamp) {
            builder.skipTimestamp();
        }
        return builder.build(TimeHistogramType.load);
    }

    @JsonProperty("loadStatisticsChart")
    public TimeSeriesView getLoadStatisticsChart() {
        TimeHistogramChartBuilder builder = new TimeHistogramChartBuilder(sortedTimeHistograms);
        if (!includeTimestamp) {
            builder.skipTimestamp();
        }
        return builder.build(TimeHistogramType.loadStatistics);
    }



    public static HistogramView view(NodeHistogramSummary summary) {
        Application application = summary.getApplication();
        String nodeName = NodeName.toNodeName(application.getName(), application.getServiceType());
        Histogram applicationHistogram = summary.getNodeHistogram().getApplicationHistogram();
        List<TimeHistogram> histogramList = summary.getNodeHistogram().getApplicationTimeHistogram().getHistogramList();

        return new HistogramView(nodeName, applicationHistogram, histogramList);
    }
}
