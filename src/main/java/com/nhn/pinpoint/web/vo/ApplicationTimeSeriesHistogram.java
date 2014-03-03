package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.SlotType;
import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.TimeHistogram;
import com.nhn.pinpoint.web.util.TimeWindow;
import com.nhn.pinpoint.web.util.TimeWindowOneMinuteSampler;
import com.nhn.pinpoint.web.view.ResponseTimeViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
public class ApplicationTimeSeriesHistogram {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Application application;
    private final Range range;
    private TimeWindow window;

    private List<TimeHistogram> histogramList = Collections.emptyList();

    public ApplicationTimeSeriesHistogram(Application application, Range range) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        this.application = application;
        this.range = range;
        this.window = new TimeWindow(range, TimeWindowOneMinuteSampler.SAMPLER);
    }

    public void build(List<ResponseTime> responseHistogramList) {
        if (responseHistogramList == null) {
            throw new NullPointerException("responseHistogramList must not be null");
        }

        Map<Long, TimeHistogram> applicationLevelHistogram = new HashMap<Long, TimeHistogram>();

        for (ResponseTime responseTime : responseHistogramList) {
            final Long timeStamp = responseTime.getTimeStamp();
            TimeHistogram timeHistogram = applicationLevelHistogram.get(timeStamp);
            if (timeHistogram == null) {
                timeHistogram = new TimeHistogram(application.getServiceType(), timeStamp);
                applicationLevelHistogram.put(timeStamp, timeHistogram);
            }
            // 개별 agent 레벨 데이터를 합친다.
            Histogram applicationResponseHistogram = responseTime.getApplicationResponseHistogram();
            timeHistogram.add(applicationResponseHistogram);
        }

        List<TimeHistogram> histogramList = new ArrayList<TimeHistogram>(applicationLevelHistogram.values());

//        Collections.sort(histogramList, TimeHistogram.ASC_COMPARATOR);
        histogramList = interpolation(histogramList);

        this.histogramList = histogramList;

        if (logger.isDebugEnabled()) {
            for (TimeHistogram histogram : this.histogramList) {
                logger.debug("applicationLevel histogram:{}", histogram);
            }
        }
    }

    private List<TimeHistogram> interpolation(List<TimeHistogram> histogramList) {
        if (histogramList.size() == 0) {
            return histogramList;
        }
        List<TimeHistogram> result = new ArrayList<TimeHistogram>();
        int i =0;
        for (Long time : window) {
            logger.info("------------------------{}", i++);
            result.add(new TimeHistogram(application.getServiceType(), time));
        }


        for (TimeHistogram timeHistogram : histogramList) {
            long time = window.refineTimestamp(timeHistogram.getTimeStamp());
            int windowIndex = window.getWindowIndex(time);

            TimeHistogram windowHistogram = result.get(windowIndex);
            windowHistogram.add(timeHistogram);
        }


        return result;
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
