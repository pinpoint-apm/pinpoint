package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.TimeHistogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
public class ApplicationTimeSeriesHistogram  {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Application application;

    private List<TimeHistogram> histogramList = Collections.emptyList();

    public ApplicationTimeSeriesHistogram(Application application) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        this.application = application;
    }

    public void build(List<ResponseTime> responseHistogramList) {
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
            timeHistogram.getHistogram().add(applicationResponseHistogram);
        }

        List<TimeHistogram> histogramList = new ArrayList<TimeHistogram>(applicationLevelHistogram.values());
        Collections.sort(histogramList, TimeHistogram.ASC_COMPARATOR);

        this.histogramList = histogramList;

        if (logger.isDebugEnabled()) {
            for (TimeHistogram histogram : this.histogramList) {
                logger.debug("applicationLevel histogram:{}", histogram);
            }
        }
    }

    public List<ResponseTimeModel> viewModel() {
        return null;

    }

}
