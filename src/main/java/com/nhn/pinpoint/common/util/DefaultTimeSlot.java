package com.nhn.pinpoint.common.util;

/**
 * @author emeroad
 */
//@Component
// spring 디펜던시를 걸어야 되서 그냥 안함.
public class DefaultTimeSlot implements TimeSlot {

    private static final long ONE_MIN_RESOLUTION =  60000; // 1min

    private final long resolution;

    public DefaultTimeSlot() {
        this(ONE_MIN_RESOLUTION);
    }

    public DefaultTimeSlot(long resolution) {
        this.resolution = resolution;
    }

    @Override
    public long getTimeSlot(long time) {
        // 과거 time을 기준으로 얻어오나, 모두 동일하게 과거 시간의 슬롯을 얻어오게 되므로 + RESOLUTION을 하지 않아도 된다.
        return (time / resolution) * resolution;
    }
}
