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

import com.google.common.collect.Ordering;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowDownSampler;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallData;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author emeroad
 */
public class ApplicationTimeHistogramBuilder {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final Ordering<TimeHistogram> histogramOrdering = Ordering.from(TimeHistogram.TIME_STAMP_ASC_COMPARATOR);

    private final Application application;
    private final TimeWindow window;


    public ApplicationTimeHistogramBuilder(Application application, Range range) {
        this.application = Objects.requireNonNull(application, "application");
        this.window = new TimeWindow(range, TimeWindowDownSampler.SAMPLER);
    }

    public ApplicationTimeHistogram build(List<ResponseTime> responseHistogramList) {
        Objects.requireNonNull(responseHistogramList, "responseHistogramList");

        Map<Long, TimeHistogram> applicationLevelHistogram = new HashMap<>();

        for (ResponseTime responseTime : responseHistogramList) {
            final Long timeStamp = responseTime.getTimeStamp();
            TimeHistogram timeHistogram = applicationLevelHistogram.get(timeStamp);
            if (timeHistogram == null) {
                timeHistogram = new TimeHistogram(application.serviceType(), timeStamp);
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
        return new ApplicationTimeHistogram(application, histogramList);
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
        return new ApplicationTimeHistogram(application, histogramList);

    }

    private List<TimeHistogram> interpolation(Collection<TimeHistogram> histogramList) {
        // upon individual span query, "window time" alone may not be enough
        //
        Map<Long, TimeHistogram> resultMap = new HashMap<>();
        for (Long time : window) {
            resultMap.put(time, new TimeHistogram(application.serviceType(), time));
        }


        for (TimeHistogram timeHistogram : histogramList) {
            long time = window.refineTimestamp(timeHistogram.getTimeStamp());

            TimeHistogram windowHistogram = resultMap.computeIfAbsent(time, t -> new TimeHistogram(application.serviceType(), t));
            windowHistogram.add(timeHistogram);
        }

        return histogramOrdering.sortedCopy(resultMap.values());
    }

}
