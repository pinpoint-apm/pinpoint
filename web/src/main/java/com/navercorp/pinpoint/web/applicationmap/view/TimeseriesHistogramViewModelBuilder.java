package com.navercorp.pinpoint.web.applicationmap.view;

import com.google.common.primitives.Longs;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.ToLongFunction;

public class TimeseriesHistogramViewModelBuilder {
    private final Application application;
    private final List<TimeHistogram> histogramList;

    public TimeseriesHistogramViewModelBuilder(Application application, List<TimeHistogram> histogramList) {
        this.application = Objects.requireNonNull(application, "application");
        this.histogramList = Objects.requireNonNull(histogramList, "histogramList");
    }

    public List<TimeHistogramViewModel> build() {
        final List<TimeHistogramViewModel> result = new ArrayList<>(9);
        ServiceType serviceType = application.getServiceType();
        HistogramSchema schema = serviceType.getHistogramSchema();

        result.add(new TimeseriesHistogramViewModel(schema.getFastSlot().getSlotName(), getColumnValue(histogramList, Histogram::getFastCount)));
        result.add(new TimeseriesHistogramViewModel(schema.getNormalSlot().getSlotName(),  getColumnValue(histogramList, Histogram::getNormalCount)));
        result.add(new TimeseriesHistogramViewModel(schema.getSlowSlot().getSlotName(),  getColumnValue(histogramList, Histogram::getSlowCount)));
        result.add(new TimeseriesHistogramViewModel(schema.getVerySlowSlot().getSlotName(),  getColumnValue(histogramList, Histogram::getVerySlowCount)));
        result.add(new TimeseriesHistogramViewModel(schema.getTotalErrorView().getSlotName(),  getColumnValue(histogramList, Histogram::getTotalErrorCount)));

        result.add(new TimeseriesHistogramViewModel(ResponseTimeStatics.AVG_ELAPSED_TIME,  getColumnValue(histogramList, Histogram::getAvgElapsed)));

        result.add(new TimeseriesHistogramViewModel(ResponseTimeStatics.MAX_ELAPSED_TIME,  getColumnValue(histogramList, Histogram::getMaxElapsed)));
        result.add(new TimeseriesHistogramViewModel(ResponseTimeStatics.SUM_ELAPSED_TIME,  getColumnValue(histogramList, Histogram::getSumElapsed)));
        result.add(new TimeseriesHistogramViewModel(ResponseTimeStatics.TOTAL_COUNT,  getColumnValue(histogramList, Histogram::getTotalCount)));

        return result;
    }

    private List<Long> getColumnValue(List<TimeHistogram> histogramList, ToLongFunction<TimeHistogram> function) {
        long[] result = new long[histogramList.size()];
        int i = 0;
        for (TimeHistogram timeHistogram : histogramList) {
            result[i++] = function.applyAsLong(timeHistogram);
        }
        return Longs.asList(result);
    }

}
