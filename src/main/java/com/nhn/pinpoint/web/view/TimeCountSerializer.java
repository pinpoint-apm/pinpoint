package com.nhn.pinpoint.web.view;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

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
