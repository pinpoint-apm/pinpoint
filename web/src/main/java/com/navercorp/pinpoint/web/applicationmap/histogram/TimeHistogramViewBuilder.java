package com.navercorp.pinpoint.web.applicationmap.histogram;

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.view.timeseries.TimeSeriesData;
import com.navercorp.pinpoint.web.view.timeseries.TimeSeriesValue;
import com.navercorp.pinpoint.web.view.timeseries.TimeSeriesValueGroup;
import com.navercorp.pinpoint.web.view.timeseries.TimeSeriesView;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TimeHistogramViewBuilder {
    private final Application application;
    private final List<TimeHistogram> histogramList;

    private String title;
    private String unit = null;

    public TimeHistogramViewBuilder(Application application, List<TimeHistogram> histogramList) {
        this.application = Objects.requireNonNull(application, "application");
        this.histogramList = Objects.requireNonNull(histogramList, "histogramList");
        this.title = application.getName();
    }

    public TimeHistogramViewBuilder setTitle(String title) {
        this.title = Objects.requireNonNull(title, "title");
        return this;
    }

    public TimeHistogramViewBuilder setUnit(String unit) {
        this.unit = Objects.requireNonNull(unit, "unit");
        return this;
    }

    public TimeSeriesView build() {
        setDefaultUnit();

        List<TimeSeriesValue> timeSeriesCountValueList = createTimeSeriesCountValueList(application.getServiceType(), histogramList);
        List<TimeSeriesValue> timeSeriesTimeValueList = createTimeSeriesTimeValueList(histogramList);

        List<TimeSeriesValueGroup> timeSeriesValueGroupList = new ArrayList<>();
        timeSeriesValueGroupList.add(new TimeSeriesValueGroup(timeSeriesCountValueList, "countValues"));
        timeSeriesValueGroupList.add(new TimeSeriesValueGroup(timeSeriesTimeValueList, "timeValues"));

        List<Long> timeStampList = getTimeStampList();
        TimeSeriesData timeSeriesData = new TimeSeriesData(title, unit, timeStampList, timeSeriesValueGroupList);
        return new TimeSeriesView(timeSeriesData);
    }

    private List<TimeSeriesValue> createTimeSeriesCountValueList(ServiceType serviceType, List<TimeHistogram> histogramList) {
        List<TimeSeriesValue> timeSeriesValueList = new ArrayList<>(6);
        HistogramSchema schema = serviceType.getHistogramSchema();
        timeSeriesValueList.add(new TimeSeriesValue(schema.getFastSlot().getSlotName(), Collections.emptyList(), applyFunction(histogramList, Histogram::getFastCount)));
        timeSeriesValueList.add(new TimeSeriesValue(schema.getNormalSlot().getSlotName(), Collections.emptyList(), applyFunction(histogramList, Histogram::getNormalCount)));
        timeSeriesValueList.add(new TimeSeriesValue(schema.getSlowSlot().getSlotName(), Collections.emptyList(), applyFunction(histogramList, Histogram::getSlowCount)));
        timeSeriesValueList.add(new TimeSeriesValue(schema.getVerySlowSlot().getSlotName(), Collections.emptyList(), applyFunction(histogramList, Histogram::getVerySlowCount)));
        timeSeriesValueList.add(new TimeSeriesValue(schema.getErrorSlot().getSlotName(), Collections.emptyList(), applyFunction(histogramList, Histogram::getErrorCount)));
        timeSeriesValueList.add(new TimeSeriesValue(ResponseTimeStatics.TOTAL_COUNT, Collections.emptyList(), applyFunction(histogramList, Histogram::getTotalCount)));

        return timeSeriesValueList;
    }

    private List<TimeSeriesValue> createTimeSeriesTimeValueList(List<TimeHistogram> histogramList) {
        List<TimeSeriesValue> timeSeriesValueList = new ArrayList<>(3);
        timeSeriesValueList.add(new TimeSeriesValue(ResponseTimeStatics.AVG_ELAPSED_TIME, Collections.emptyList(), applyFunction(histogramList, Histogram::getAvgElapsed)));
        timeSeriesValueList.add(new TimeSeriesValue(ResponseTimeStatics.MAX_ELAPSED_TIME, Collections.emptyList(), applyFunction(histogramList, Histogram::getMaxElapsed)));
        timeSeriesValueList.add(new TimeSeriesValue(ResponseTimeStatics.SUM_ELAPSED_TIME, Collections.emptyList(), applyFunction(histogramList, Histogram::getSumElapsed)));

        return timeSeriesValueList;
    }

    private List<Long> applyFunction(List<TimeHistogram> histogramList, Function<TimeHistogram, Long> function) {
        return histogramList
                .stream()
                .map(function)
                .collect(Collectors.toList());
    }

    private List<Long> getTimeStampList() {
        return histogramList.stream()
                .map(TimeHistogram::getTimeStamp)
                .collect(Collectors.toList());
    }

    private void setDefaultUnit() {
        if (unit == null) {
            unit = "count, ms";
        }
    }
}


