/*
 * Copyright 2016 NAVER Corp.
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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.web.applicationmap.link.LinkInfo;
import com.navercorp.pinpoint.web.applicationmap.link.MatcherGroup;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.AgentStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class AgentInfoSerializer extends JsonSerializer<AgentInfo> {

    @Autowired(required = false)
    private List<MatcherGroup> matcherGroupList;


    @Override
    public void serialize(AgentInfo agentInfo, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();

        jgen.writeStringField("applicationName", agentInfo.getApplicationName());
        jgen.writeStringField("agentId", agentInfo.getAgentId());
        jgen.writeStringField("agentName", agentInfo.getAgentName());
        jgen.writeNumberField("startTimestamp", agentInfo.getStartTimestamp());
        jgen.writeStringField("hostName", agentInfo.getHostName());
        jgen.writeStringField("ip", agentInfo.getIp());
        jgen.writeStringField("ports", agentInfo.getPorts());

        jgen.writeStringField("serviceType", agentInfo.getServiceType().getDesc());
        jgen.writeNumberField("pid", agentInfo.getPid());
        jgen.writeStringField("vmVersion", agentInfo.getVmVersion());
        jgen.writeStringField("agentVersion", agentInfo.getAgentVersion());
        jgen.writeObjectField("serverMetaData", agentInfo.getServerMetaData());
        jgen.writeObjectField("jvmInfo", agentInfo.getJvmInfo());

        AgentStatus status = agentInfo.getStatus();
        if (status != null) {
            jgen.writeObjectField("status", status);
        }

        jgen.writeNumberField("initialStartTimestamp", agentInfo.getInitialStartTimestamp());

        if (matcherGroupList != null) {
            jgen.writeFieldName("linkList");
            jgen.writeStartArray();

            for (MatcherGroup matcherGroup : matcherGroupList) {
                if (matcherGroup.ismatchingType(agentInfo)) {
                    LinkInfo linkInfo = matcherGroup.makeLinkInfo(agentInfo);
                    jgen.writeObject(linkInfo);
                }
            }

            jgen.writeEndArray();
        }

        jgen.writeEndObject();
    }

}
