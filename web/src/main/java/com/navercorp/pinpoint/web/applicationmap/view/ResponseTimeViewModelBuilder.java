package com.navercorp.pinpoint.web.applicationmap.view;

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.SlotType;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

        result.add(new ResponseTimeViewModel(schema.getFastSlot().getSlotName(), getColumnValue(histogramList, SlotType.FAST)));
        result.add(new ResponseTimeViewModel(schema.getNormalSlot().getSlotName(), getColumnValue(histogramList, SlotType.NORMAL)));
        result.add(new ResponseTimeViewModel(schema.getSlowSlot().getSlotName(), getColumnValue(histogramList, SlotType.SLOW)));
        result.add(new ResponseTimeViewModel(schema.getVerySlowSlot().getSlotName(), getColumnValue(histogramList, SlotType.VERY_SLOW)));
        result.add(new ResponseTimeViewModel(schema.getErrorSlot().getSlotName(), getColumnValue(histogramList, SlotType.ERROR)));
        result.add(new ResponseTimeViewModel(ResponseTimeStatics.AVG_ELAPSED_TIME, getAvgValue(histogramList)));
        result.add(new ResponseTimeViewModel(ResponseTimeStatics.MAX_ELAPSED_TIME, getColumnValue(histogramList, SlotType.MAX_STAT)));
        result.add(new ResponseTimeViewModel(ResponseTimeStatics.SUM_ELAPSED_TIME, getColumnValue(histogramList, SlotType.SUM_STAT)));
        result.add(new ResponseTimeViewModel(ResponseTimeStatics.TOTAL_COUNT, getTotalCount(histogramList)));

        return result;
    }

    private List<TimeCount> getColumnValue(List<TimeHistogram> histogramList, SlotType slotType) {
        List<TimeCount> result = new ArrayList<>(histogramList.size());
        for (TimeHistogram timeHistogram : histogramList) {
            final long timeStamp = timeHistogram.getTimeStamp();
            TimeCount TimeCount = new TimeCount(timeStamp, getCount(timeHistogram, slotType));
            result.add(TimeCount);
        }
        return result;
    }

    private List<TimeCount> getAvgValue(List<TimeHistogram> histogramList) {
        List<TimeCount> result = new ArrayList<>(histogramList.size());
        for (TimeHistogram timeHistogram : histogramList) {
            final long timeStamp = timeHistogram.getTimeStamp();
            final long totalCount = timeHistogram.getTotalCount();
            final long sumElapsed = getCount(timeHistogram, SlotType.SUM_STAT);
            final long avgElapsed = totalCount > 0 ? sumElapsed / totalCount : 0L;

            result.add(new TimeCount(timeStamp, avgElapsed));
        }
        return result;
    }

    private List<TimeCount> getTotalCount(List<TimeHistogram> histogramList) {
        List<TimeCount> result = new ArrayList<>(histogramList.size());
        for (TimeHistogram timeHistogram : histogramList) {
            final long timeStamp = timeHistogram.getTimeStamp();
            final long totalCount = timeHistogram.getTotalCount();
            result.add(new TimeCount(timeStamp, totalCount));
        }
        return result;
    }

    private long getCount(TimeHistogram timeHistogram, SlotType slotType) {
        return timeHistogram.getCount(slotType);
    }
}
