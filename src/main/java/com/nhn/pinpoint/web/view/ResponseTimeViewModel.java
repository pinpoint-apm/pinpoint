package com.nhn.pinpoint.web.view;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;
import java.util.List;

/**
 * @author emeroad
 */
public interface ResponseTimeViewModel {
    @JsonProperty("key")
    String getColumnName();

    @JsonProperty("values")
    List<TimeCount> getColumnValue();

    @JsonSerialize(using=TimeCountSerializer.class)
    public static class TimeCount {

        private final long time;
        private final long count;

        public TimeCount(long time, long count) {
            this.time = time;
            this.count = count;
        }

        public long getTime() {
            return time;
        }

        public long getCount() {
            return count;
        }
    }

}
