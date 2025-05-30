package com.navercorp.pinpoint.metric.collector.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.model.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TelegrafMetric {
    private final List<Field> fields;
    private final String name;
    private final List<Tag> tags;
    private final long timestamp;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public TelegrafMetric(@JsonProperty("fields") Fields fields,
                          @JsonProperty("name") String name,
                          @JsonProperty("tags") Tags tags,
                          @JsonProperty("timestamp") long timestamp) {
        this.fields = fields.fields;
        this.name = name;
        this.tags = tags.getTags();
        this.timestamp = timestamp;
    }

    public static class Fields {

        private final List<Field> fields = new ArrayList<>();

        @JsonAnySetter
        public void add(String name, Double value) {
            fields.add(new Field(name, value));
        }

    }

    public record Field(String name, double value) {
            public Field(String name, double value) {
                this.name = Objects.requireNonNull(name, "name");
                this.value = value;
            }

            @Override
            public String toString() {
                return name + "=" + value;
            }
        }


    public List<Field> getFields() {
        return fields;
    }

    public String getName() {
        return name;
    }

    public List<Tag> getTags() {
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
