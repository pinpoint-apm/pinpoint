package com.nhn.pinpoint.web.vo;

import java.util.List;

/**
 * @author emeroad
 */
public interface ResponseTimeModel {

    String getColumnName();

    List<TimeValue> getColumnValue();

    public static class TimeValue {
        private long time;
        private long value;

        public TimeValue(long time, long value) {
            this.time = time;
            this.value = value;
        }

        public long getTime() {
            return time;
        }

        public long getValue() {
            return value;
        }
    }

}
