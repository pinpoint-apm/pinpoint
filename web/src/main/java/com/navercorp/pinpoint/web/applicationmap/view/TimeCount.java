package com.navercorp.pinpoint.web.applicationmap.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;

@JsonSerialize(using = TimeCount.TimeCountSerializer.class)
public record TimeCount(long time, long count) {

    public static class TimeCountSerializer extends JsonSerializer<TimeCount> {
        @Override
        public void serialize(TimeCount count, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartArray();
            jgen.writeNumber(count.time());
            jgen.writeNumber(count.count());
            jgen.writeEndArray();
        }
    }
}
