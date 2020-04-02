/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.applicationmap.histogram;

import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallData;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowDownSampler;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.ResponseTime;

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
        this.application = Objects.requireNonNull(application, "application");
        this.range = Objects.requireNonNull(range, "range");
        this.window = new TimeWindow(range, TimeWindowDownSampler.SAMPLER);
    }

    public ApplicationTimeHistogram build(List<ResponseTime> responseHistogramList) {
        if (responseHistogramList == null) {
            throw new NullPointerException("responseHistogramList");
        }

        Map<Long, TimeHistogram> applicationLevelHistogram = new HashMap<>();

        for (ResponseTime responseTime : responseHistogramList) {
            final Long timeStamp = responseTime.getTimeStamp();
            TimeHistogram timeHistogram = applicationLevelHistogram.get(timeStamp);
            if (timeHistogram == null) {
                timeHistogram = new TimeHistogram(application.getServiceType(), timeStamp);
                applicationLevelHistogram.put(timeStamp, timeHistogram);
            }
            // add each agent-level data
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
        Map<Long, TimeHistogram> applicationLevelHistogram = new HashMap<>();
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
        // upon individual span query, "window time" alone may not be enough
        //
        Map<Long, TimeHistogram> resultMap = new HashMap<>();
        for (Long time : window) {
            resultMap.put(time, new TimeHistogram(application.getServiceType(), time));
        }


        for (TimeHistogram timeHistogram : histogramList) {
            long time = window.refineTimestamp(timeHistogram.getTimeStamp());

            TimeHistogram windowHistogram = resultMap.computeIfAbsent(time, t -> new TimeHistogram(application.getServiceType(), t));
            windowHistogram.add(timeHistogram);
        }


        List<TimeHistogram> resultList = new ArrayList<>(resultMap.values());
        resultList.sort(TimeHistogram.TIME_STAMP_ASC_COMPARATOR);
        return resultList;
    }

}
