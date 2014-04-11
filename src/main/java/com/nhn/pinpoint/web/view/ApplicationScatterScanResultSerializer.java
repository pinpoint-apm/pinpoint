package com.nhn.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.scatter.ApplicationScatterScanResult;

import java.io.IOException;

/**
 * @author emeroad
 */
public class ApplicationScatterScanResultSerializer extends JsonSerializer<ApplicationScatterScanResult> {
    @Override
    public void serialize(ApplicationScatterScanResult value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        Application application = value.getApplication();
        jgen.writeStringField("applicationName", application.getName());
        jgen.writeNumberField("applicationServiceType", application.getServiceTypeCode());

        jgen.writeFieldName("scatter");
        jgen.writeObject(value.getScatterScanResult());

        jgen.writeEndObject();
    }
}
