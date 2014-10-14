package com.nhn.pinpoint.web.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * @author emeroad
 */
public class ResponseTimeViewModel {

    private final String columnName;
    private final List<TimeCount> columnValue;

    public ResponseTimeViewModel(String columnName, List<TimeCount> columnValue) {
        if (columnName == null) {
            throw new NullPointerException("columnName must not be null");
        }
        if (columnValue == null) {
            throw new NullPointerException("columnValue must not be null");
        }
        this.columnName = columnName;
        this.columnValue = columnValue;
    }

    @JsonProperty("key")
    public String getColumnName() {
        return columnName;
    }

    @JsonProperty("values")
    public List<TimeCount> getColumnValue() {
        return columnValue;
    }

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
