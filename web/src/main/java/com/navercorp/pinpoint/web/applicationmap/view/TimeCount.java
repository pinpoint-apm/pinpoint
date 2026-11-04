package com.navercorp.pinpoint.web.applicationmap.view;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = TimeCount.TimeCountSerializer.class)
public record TimeCount(long time, long count) {

    public static class TimeCountSerializer extends ValueSerializer<TimeCount> {
        @Override
        public void serialize(TimeCount count, JsonGenerator jgen, SerializationContext provider) {
            jgen.writeStartArray();
            jgen.writeNumber(count.time());
            jgen.writeNumber(count.count());
            jgen.writeEndArray();
        }
    }
}
