/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.view;

import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroup;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstance;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * @author emeroad
 * @author minwoo.jung
 */
@Component
public class ServerGroupListSerializer extends JsonSerializer<ServerGroupList> {

    @Override
    public void serialize(ServerGroupList serverGroupList, JsonGenerator jgen, SerializerProvider provider) throws IOException {

        jgen.writeStartObject();

        for (ServerGroup serverGroup : serverGroupList.getServerGroupList()) {
            jgen.writeFieldName(serverGroup.getHostName());
            jgen.writeStartObject();

            jgen.writeStringField("name", serverGroup.getHostName());
            jgen.writeStringField("status", null);
            jgen.writeObjectField("linkList", serverGroup.getLinkList());


            List<ServerInstance> serverInstances = serverGroup.getInstanceList();
            jgen.writeFieldName("instanceList");
            writeInstanceList(jgen, serverInstances);

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