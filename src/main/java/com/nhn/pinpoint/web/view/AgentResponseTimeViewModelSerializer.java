package com.nhn.pinpoint.web.view;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

/**
 * @author emeroad
 * @author netspider
 */
public class AgentResponseTimeViewModelSerializer extends JsonSerializer<AgentResponseTimeViewModel> {
    @Override
    public void serialize(AgentResponseTimeViewModel value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        // jgen.writeStartObject();
        // jgen.writeFieldName(value.getAgentName());
        jgen.writeObject(value.getResponseTimeViewModel());
        // jgen.writeEndObject();
    }
}
