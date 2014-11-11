package com.nhn.pinpoint.web.applicationmap.histogram;

import com.nhn.pinpoint.web.applicationmap.rawdata.LinkCallData;
import com.nhn.pinpoint.web.util.TimeWindow;
import com.nhn.pinpoint.web.util.TimeWindowDownSampler;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;
import com.nhn.pinpoint.web.vo.ResponseTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
public class ApplicationTimeHistogramBuilder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Application application;
    private final Range range;
    private final TimeWindow window;



    public ApplicationTimeHistogramBuilder(Application application, Range range) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        this.application = application;
        this.range = range;
        this.window = new TimeWindow(range, TimeWindowDownSampler.SAMPLER);
    }

    public ApplicationTimeHistogram build(List<ResponseTime> responseHistogramList) {
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


//        Collections.sort(histogramList, TimeHistogram.TIME_STAMP_ASC_COMPARATOR);
        List<TimeHistogram> histogramList = interpolation(applicationLevelHistogram.values());
        if (logger.isTraceEnabled()) {
            for (TimeHistogram histogram : histogramList) {
                logger.trace("applicationLevel histogram:{}", histogram);
            }
        }
        ApplicationTimeHistogram applicationTimeHistogram = new ApplicationTimeHistogram(application, range, histogramList);
        return applicationTimeHistogram;
    }

    public ApplicationTimeHistogram build(Collection<LinkCallData> linkCallDataMapList) {
        Map<Long, TimeHistogram> applicationLevelHistogram = new HashMap<Long, TimeHistogram>();
        for (LinkCallData linkCallData : linkCallDataMapList) {
            for (TimeHistogram timeHistogram : linkCallData.getTimeHistogram()) {
                Long timeStamp = timeHistogram.getTimeStamp();
                TimeHistogram histogram = applicationLevelHistogram.get(timeStamp);
                if (histogram == null) {
                    histogram = new TimeHistogram(timeHistogram.getHistogramSchema(), timeStamp);
                    applicationLevelHistogram.put(timeStamp, histogram);
                }
                histogram.add(timeHistogram);
            }
        }

        List<TimeHistogram> histogramList = interpolation(applicationLevelHistogram.values());
        if (logger.isTraceEnabled()) {
            for (TimeHistogram histogram : histogramList) {
                logger.trace("applicationLevel histogram:{}", histogram);
            }
        }
        ApplicationTimeHistogram applicationTimeHistogram = new ApplicationTimeHistogram(application, range, histogramList);
        return applicationTimeHistogram;

    }

    private List<TimeHistogram> interpolation(Collection<TimeHistogram> histogramList) {
        // span에 대한 개별 조회시 window time만 가지고 보간하는것에 한계가 있을수 있음.
        //
        Map<Long, TimeHistogram> resultMap = new HashMap<Long, TimeHistogram>();
        for (Long time : window) {
            resultMap.put(time, new TimeHistogram(application.getServiceType(), time));
        }


        for (TimeHistogram timeHistogram : histogramList) {
            long time = window.refineTimestamp(timeHistogram.getTimeStamp());

            TimeHistogram windowHistogram = resultMap.get(time);
            if (windowHistogram == null) {
                windowHistogram = new TimeHistogram(application.getServiceType(), time);
                resultMap.put(time, windowHistogram);
            }
            windowHistogram.add(timeHistogram);
        }


        List<TimeHistogram> resultList = new ArrayList<TimeHistogram>(resultMap.values());
        Collections.sort(resultList, TimeHistogram.TIME_STAMP_ASC_COMPARATOR);
        return resultList;
    }

}
