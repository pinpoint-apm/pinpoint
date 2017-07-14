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

package com.navercorp.pinpoint.web.vo;

import java.util.*;

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.web.util.TimeWindow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author netspider
 * 
 */
@Deprecated
public class LoadFactor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final Integer SLOT_VERY_SLOW = Integer.MAX_VALUE - 1;
    public static final Integer SLOT_ERROR = (int)Integer.MAX_VALUE;

//    /**
//     * <pre>
//     * key = responseTimeslot
//     * value = count
//     * </pre>
//     */
//    private final SortedMap<Integer, Long> histogramSummary = new TreeMap<Integer, Long>();;

    /**
     * <pre>
     * index = responseTimeslot index,
     * value = key=timestamp, value=value
     * </pre>
     */
    private final List<Map<Long, Long>> timeseriesValueList = new ArrayList<>();
    private final Map<Integer, Integer> timeseriesSlotIndex = new TreeMap<>();

    private final Range range;

    private long successCount = 0;
    private long failedCount = 0;

    private final TimeWindow timeWindow;

    public LoadFactor(Range range) {
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        this.range = range;
        this.timeWindow = new TimeWindow(range);
    }

    /**
     * initialize timeseries with default value.
     *
     * @return
     */
    private Map<Long, Long> makeEmptyTimeseriesValueMap() {
        Map<Long, Long> map = new TreeMap<>();
        for (Long time : timeWindow) {
            map.put(time, 0L);
        }
        return map;
    }

    /**
     * Empty slots in the view is shown as 0 if the histogram slot is set.
     * If not, value cannot be shown as the key is unknown.
     *
     * @param schema
     */
    public void setDefaultHistogramSlotList(HistogramSchema schema) {
        if (successCount > 0 || failedCount > 0) {
            throw new IllegalStateException("Can't set slot list while containing the data.");
        }

//        histogramSummary.clear();
        timeseriesSlotIndex.clear();
        timeseriesValueList.clear();

//        histogramSummary.put(SLOT_VERY_SLOW, 0L);
//        histogramSummary.put(SLOT_ERROR, 0L);

        timeseriesSlotIndex.put((int)schema.getFastSlot().getSlotTime(), timeseriesSlotIndex.size());
        timeseriesValueList.add(makeEmptyTimeseriesValueMap());

        timeseriesSlotIndex.put((int)schema.getNormalSlot().getSlotTime(), timeseriesSlotIndex.size());
        timeseriesValueList.add(makeEmptyTimeseriesValueMap());

        timeseriesSlotIndex.put((int)schema.getSlowSlot().getSlotTime(), timeseriesSlotIndex.size());
        timeseriesValueList.add(makeEmptyTimeseriesValueMap());

        timeseriesSlotIndex.put(SLOT_VERY_SLOW, timeseriesSlotIndex.size());
        timeseriesSlotIndex.put(SLOT_ERROR, timeseriesSlotIndex.size());

        timeseriesValueList.add(makeEmptyTimeseriesValueMap());
        timeseriesValueList.add(makeEmptyTimeseriesValueMap());
    }

    public void addSample(long timestamp, int responseTimeslot, long callCount, boolean isFailed) {
        if (logger.isDebugEnabled()) {
            logger.debug("Add sample. timeslot={}, responseTimeslot={}, callCount={}, failed={}", timestamp, responseTimeslot, callCount, isFailed);
        }

        timestamp = timeWindow.refineTimestamp(timestamp);

        if (isFailed) {
            failedCount += callCount;
        } else {
            successCount += callCount;
        }

        if (responseTimeslot == -1) {
            responseTimeslot = SLOT_ERROR;
        } else if (responseTimeslot == 0) {
            responseTimeslot = SLOT_VERY_SLOW;
        }

        // add summary
//        long value = histogramSummary.containsKey(responseTimeslot) ? histogramSummary.get(responseTimeslot) + callCount : callCount;
//        histogramSummary.put(responseTimeslot, value);

        /**
         * <pre>
         * timeseriesValueList :
         * list[response_slot_no + 0] = value<timestamp, call count>
         * list[response_slot_no + 1] = value<timestamp, call count>
         * list[response_slot_no + N] = value<timestamp, call count>
         * </pre>
         */
        for (int i = 0; i < timeseriesValueList.size(); i++) {
            Map<Long, Long> map = timeseriesValueList.get(i);

            // the same time should exist in different slots.
            // FIXME change responseTimeSlot's data type to short
            Integer slotNumber = timeseriesSlotIndex.get(responseTimeslot);
            if (i == slotNumber) {
                long v = map.containsKey(timestamp) ? map.get(timestamp) + callCount : callCount;
                map.put(timestamp, v);
            } else {
                if (!map.containsKey(timestamp)) {
                    map.put(timestamp, 0L);
                }
            }
        }
    }

//    public Map<Integer, Long> getHistogramSummary() {
//        return histogramSummary;
//    }

    public long getSuccessCount() {
        return successCount;
    }

    public long getFailedCount() {
        return failedCount;
    }

    public Map<Integer, Integer> getTimeseriesSlotIndex() {
        return timeseriesSlotIndex;
    }

    public List<Map<Long, Long>> getTimeseriesValue() {
        return timeseriesValueList;
    }


    public int getVerySlow() {
        return SLOT_VERY_SLOW;
    }

    public int getError() {
        return SLOT_ERROR;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LoadFactor{");
        sb.append(", timeseriesValueList=").append(timeseriesValueList);
        sb.append(", timeseriesSlotIndex=").append(timeseriesSlotIndex);
        sb.append(", range=").append(range);
        sb.append(", successCount=").append(successCount);
        sb.append(", failedCount=").append(failedCount);
        sb.append(", timeWindow=").append(timeWindow);
        sb.append('}');
        return sb.toString();
    }
}