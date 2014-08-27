package com.nhn.pinpoint.web.view;

import com.nhn.pinpoint.web.applicationmap.ServerInstance;
import com.nhn.pinpoint.web.applicationmap.ServerInstanceList;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author emeroad
 */
public class ServerInstanceListSerializer extends JsonSerializer<ServerInstanceList> {

    @Override
    public void serialize(ServerInstanceList serverInstanceList, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {

        jgen.writeStartObject();

        Map<String,List<ServerInstance>> map = serverInstanceList.getServerInstanceList();
        for (Map.Entry<String, List<ServerInstance>> entry : map.entrySet()) {
            jgen.writeFieldName(entry.getKey());
            jgen.writeStartObject();

            jgen.writeStringField("name", entry.getKey());
            jgen.writeStringField("status", null);

            jgen.writeFieldName("instanceList");
            writeInstanceList(jgen, entry.getValue());

            jgen.writeEndObject();
        }


        jgen.writeEndObject();

    }

    private void writeInstanceList(JsonGenerator jgen, List<ServerInstance> serverList) throws IOException {
        jgen.writeStartObject();
        for (ServerInstance serverInstance : serverList) {
            jgen.writeFieldName(serverInstance.getName());
            jgen.writeObject(serverInstance);
        }

        jgen.writeEndObject();
    }


}
