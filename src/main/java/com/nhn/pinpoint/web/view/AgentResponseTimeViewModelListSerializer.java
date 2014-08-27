package com.nhn.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author emeroad
 */
public class AgentResponseTimeViewModelListSerializer extends JsonSerializer<AgentResponseTimeViewModelList> {

    @Override
    public void serialize(AgentResponseTimeViewModelList viewModelList, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeFieldName(viewModelList.getFieldName());
        jgen.writeStartObject();
        for (AgentResponseTimeViewModel agentResponseTimeViewModel : viewModelList.getAgentResponseTimeViewModelList()) {
            jgen.writeObject(agentResponseTimeViewModel);
        }
        jgen.writeEndObject();
    }
}
