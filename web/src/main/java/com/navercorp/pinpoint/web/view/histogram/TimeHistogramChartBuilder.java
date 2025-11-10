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

import com.navercorp.pinpoint.common.timeseries.array.LongArray;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.metric.web.view.TimeSeriesValueView;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TimeHistogramChartBuilder {
    private final List<TimeHistogram> orderedTimeHistograms;
    private boolean includeTimestamp = true;

    public TimeHistogramChartBuilder(List<TimeHistogram> orderedTimeHistograms) {
        this.orderedTimeHistograms = Objects.requireNonNull(orderedTimeHistograms, "orderedTimeHistograms");
    }

    public TimeHistogramChartBuilder skipTimestamp() {
        this.includeTimestamp = false;
        return this;
    }

    public TimeHistogramChart build(TimeHistogramType timeHistogramType) {
        if (timeHistogramType == TimeHistogramType.load) {
            return buildLoadChart();
        } else if (timeHistogramType == TimeHistogramType.loadStatistics) {
            return buildLoadStatisticsChart();
        }
        return new TimeHistogramChart("empty", Collections.emptyList(), Collections.emptyList());
    }

    public TimeHistogramChart buildLoadChart() {
        List<TimeSeriesValueView> metricValueList = createLoadValueList(orderedTimeHistograms);
        List<Long> timestamp = getTimestamp();
        return new TimeHistogramChart("Load", timestamp,
                List.of(new TimeHistogramValueGroupView("load", "count", TimeHistogramChartType.AREA_STEP, metricValueList)));
    }

    private List<TimeSeriesValueView> createLoadValueList(List<TimeHistogram> histogramList) {
        if (histogramList.isEmpty()) {
            return Collections.emptyList();
        }
        HistogramSchema schema = histogramList.get(0).getHistogramSchema();
        return List.of(
                new TimeHistogramValueView(schema.getFastSlot().getSlotName(), LongArray.asList(histogramList, Histogram::getFastCount)),
                new TimeHistogramValueView(schema.getNormalSlot().getSlotName(), LongArray.asList(histogramList, Histogram::getNormalCount)),
                new TimeHistogramValueView(schema.getSlowSlot().getSlotName(), LongArray.asList(histogramList, Histogram::getSlowCount)),
                new TimeHistogramValueView(schema.getVerySlowSlot().getSlotName(), LongArray.asList(histogramList, Histogram::getVerySlowCount)),
                new TimeHistogramValueView(schema.getTotalErrorView().getSlotName(), LongArray.asList(histogramList, Histogram::getTotalErrorCount))
        );
    }

    public TimeHistogramChart buildLoadStatisticsChart() {
        List<TimeSeriesValueView> metricValueList = createLoadStatisticsValueList(orderedTimeHistograms);
        List<Long> timestamp = getTimestamp();
        return new TimeHistogramChart("Load Avg & Max", timestamp,
                List.of(new TimeHistogramValueGroupView("loadStatistics", "ms", TimeHistogramChartType.AREA_STEP, metricValueList)));
    }

    private List<Long> getTimestamp() {
        if (includeTimestamp) {
            return LongArray.asList(orderedTimeHistograms, TimeHistogram::getTimeStamp);
        }
        return null;
    }

    private List<TimeSeriesValueView> createLoadStatisticsValueList(List<TimeHistogram> histogramList) {

        return List.of(
                new TimeHistogramValueView(ResponseTimeStatics.AVG_ELAPSED_TIME, LongArray.asList(histogramList, Histogram::getAvgElapsed)),
                new TimeHistogramValueView(ResponseTimeStatics.MAX_ELAPSED_TIME, LongArray.asList(histogramList, Histogram::getMaxElapsed))
        );
    }


}
