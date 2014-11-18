package com.nhn.pinpoint.web.util;

import com.nhn.pinpoint.common.util.MathUtils;
import com.nhn.pinpoint.web.vo.Range;

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
     * <p>This implementation returns the window size that generates a 
     * <tt>MINIMUM_TIMESLOT. 
     * Additionally, the window size is calculated in multiples of a 
     * <tt>IDEAL_NUM_TIMESLOTS</tt>.
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
