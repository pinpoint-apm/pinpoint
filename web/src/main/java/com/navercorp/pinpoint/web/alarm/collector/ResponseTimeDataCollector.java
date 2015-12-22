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

package com.navercorp.pinpoint.web.alarm.collector;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.navercorp.pinpoint.web.alarm.DataCollectorFactory.DataCollectorCategory;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.dao.MapResponseDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.ResponseTime;

/**
 * @author minwoo.jung
 */
public class ResponseTimeDataCollector extends DataCollector {

    private final Application application;
    private final MapResponseDao responseDao;
    private final long timeSlotEndTime;
    private final long slotInterval;
    private final AtomicBoolean init =new AtomicBoolean(false); // need to consider a race condition when checkers start simultaneously.

    private long slowCount = 0;
    private long errorCount = 0;
    private long totalCount = 0;
    private long slowRate = 0;
    private long errorRate = 0;

    public ResponseTimeDataCollector(DataCollectorCategory category, Application application, MapResponseDao responseDAO, long timeSlotEndTime, long slotInterval) {
        super(category);
        this.application = application;
        this.responseDao = responseDAO;
        this.timeSlotEndTime = timeSlotEndTime;
        this.slotInterval = slotInterval;
    }

    @Override
    public void collect() {
        if (init.get()) {
            return;
        }

        Range range = Range.createUncheckedRange(timeSlotEndTime - slotInterval, timeSlotEndTime);
        List<ResponseTime> responseTimes = responseDao.selectResponseTime(application, range);

        for (ResponseTime responseTime : responseTimes) {
            sum(responseTime.getAgentResponseHistogramList());
        }

        setSlowRate();
        setErrorRate();

        init.set(true);
    }

    private void setSlowRate() {
        slowRate = calculatePercent(slowCount);
    }

    private void setErrorRate() {
        errorRate = calculatePercent(errorCount);
    }

    private long calculatePercent(long value) {
        if (totalCount == 0 || value == 0) {
            return 0;
        } else {
            return (value * 100L) / totalCount;
        }
    }

    private void sum(Collection<TimeHistogram> timeHistograms) {
        for (TimeHistogram timeHistogram : timeHistograms) {
            slowCount += timeHistogram.getSlowCount();
            slowCount += timeHistogram.getVerySlowCount();
            errorCount += timeHistogram.getTotalErrorCount();
            totalCount += timeHistogram.getTotalCount();
        }
    }

    public long getSlowCount() {
        return slowCount;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public long getSlowRate() {
        return slowRate;
    }

    public long getErrorRate() {
        return errorRate;
    }

}
