package com.navercorp.pinpoint.metric.collector.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.model.json.Tags;

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

    public static class Field {
        private final String name;
        private final double value;

        public Field(String name, double value) {
            this.name = Objects.requireNonNull(name, "name");
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public double getValue() {
            return value;
        }

        @Override
        public String toString() {
            return name + "=" + value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Field field = (Field) o;

            if (Double.compare(field.value, value) != 0) return false;
            return name.equals(field.name);
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = name.hashCode();
            temp = Double.doubleToLongBits(value);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
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
