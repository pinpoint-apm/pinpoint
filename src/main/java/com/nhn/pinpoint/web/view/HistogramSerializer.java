package com.nhn.pinpoint.web.view;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * @author emeroad
 */
public class HistogramSerializer extends JsonSerializer<Histogram> {


    @Override
    public void serialize(Histogram histogram, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        final HistogramSchema schema = histogram.getServiceType().getHistogramSchema();

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
