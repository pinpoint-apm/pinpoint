package com.nhn.pinpoint.web.util;

import com.nhn.pinpoint.web.vo.Range;

/**
 * 
 * @author netspider
 * 
 */
public class TimeWindow {

	private static final int ONE_MINUTE = 60000;
	private static final int ONE_HOUR = ONE_MINUTE * 60;
	private static final int SIX_HOURS = ONE_HOUR * 6;
	private static final int ONE_DAY = SIX_HOURS * 4;

    private final int windowSize;

    public TimeWindow(Range range) {
        this.windowSize = getWindowSize(range.getFrom(), range.getTo());
    }

    /**
	 * timestamp를 윈도우 사이즈에 맞는 timestamp로 변환.
	 * 
	 * @param timestamp
	 * @return
	 */
	public long refineTimestamp(long timestamp) {
		long time = timestamp / windowSize * windowSize;
		return time;
	}

    public int getWindowSize() {
        return windowSize;
    }

    public int getWindowSize(long from, long to) {
		long diff = to - from;
		int size;

		if (diff <= ONE_HOUR) {
			size = ONE_MINUTE;
		} else if (diff <= SIX_HOURS) {
			size = ONE_MINUTE * 5;
		} else if (diff <= ONE_DAY) {
			size = ONE_MINUTE * 10;
		} else if (diff <= ONE_DAY * 2) {
			size = ONE_MINUTE * 15;
		} else {
			size = ONE_MINUTE * 20;
		}

		return size;
	}
}
