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

package com.navercorp.pinpoint.web.view;

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
