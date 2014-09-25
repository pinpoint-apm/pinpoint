package com.nhn.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
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
