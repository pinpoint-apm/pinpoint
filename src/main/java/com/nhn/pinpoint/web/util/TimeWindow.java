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

    private final Range range;

    private final Range windowRange;

    private long offset;

    public TimeWindow(Range range) {
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        this.windowSize = getWindowSize(range.getFrom(), range.getTo());
        this.range = range;
        this.windowRange = createWindowRange();
        this.offset = windowRange.getFrom();
    }

    public long getNextWindowTime() {
        long current = offset;
        this.offset += windowSize;
        return current;
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

    public Range getWindowRange() {
        return windowRange;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public Range createWindowRange() {
        long from = refineTimestamp(range.getFrom());
        long to = refineTimestamp(range.getTo());
        return new Range(from, to);
    }

    private int getWindowSize(long from, long to) {
		long diff = to - from;
		int size;
        // 구간 설정 부분은 제고의 여지가 있음.
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
