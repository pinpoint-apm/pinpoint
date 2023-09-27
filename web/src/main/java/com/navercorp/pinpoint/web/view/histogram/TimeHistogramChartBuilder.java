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

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.view.TimeSeries.TimeSeriesValueView;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TimeHistogramChartBuilder {
    private final List<TimeHistogram> orderedTimeHistograms;

    public TimeHistogramChartBuilder(List<TimeHistogram> orderedTimeHistograms) {
        this.orderedTimeHistograms = Objects.requireNonNull(orderedTimeHistograms, "orderedTimeHistograms");
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
        return new TimeHistogramChart("Load", getTimeStampList(orderedTimeHistograms),
                List.of(new TimeHistogramValueGroupView("load", "count", "area-step", metricValueList)));
    }

    private List<TimeSeriesValueView> createLoadValueList(List<TimeHistogram> histogramList) {
        if (histogramList.isEmpty()) {
            return Collections.emptyList();
        }
        HistogramSchema schema = histogramList.get(0).getHistogramSchema();
        return List.of(
                new TimeHistogramValueView(schema.getFastSlot().getSlotName(), getValueList(histogramList, Histogram::getFastCount)),
                new TimeHistogramValueView(schema.getNormalSlot().getSlotName(), getValueList(histogramList, Histogram::getNormalCount)),
                new TimeHistogramValueView(schema.getSlowSlot().getSlotName(), getValueList(histogramList, Histogram::getSlowCount)),
                new TimeHistogramValueView(schema.getVerySlowSlot().getSlotName(), getValueList(histogramList, Histogram::getVerySlowCount)),
                new TimeHistogramValueView(schema.getErrorSlot().getSlotName(), getValueList(histogramList, Histogram::getErrorCount))
        );
    }

    public TimeHistogramChart buildLoadStatisticsChart() {
        List<TimeSeriesValueView> metricValueList = createLoadStatisticsValueList(orderedTimeHistograms);
        return new TimeHistogramChart("Load Avg & Max", getTimeStampList(orderedTimeHistograms),
                List.of(new TimeHistogramValueGroupView("loadStatistics", "ms", "area-step", metricValueList)));
    }

    private List<TimeSeriesValueView> createLoadStatisticsValueList(List<TimeHistogram> histogramList) {
        return List.of(
                new TimeHistogramValueView(ResponseTimeStatics.AVG_ELAPSED_TIME, getValueList(histogramList, Histogram::getAvgElapsed)),
                new TimeHistogramValueView(ResponseTimeStatics.MAX_ELAPSED_TIME, getValueList(histogramList, Histogram::getMaxElapsed))
        );
    }


    private List<Long> getTimeStampList(List<TimeHistogram> histogramList) {
        return histogramList.stream()
                .map(TimeHistogram::getTimeStamp)
                .collect(Collectors.toList());
    }

    public List<? extends Number> getValueList(List<TimeHistogram> histogramList, Function<TimeHistogram, Number> function) {
        return histogramList
                .stream()
                .map(function)
                .collect(Collectors.toList());
    }

}
