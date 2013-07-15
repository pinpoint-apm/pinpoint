package com.nhn.pinpoint.web.util;

/**
 * 
 * @author netspider
 * 
 */
public class TimeWindowUtils {

	private static final int ONE_MINUTE = 60000;
	private static final int SIX_HOURS = 8640000;
	private static final int ONE_DAY = 34560000;

	/**
	 * timestamp를 윈도우 사이즈에 맞는 timestamp로 변환.
	 * 
	 * @param from
	 * @param to
	 * @param timestamp
	 * @return
	 */
	public static long refineTimestamp(long from, long to, long timestamp) {
		long slotSize = getWindowSize(from, to);
		long time = timestamp / slotSize * slotSize;
		return time;
	}

	public static int getWindowIndex(long from, int windowSize, long timestamp) {
		return (int) (timestamp - from) / windowSize;
	}

	public static int getWindowSize(long from, long to) {
		long diff = to - from;
		int size;

		if (diff < SIX_HOURS) {
			size = ONE_MINUTE * 5;
		} else if (diff < ONE_DAY) {
			size = ONE_MINUTE * 10;
		} else if (diff < ONE_DAY * 2) {
			size = ONE_MINUTE * 15;
		} else {
			size = ONE_MINUTE * 20;
		}

		return size;
	}
}
