package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.ServiceType;

import java.util.Comparator;

/**
 * @author emeroad
 */
public class TimeHistogram extends Histogram  {

    public static final Comparator<TimeHistogram> ASC_COMPARATOR = new AscComparator();

    private final long timeStamp;

    public TimeHistogram(ServiceType serviceType, long timeStamp) {
        super(serviceType);
        this.timeStamp = timeStamp;
    }

    public TimeHistogram(HistogramSchema schema, long timeStamp) {
        super(schema);
        this.timeStamp = timeStamp;
    }

    public long getTimeStamp() {
        return timeStamp;
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
                ", " + super.toString() +
                '}';
    }
}
