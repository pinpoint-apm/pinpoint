/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.monitor.metric.profilermetric;

import com.navercorp.pinpoint.profiler.monitor.metric.MetricType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NetworkMetric implements ProfilerMetric {
    private final Fields<Long> fields;
    private final String name;
    private final Fields<String> tags;
    private final long timestamp;
    private final long collectInterval;

    public NetworkMetric(String name, long timestamp, long collectInterval) {
        this.fields = new Fields<>();
        this.name = Objects.requireNonNull(name, "name");
        this.tags = new Fields<>();
        this.timestamp = timestamp;
        this.collectInterval = collectInterval;
    }

    public void addField(String name, Long value) {
        this.fields.add(name, value);
    }

    public void addTag(String name, String value) {
        this.tags.add(name, value);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"fields\":").append(fields);
        sb.append(",\"name\":").append("\"" + name + "\"");
        sb.append(",\"tags\":").append(tags);
        sb.append(",\"timestamp\":").append(timestamp);
        sb.append("}");
        return sb.toString();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return name;
    }

    public List<Field<Long>> getFields() {
        return fields.getFields();
    }

    public List<Field<String>> getTags() {
        return tags.getFields();
    }

    public long getCollectInterval() {
        return collectInterval;
    }


    public class Fields<T> {
        List<Field<T>> fields;

        public Fields() {
            fields = new ArrayList<>();
        }

        public void add(String name, T value) {
            fields.add(new Field<>(name, value));
        }

        public List<Field<T>> getFields() {
            return fields;
        }
    }
}
