package com.navercorp.pinpoint.web.applicationmap.view;

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

public class ResponseTimeViewModelBuilder {
    private final Application application;
    private final List<TimeHistogram> histogramList;

    public ResponseTimeViewModelBuilder(Application application, List<TimeHistogram> histogramList) {
        this.application = Objects.requireNonNull(application, "application");
        this.histogramList = Objects.requireNonNull(histogramList, "histogramList");
    }

    public List<TimeHistogramViewModel> build() {
        final List<TimeHistogramViewModel> result = new ArrayList<>(9);
        ServiceType serviceType = application.getServiceType();
        HistogramSchema schema = serviceType.getHistogramSchema();

        result.add(new ResponseTimeViewModel(schema.getFastSlot().getSlotName(), getColumnValue(histogramList, Histogram::getFastCount)));
        result.add(new ResponseTimeViewModel(schema.getNormalSlot().getSlotName(), getColumnValue(histogramList, Histogram::getNormalCount)));
        result.add(new ResponseTimeViewModel(schema.getSlowSlot().getSlotName(), getColumnValue(histogramList, Histogram::getSlowCount)));
        result.add(new ResponseTimeViewModel(schema.getVerySlowSlot().getSlotName(), getColumnValue(histogramList, Histogram::getVerySlowCount)));

        result.add(new ResponseTimeViewModel(schema.getTotalErrorView().getSlotName(), getColumnValue(histogramList, Histogram::getTotalErrorCount)));

        result.add(new ResponseTimeViewModel(ResponseTimeStatics.AVG_ELAPSED_TIME, getColumnValue(histogramList, Histogram::getAvgElapsed)));
        result.add(new ResponseTimeViewModel(ResponseTimeStatics.MAX_ELAPSED_TIME, getColumnValue(histogramList, Histogram::getMaxElapsed)));
        result.add(new ResponseTimeViewModel(ResponseTimeStatics.SUM_ELAPSED_TIME, getColumnValue(histogramList, Histogram::getSumElapsed)));
        result.add(new ResponseTimeViewModel(ResponseTimeStatics.TOTAL_COUNT, getColumnValue(histogramList, Histogram::getTotalCount)));

        return result;
    }

    private List<TimeCount> getColumnValue(List<TimeHistogram> histogramList, ToLongFunction<TimeHistogram> function) {
        List<TimeCount> result = new ArrayList<>(histogramList.size());
        for (TimeHistogram timeHistogram : histogramList) {
            final long timeStamp = timeHistogram.getTimeStamp();
            final long count = function.applyAsLong(timeHistogram);
            TimeCount TimeCount = new TimeCount(timeStamp, count);
            result.add(TimeCount);
        }
        return result;
    }

}
