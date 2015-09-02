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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.common.util.AgentLifeCycleState;
import com.navercorp.pinpoint.web.applicationmap.link.MatcherGroup;
import com.navercorp.pinpoint.web.applicationmap.link.ServerMatcher;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.AgentStatus;
import com.navercorp.pinpoint.web.vo.ApplicationAgentList;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public class ApplicationAgentListSerializer extends JsonSerializer<ApplicationAgentList> {

    @Autowired(required = false)
    private MatcherGroup matcherGroup;

    @Override
    public void serialize(ApplicationAgentList applicationAgentList, JsonGenerator jgen, SerializerProvider provider) throws IOException,
            JsonProcessingException {
        jgen.writeStartObject();
        Map<String, List<AgentInfo>> map = applicationAgentList.getApplicationAgentList();

        for (Map.Entry<String, List<AgentInfo>> entry : map.entrySet()) {
            jgen.writeFieldName(entry.getKey());
            writeAgentList(jgen, entry.getValue(), getMatcherGroup());
        }

        jgen.writeEndObject();
    }

    private void writeAgentList(JsonGenerator jgen, List<AgentInfo> agentList, MatcherGroup matcherGroup) throws IOException {
        jgen.writeStartArray();
        for (AgentInfo agentInfo : agentList) {
            jgen.writeStartObject();
            jgen.writeStringField("applicationName", agentInfo.getApplicationName());
            jgen.writeStringField("agentId", agentInfo.getAgentId());
            jgen.writeNumberField("startTime", agentInfo.getStartTimestamp());
            jgen.writeStringField("hostName", agentInfo.getHostName());
            jgen.writeStringField("ip", agentInfo.getIp());
            jgen.writeStringField("ports", agentInfo.getPorts());
            jgen.writeStringField("serviceType", agentInfo.getServiceType().toString());
            jgen.writeNumberField("pid", agentInfo.getPid());
            jgen.writeStringField("vmVersion", agentInfo.getVmVersion());
            jgen.writeStringField("agentVersion", agentInfo.getAgentVersion());
            jgen.writeObjectField("serverMetaData", agentInfo.getServerMetaData());

            AgentStatus agentStatus = agentInfo.getStatus();
            if (agentStatus == null) {
                jgen.writeNumberField("endTimeStamp", 0);
                jgen.writeStringField("endStatus", AgentLifeCycleState.UNKNOWN.getDesc());
            } else {
                jgen.writeNumberField("endTimeStamp", agentStatus.getEventTimestamp());
                jgen.writeStringField("endStatus", agentStatus.getState().getDesc());
            }
            jgen.writeObjectField("status", agentStatus);
            
            jgen.writeNumberField("initialStartTime", agentInfo.getInitialStartTimestamp());

            ServerMatcher serverMatcher = matcherGroup.match(agentInfo.getHostName());
            jgen.writeStringField("linkName", serverMatcher.getLinkName());
            jgen.writeStringField("linkURL", serverMatcher.getLink(agentInfo.getHostName()));

            jgen.writeEndObject();
        }
        jgen.writeEndArray();
    }

    private MatcherGroup getMatcherGroup() {
        if (matcherGroup != null) {
            return matcherGroup;
        }

        return new MatcherGroup();
    }

}