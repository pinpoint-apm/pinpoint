package com.nhn.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nhn.pinpoint.web.vo.scatter.Dot;

import java.io.IOException;

/**
 * @author emeroad
 */
public class DotSerializer extends JsonSerializer<Dot> {
    @Override
    public void serialize(Dot dot, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartArray();
        jgen.writeNumber(dot.getAcceptedTime());
        jgen.writeNumber(dot.getElapsedTime());
        jgen.writeString(dot.getTransactionId());
        jgen.writeNumber(dot.getSimpleExceptionCode());
        jgen.writeEndArray();
    }
}
