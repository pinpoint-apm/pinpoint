package com.nhn.pinpoint.web.view;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.util.List;

/**
 * @author emeroad
 */
public class AgentResponseTimeViewModelSerializer extends JsonSerializer<AgentResponseTimeViewModel> {
    @Override
    public void serialize(AgentResponseTimeViewModel value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeFieldName(value.getAgentName());
        jgen.writeObject(value.getResponseTimeViewModel());
    }
}
