package com.nhn.pinpoint.common.util;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class TimeSlot {
	private static final int RESOLUTION =  60000; // 1min

	public static long getStatisticsRowSlot(long time) {
        // 과거 time을 기준으로 얻어오나, 모두 동일하게 과거 시간의 슬롯을 얻어오게 되므로 + RESOLUTION을 하지 않아도 된다.
		return (time / RESOLUTION) * RESOLUTION;
	}
}
