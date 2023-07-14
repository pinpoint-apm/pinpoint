package com.navercorp.pinpoint.web.view.histogram;

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.view.TimeSeries.TimeSeriesValueGroupView;
import com.navercorp.pinpoint.web.view.TimeSeries.TimeSeriesValueView;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TimeHistogramChartBuilder {
    private final List<TimeHistogram> timeHistograms;

    public TimeHistogramChartBuilder(List<TimeHistogram> timeHistograms) {
        this.timeHistograms = Objects.requireNonNull(timeHistograms, "timeHistograms");
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
        List<TimeSeriesValueView> metricValueList = createLoadValueList(timeHistograms);
        return new TimeHistogramChart("Load", getTimeStampList(timeHistograms),
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
        List<TimeSeriesValueView> metricValueList = createLoadStatisticsValueList(timeHistograms);
        return new TimeHistogramChart("Load Avg & Max", getTimeStampList(timeHistograms),
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
