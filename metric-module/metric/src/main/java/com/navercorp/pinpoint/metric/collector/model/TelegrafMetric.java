package com.navercorp.pinpoint.metric.collector.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class TelegrafMetric {
    private final Map<String, Double> fields;
    private final String name;
    private final Map<String, String> tags;
    private final long timestamp;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public TelegrafMetric(@JsonProperty("fields") Map<String, Double> fields,
                          @JsonProperty("name") String name,
                          @JsonProperty("tags") Map<String, String> tags,
                          @JsonProperty("timestamp") long timestamp) {
        this.fields = fields;
        this.name = name;
        this.tags = tags;
        this.timestamp = timestamp;
    }

    public Map<String, Double> getFields() {
        return fields;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "TelegrafMetric{" +
                "fields=" + fields +
                ", name='" + name + '\'' +
                ", tags=" + tags +
                ", timestamp=" + timestamp +
                '}';
    }
}
