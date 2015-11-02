package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author jaehong.kim
 */
public class TransactionMetaDataSerializer extends JsonSerializer<TransactionMetaDataViewModel.MetaData> {
    @Override
    public void serialize(TransactionMetaDataViewModel.MetaData value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartArray();
        jgen.writeString(value.getTraceId());
        jgen.writeNumber(value.getCollectorAcceptTime());
        jgen.writeNumber(value.getStartTime());
        jgen.writeNumber(value.getElapsed());
        jgen.writeString(value.getApplication());
        jgen.writeString(value.getAgentId());
        jgen.writeString(value.getEndpoint());
        jgen.writeNumber(value.getException());
        jgen.writeString(value.getRemoteAddr());
        jgen.writeEndArray();
    }
}
