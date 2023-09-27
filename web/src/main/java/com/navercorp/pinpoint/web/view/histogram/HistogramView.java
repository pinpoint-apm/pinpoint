/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.view.histogram;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApdexScore;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.link.LinkName;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeName;
import com.navercorp.pinpoint.web.view.TimeSeries.TimeSeriesView;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.util.List;
import java.util.Objects;

public class HistogramView {
    private final String key;
    private final Histogram histogram;
    private final List<TimeHistogram> sortedTimeHistograms;

    public HistogramView(String key, Histogram histogram, List<TimeHistogram> timeHistograms) {
        this.key = Objects.requireNonNull(key, "key");
        this.histogram = Objects.requireNonNull(histogram, "histogram");
        this.sortedTimeHistograms = Objects.requireNonNull(timeHistograms, "timeHistograms");
    }

    public HistogramView(NodeName nodeName, NodeHistogram nodeHistogram) {
        this(nodeName.getName(), nodeHistogram.getApplicationHistogram(), nodeHistogram.getApplicationTimeHistogram().getHistogramList());
    }

    public HistogramView(LinkName linkName, Histogram histogram, ApplicationTimeHistogram applicationTimeHistogram) {
        this(linkName.getName(), histogram, applicationTimeHistogram.getHistogramList());
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
        return builder.build(TimeHistogramType.load);
    }

    @JsonProperty("loadStatisticsChart")
    public TimeSeriesView getLoadStatisticsChart() {
        TimeHistogramChartBuilder builder = new TimeHistogramChartBuilder(sortedTimeHistograms);
        return builder.build(TimeHistogramType.loadStatistics);
    }
}
