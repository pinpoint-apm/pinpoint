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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.common.bo.AgentInfoBo;
import com.navercorp.pinpoint.web.applicationmap.link.MatcherGroup;
import com.navercorp.pinpoint.web.applicationmap.link.ServerMatcher;
import com.navercorp.pinpoint.web.vo.ApplicationAgentList;

/**
 * @author minwoo.jung
 */
public class ApplicationAgentListSerializer extends JsonSerializer<ApplicationAgentList> {

  @Override
  public void serialize(ApplicationAgentList applicationAgentList, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      Map<String, List<AgentInfoBo>> map = applicationAgentList.getApplicationAgentList();
      
      for (Map.Entry<String, List<AgentInfoBo>> entry : map.entrySet()) {
          jgen.writeFieldName(entry.getKey());
          writeAgentList(jgen, entry.getValue(), applicationAgentList.getMatcherGroup());
      }


      jgen.writeEndObject();

  }
    
    private void writeAgentList(JsonGenerator jgen, List<AgentInfoBo> agentList, MatcherGroup matcherGroup) throws IOException {
        jgen.writeStartArray();
        for (AgentInfoBo agentInfoBo : agentList) {
            jgen.writeStartObject();
            jgen.writeStringField("hostName", agentInfoBo.getHostName());
            jgen.writeStringField("ip", agentInfoBo.getIp());
            jgen.writeStringField("ports", agentInfoBo.getPorts());
            jgen.writeStringField("agentId", agentInfoBo.getAgentId());
            jgen.writeStringField("applicationName", agentInfoBo.getApplicationName());
            jgen.writeStringField("serviceType", agentInfoBo.getServiceType().toString());
            jgen.writeNumberField("pid", agentInfoBo.getPid());
            jgen.writeStringField("version", agentInfoBo.getVersion());
            jgen.writeNumberField("startTime", agentInfoBo.getStartTime());
            jgen.writeNumberField("endTimeStamp", agentInfoBo.getEndTimeStamp());
            jgen.writeNumberField("endStatus", agentInfoBo.getEndStatus());
            jgen.writeObjectField("serverMetaData", agentInfoBo.getServerMetaData());
            
            ServerMatcher serverMatcher = matcherGroup.match(agentInfoBo.getHostName());
            jgen.writeStringField("linkName", serverMatcher.getLinkName());
            jgen.writeStringField("linkURL", serverMatcher.getLink(agentInfoBo.getHostName()));
            
            jgen.writeEndObject();
        }
        jgen.writeEndArray();
    }
}