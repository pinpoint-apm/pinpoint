package com.nhn.pinpoint.web.view;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

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
