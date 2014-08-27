package com.nhn.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author emeroad
 */
public class TimeCountSerializer extends JsonSerializer<ResponseTimeViewModel.TimeCount> {
    @Override
    public void serialize(ResponseTimeViewModel.TimeCount value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartArray();
        jgen.writeNumber(value.getTime());
        jgen.writeNumber(value.getCount());
        jgen.writeEndArray();
    }
}
