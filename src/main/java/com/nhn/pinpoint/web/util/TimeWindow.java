package com.nhn.pinpoint.web.util;

import com.nhn.pinpoint.web.vo.Range;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 
 * @author netspider
 * 
 */
public class TimeWindow implements Iterable<Long> {

	private static final int ONE_MINUTE = 60000;
	private static final int ONE_HOUR = ONE_MINUTE * 60;
	private static final int SIX_HOURS = ONE_HOUR * 6;
	private static final int ONE_DAY = SIX_HOURS * 4;

    private final int windowSlotSize;

    private final Range range;

    private final Range windowRange;



    public TimeWindow(Range range) {
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        this.windowSlotSize = getWindowSlotSize(range.getFrom(), range.getTo());
        this.range = range;
        this.windowRange = createWindowRange();

    }

    public Iterator<Long> iterator() {
        return new Itr();
    }



    /**
	 * timestamp를 윈도우 사이즈에 맞는 timestamp로 변환.
	 * 
	 * @param timestamp
	 * @return
	 */
	public long refineTimestamp(long timestamp) {
		long time = timestamp / windowSlotSize * windowSlotSize;
		return time;
	}

    public Range getWindowRange() {
        return windowRange;
    }

    public int getWindowSlotSize() {
        return windowSlotSize;
    }

    private Range createWindowRange() {
        long from = refineTimestamp(range.getFrom());
        long to = refineTimestamp(range.getTo());
        return new Range(from, to);
    }

    private int getWindowSlotSize(long from, long to) {
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

    private class Itr implements Iterator<Long> {

        private long cursor;

        public Itr() {
            this.cursor = windowRange.getFrom();
        }

        @Override
        public boolean hasNext() {
            if (cursor > windowRange.getTo()) {
                return false;
            }
            return true;
        }

        @Override
        public Long next() {
            long current = cursor;
            if (hasNext()) {
                cursor += windowSlotSize;
                return current;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
