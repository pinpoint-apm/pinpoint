package com.profiler.common.util;

/**
 * 
 * @author netspider
 * 
 */
public class TimeSlot {
	private static final int RESOLUTION = 60000; // one minute

	public static long getStatisticsRowSlot(long time) {
		return (time / RESOLUTION) * RESOLUTION;
	}
}
