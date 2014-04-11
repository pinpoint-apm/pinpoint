package com.nhn.pinpoint.web.applicationmap.histogram;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.SlotType;
import com.nhn.pinpoint.web.view.ResponseTimeViewModel;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
public class ApplicationTimeHistogram {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Application application;
    private final Range range;

    private List<TimeHistogram> histogramList;

    public ApplicationTimeHistogram(Application application, Range range) {
        this(application, range, Collections.<TimeHistogram>emptyList());
    }

    public ApplicationTimeHistogram(Application application, Range range, List<TimeHistogram> histogramList) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        if (histogramList == null) {
            throw new NullPointerException("histogramList must not be null");
        }
        this.application = application;
        this.range = range;
        this.histogramList = histogramList;
    }

    public List<ResponseTimeViewModel> createViewModel() {
        final List<ResponseTimeViewModel> value = new ArrayList<ResponseTimeViewModel>(5);
        ServiceType serviceType = application.getServiceType();
        HistogramSchema schema = serviceType.getHistogramSchema();
        value.add(new ResponseTimeViewModel(schema.getFastSlot().getSlotName(), getColumnValue(SlotType.FAST)));
        value.add(new ResponseTimeViewModel(schema.getNormalSlot().getSlotName(), getColumnValue(SlotType.NORMAL)));
        value.add(new ResponseTimeViewModel(schema.getSlowSlot().getSlotName(), getColumnValue(SlotType.SLOW)));
        value.add(new ResponseTimeViewModel(schema.getVerySlowSlot().getSlotName(), getColumnValue(SlotType.VERY_SLOW)));
        value.add(new ResponseTimeViewModel(schema.getErrorSlot().getSlotName(), getColumnValue(SlotType.ERROR)));
        return value;

    }

    public List<ResponseTimeViewModel.TimeCount> getColumnValue(SlotType slotType) {
        List<ResponseTimeViewModel.TimeCount> result = new ArrayList<ResponseTimeViewModel.TimeCount>(histogramList.size());
        for (TimeHistogram timeHistogram : histogramList) {
            final long timeStamp = timeHistogram.getTimeStamp();

            ResponseTimeViewModel.TimeCount TimeCount = new ResponseTimeViewModel.TimeCount(timeStamp, getCount(timeHistogram, slotType));
            result.add(TimeCount);
        }
        return result;
    }


    public long getCount(TimeHistogram timeHistogram, SlotType slotType) {
        return timeHistogram.getCount(slotType);
    }


}
