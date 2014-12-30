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

package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.common.util.MathUtils;
import com.navercorp.pinpoint.web.vo.Range;

/**
 * @author hyungil.jeong
 */
public class TimeWindowSlotCentricSampler implements TimeWindowSampler {

    private static final long ONE_SECOND = 1000L;
    static final long DEFAULT_MINIMUM_TIMESLOT = 5 * ONE_SECOND;
    static final long DEFAULT_IDEAL_NUM_TIMESLOTS = 200;
    
    private final long minTimeslot;
    private final long idealNumTimeslots;
    
    public TimeWindowSlotCentricSampler() {
        this.minTimeslot = DEFAULT_MINIMUM_TIMESLOT;
        this.idealNumTimeslots = DEFAULT_IDEAL_NUM_TIMESLOTS;
    }
    
    public TimeWindowSlotCentricSampler(long minTimeslot, long idealNumTimeslots) {
        this.minTimeslot = minTimeslot;
        this.idealNumTimeslots = idealNumTimeslots;
    }

    /**
     * <p>This implementation returns the window size that would generate the number of timeslots closest to 
     * <tt>idealNumTimeslots</tt> for a given <tt>range</tt>.
     * <p>Additionally, the window size is generated in multiples of
     * <tt>minTimeslot</tt>.
     * 
     * @param range range to calculate the time window over 
     * @return size of the ideal time window
     */
    @Override
    public long getWindowSize(Range range) {
        final long periodMs = range.getRange();
        final long idealTimeslotSize = periodMs / this.idealNumTimeslots;
        if (idealTimeslotSize < this.minTimeslot) {
            return this.minTimeslot;
        }
        if (idealTimeslotSize % this.minTimeslot == 0) {
            return idealTimeslotSize;
        } else {
            final long nearestMultipleOfMinTimeslotSize = MathUtils.roundToNearestMultipleOf(idealTimeslotSize, this.minTimeslot);
            return findOptimalWindowSize(periodMs, nearestMultipleOfMinTimeslotSize);
        }
    }

    private long findOptimalWindowSize(long periodMs, long nearestMultipleOfMinTimeslotSize) {
        final double idealTimeslotSize = (double)periodMs / this.idealNumTimeslots;
        final long timeslotSizeToCompare = nearestMultipleOfMinTimeslotSize < idealTimeslotSize ? 
                nearestMultipleOfMinTimeslotSize + this.minTimeslot : nearestMultipleOfMinTimeslotSize - this.minTimeslot;
        if (Math.abs(nearestMultipleOfMinTimeslotSize - idealTimeslotSize) / ((double)nearestMultipleOfMinTimeslotSize / this.minTimeslot) <
                Math.abs(timeslotSizeToCompare - idealTimeslotSize) / ((double)timeslotSizeToCompare / this.minTimeslot)) {
            return nearestMultipleOfMinTimeslotSize;
        } else {
            return timeslotSizeToCompare;
        }
    }

}
