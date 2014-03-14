package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.TimeHistogram;
import com.nhn.pinpoint.web.util.TimeWindow;
import com.nhn.pinpoint.web.util.TimeWindowOneMinuteSampler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
public class ApplicationTimeSeriesHistogramBuilder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Application application;
    private final Range range;
    private final TimeWindow window;



    public ApplicationTimeSeriesHistogramBuilder(Application application, Range range) {
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

    public ApplicationTimeSeriesHistogram build(List<ResponseTime> responseHistogramList) {
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


//        Collections.sort(histogramList, TimeHistogram.ASC_COMPARATOR);
        List<TimeHistogram> histogramList = interpolation(applicationLevelHistogram.values());
        if (logger.isDebugEnabled()) {
            for (TimeHistogram histogram : histogramList) {
                logger.debug("applicationLevel histogram:{}", histogram);
            }
        }
        ApplicationTimeSeriesHistogram applicationTimeSeriesHistogram = new ApplicationTimeSeriesHistogram(application, range, histogramList);
        return applicationTimeSeriesHistogram;
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
        Collections.sort(resultList, TimeHistogram.ASC_COMPARATOR);
        return resultList;
    }
}
