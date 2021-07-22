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

import com.navercorp.pinpoint.web.alarm.DataCollectorFactory.DataCollectorCategory;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.dao.MapStatisticsCallerDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author minwoo.jung
 */
@Deprecated
public class MapStatisticsCallerDataCollector extends DataCollector {

    private final Application application;
    private final MapStatisticsCallerDao mapStatisticsCallerDao;
    private final long timeSlotEndTime;
    private final long slotInterval;
    private final Map<String, LinkCallData> calleeStatMap = new HashMap<>();
    private final AtomicBoolean init = new AtomicBoolean(false); // need to consider a trace condition when checkers start simultaneously.

    public MapStatisticsCallerDataCollector(DataCollectorCategory category, Application application, MapStatisticsCallerDao mapStatisticsCallerDao, long timeSlotEndTime, long slotInterval) {
        super(category);
        this.application = application;
        this.mapStatisticsCallerDao = mapStatisticsCallerDao;
        this.timeSlotEndTime = timeSlotEndTime;
        this.slotInterval = slotInterval;
    }

    @Override
    public void collect() {
        if (init.get()) {
            return;
        }

        LinkDataMap callerDataMap = mapStatisticsCallerDao.selectCaller(application, Range.newRange(timeSlotEndTime - slotInterval, timeSlotEndTime));

        for (LinkData linkData : callerDataMap.getLinkDataList()) {
            LinkCallDataMap linkCallDataMap = linkData.getLinkCallDataMap();

            for (LinkCallData linkCallData : linkCallDataMap.getLinkDataList()) {
                calleeStatMap.put(linkCallData.getTarget(), linkCallData);
            }
        }

        init.set(true);
    }

    public long getCount(String calleName, DataCategory dataCategory) {
        final LinkCallData linkCallData = calleeStatMap.get(calleName);
        if (linkCallData == null) {
            return 0;
        }

        long count = 0;
        switch (dataCategory) {
            case SLOW_COUNT:
                for (TimeHistogram timeHistogram : linkCallData.getTimeHistogram()) {
                    count += timeHistogram.getSlowCount();
                    count += timeHistogram.getVerySlowCount();
                }
                break;
            case ERROR_COUNT:
                for (TimeHistogram timeHistogram : linkCallData.getTimeHistogram()) {
                    count += timeHistogram.getTotalErrorCount();
                }
                break;
            case TOTAL_COUNT:
                for (TimeHistogram timeHistogram : linkCallData.getTimeHistogram()) {
                    count += timeHistogram.getTotalCount();
                }
                break;
            default:
                throw new IllegalArgumentException("Can't count for " + dataCategory.toString());
        }

        return count;


    }

    public long getCountRate(String calleName, DataCategory dataCategory) {
        final LinkCallData linkCallData = calleeStatMap.get(calleName);
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
                throw new IllegalArgumentException("Can't calculate rate for " + dataCategory.toString());
        }

        return calculatePercent(count, totalCount);

    }

    public enum DataCategory {
        SLOW_COUNT, ERROR_COUNT, TOTAL_COUNT,
        SLOW_RATE, ERROR_RATE
    }
}
