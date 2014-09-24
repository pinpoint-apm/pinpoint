package com.nhn.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nhn.pinpoint.web.vo.Application;

import java.io.IOException;
import java.util.List;

/**
 * @author emeroad
 */
public class ApplicationGroupSerializer extends JsonSerializer<ApplicationGroup> {

    @Override
    public void serialize(ApplicationGroup applicationGroup, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartArray();

        List<Application> applicationList = applicationGroup.getApplicationList();
        for (Application application : applicationList) {
            writeApplication(jgen, application);
        }
        jgen.writeEndArray();
    }

    private void writeApplication(JsonGenerator jgen, Application application) throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField("applicationName", application.getName());
        jgen.writeStringField("serviceType", application.getServiceType().getDesc());
        jgen.writeNumberField("code", application.getServiceType().getCode());
        jgen.writeEndObject();
    }
}
