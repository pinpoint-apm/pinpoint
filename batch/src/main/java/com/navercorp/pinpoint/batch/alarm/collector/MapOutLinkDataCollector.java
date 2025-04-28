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

package com.navercorp.pinpoint.batch.alarm.collector;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.alarm.DataCollectorCategory;
import com.navercorp.pinpoint.web.applicationmap.dao.MapOutLinkDao;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author minwoo.jung
 */
public class MapOutLinkDataCollector extends DataCollector {

    private final Application application;
    private final MapOutLinkDao mapOutLinkDao;
    private final long timeSlotEndTime;
    private final long slotInterval;
    private final Map<String, LinkCallData> inLinkStatMap = new HashMap<>();
    private final AtomicBoolean init = new AtomicBoolean(false); // need to consider a trace condition when checkers start simultaneously.

    public MapOutLinkDataCollector(DataCollectorCategory category,
                                   Application application,
                                   MapOutLinkDao mapOutLinkDao, long timeSlotEndTime, long slotInterval) {
        super(category);
        this.application = Objects.requireNonNull(application, "application");
        this.mapOutLinkDao = Objects.requireNonNull(mapOutLinkDao, "mapOutLinkDao");
        this.timeSlotEndTime = timeSlotEndTime;
        this.slotInterval = slotInterval;
    }

    @Override
    public void collect() {
        if (init.get()) {
            return;
        }

        Range between = Range.between(timeSlotEndTime - slotInterval, timeSlotEndTime);
        TimeWindow timeWindow = new TimeWindow(between);
        LinkDataMap outLinkDataMap = mapOutLinkDao.selectOutLink(application, timeWindow, false);

        for (LinkData linkData : outLinkDataMap.getLinkDataList()) {
            LinkCallDataMap linkCallDataMap = linkData.getLinkCallDataMap();

            for (LinkCallData linkCallData : linkCallDataMap.getLinkDataList()) {
                inLinkStatMap.put(linkCallData.getTarget().getName(), linkCallData);
            }
        }

        init.set(true);
    }

    public long getCount(String inLinkName, DataCategory dataCategory) {
        final LinkCallData linkCallData = inLinkStatMap.get(inLinkName);
        if (linkCallData == null) {
            return 0;
        }

        long count = 0;
        switch (dataCategory) {
            case SLOW_COUNT -> {
                for (TimeHistogram timeHistogram : linkCallData.getTimeHistogram()) {
                    count += timeHistogram.getSlowCount();
                    count += timeHistogram.getVerySlowCount();
                }
            }
            case ERROR_COUNT -> {
                for (TimeHistogram timeHistogram : linkCallData.getTimeHistogram()) {
                    count += timeHistogram.getTotalErrorCount();
                }
            }
            case TOTAL_COUNT -> {
                for (TimeHistogram timeHistogram : linkCallData.getTimeHistogram()) {
                    count += timeHistogram.getTotalCount();
                }
            }
            default ->
                throw new IllegalArgumentException("Can't count for " + dataCategory);
        }

        return count;


    }

    public long getCountRate(String inLinkName, DataCategory dataCategory) {
        final LinkCallData linkCallData = inLinkStatMap.get(inLinkName);
        if (linkCallData == null) {
            return 0;
        }

        long count = 0;
        long totalCount = 0;
        switch (dataCategory) {
            case SLOW_RATE:
                for (TimeHistogram timeHistogram : linkCallData.getTimeHistogram()) {
                    count += timeHistogram.getSlowCount();
                    count += timeHistogram.getVerySlowCount();
                    totalCount += timeHistogram.getTotalCount();
                }
                break;
            case ERROR_RATE:
                for (TimeHistogram timeHistogram : linkCallData.getTimeHistogram()) {
                    count += timeHistogram.getTotalErrorCount();
                    totalCount += timeHistogram.getTotalCount();
                }
                break;
            default:
                throw new IllegalArgumentException("Can't calculate rate for " + dataCategory);
        }

        return calculatePercent(count, totalCount);

    }

    public enum DataCategory {
        SLOW_COUNT, ERROR_COUNT, TOTAL_COUNT,
        SLOW_RATE, ERROR_RATE
    }
}
