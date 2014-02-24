package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.ServiceType;

import java.util.Comparator;

/**
 * @author emeroad
 */
public class TimeHistogram  {

    public static final AscComparator ASC_COMPARATOR = new AscComparator();

    private final long timeStamp;

    private final Histogram histogram;

    public TimeHistogram(ServiceType serviceType, long timeStamp) {
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        this.timeStamp = timeStamp;
        this.histogram = new Histogram(serviceType);
    }


    public long getTimeStamp() {
        return timeStamp;
    }

    public Histogram getHistogram() {
        return histogram;
    }

    private static class AscComparator implements Comparator<TimeHistogram> {
        @Override
        public int compare(TimeHistogram thisVal, TimeHistogram anotherVal) {
            long thisLong = thisVal.getTimeStamp();
            long anotherLong = anotherVal.getTimeStamp();
            return (thisLong<anotherLong ? -1 : (thisVal==anotherVal ? 0 : 1));
        }
    }

    @Override
    public String toString() {
        return "TimeHistogram{" +
                "timeStamp=" + timeStamp +
                ", " + histogram +
                '}';
    }
}
