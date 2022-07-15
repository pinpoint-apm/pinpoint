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

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstance;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstanceList;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.web.hyperlink.HyperLink;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author emeroad
 * @author minwoo.jung
 */
@Component
public class ServerInstanceListSerializer extends JsonSerializer<ServerInstanceList> {

    @Override
    public void serialize(ServerInstanceList serverInstanceList, JsonGenerator jgen, SerializerProvider provider) throws IOException {

        jgen.writeStartObject();

        Map<String,List<ServerInstance>> map = serverInstanceList.getServerInstanceList();
        for (Map.Entry<String, List<ServerInstance>> entry : map.entrySet()) {
            jgen.writeFieldName(entry.getKey());
            jgen.writeStartObject();

            jgen.writeStringField("name", entry.getKey());
            jgen.writeStringField("status", null);

            List<ServerInstance> serverInstances = entry.getValue();

            ServerInstance serverInstance = org.springframework.util.CollectionUtils.firstElement(serverInstances);
            List<HyperLink> linkList = serverInstance.getLinkList();
            if (CollectionUtils.hasLength(linkList)) {
                jgen.writeObjectField("linkList", linkList);
            }
            
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