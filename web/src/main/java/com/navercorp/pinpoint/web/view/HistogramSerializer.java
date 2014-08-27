package com.nhn.pinpoint.web.view;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.web.applicationmap.histogram.Histogram;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author emeroad
 */
public class HistogramSerializer extends JsonSerializer<Histogram> {


    @Override
    public void serialize(Histogram histogram, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        final HistogramSchema schema = histogram.getHistogramSchema();

        jgen.writeFieldName(schema.getFastSlot().getSlotName());
        jgen.writeNumber(histogram.getFastCount());

        jgen.writeFieldName(schema.getNormalSlot().getSlotName());
        jgen.writeNumber(histogram.getNormalCount());

        jgen.writeFieldName(schema.getSlowSlot().getSlotName());
        jgen.writeNumber(histogram.getSlowCount());

        jgen.writeFieldName(schema.getVerySlowSlot().getSlotName());
        jgen.writeNumber(histogram.getVerySlowCount());

        jgen.writeFieldName(schema.getErrorSlot().getSlotName());
        jgen.writeNumber(histogram.getErrorCount());

        jgen.writeEndObject();
    }
}
