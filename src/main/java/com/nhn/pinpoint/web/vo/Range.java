package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.common.util.DateUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 * @author netspider
 */
public final class Range {
    private final long from;
    private final long to;

    public Range(long from, long to) {
        this.from = from;
        this.to = to;
        validate();
    }

    public Range(long from, long to, boolean check) {
        this.from = from;
        this.to = to;
        if (check) {
        	validate();
        }
    }

    public static Range createUncheckedRange(long from, long to) {
        return new Range(from, to, false);
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    public long getRange() {
        return to - from;
    }

    public void validate() {
        if (this.to < this.from) {
            throw new IllegalArgumentException("invalid range:" + this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Range range = (Range) o;

        if (from != range.from) return false;
        if (to != range.to) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (from ^ (from >>> 32));
        result = 31 * result + (int) (to ^ (to >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Range{" +
                "from=" + from +
                ", to=" + to +
                ", range=" + getRange() +
                '}';
    }


    public String prettyToString() {
        return "Range{" +
                "from=" + DateUtils.longToDateStr(from) +
                ", to=" + DateUtils.longToDateStr(to) +
                ", range s=" + TimeUnit.MILLISECONDS.toSeconds(getRange()) +
                '}';
    }
}
