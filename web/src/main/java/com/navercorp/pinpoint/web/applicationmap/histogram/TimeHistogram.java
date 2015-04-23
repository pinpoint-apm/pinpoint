/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.applicationmap.histogram;

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Comparator;

/**
 * @author emeroad
 */
public class TimeHistogram extends Histogram {

    public static final Comparator<TimeHistogram> TIME_STAMP_ASC_COMPARATOR = new TimeStampAscComparator();

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


    private static class TimeStampAscComparator implements Comparator<TimeHistogram> {
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
