/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.ApplicationAgentHostList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class ApplicationAgentHostListSerializer extends JsonSerializer<ApplicationAgentHostList> {

    @Autowired
    private ServiceTypeRegistryService serviceTypeRegistryService;

    @Override
    public void serialize(ApplicationAgentHostList applicationAgentHostList, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("startIndex", applicationAgentHostList.getStartApplicationIndex());
        jsonGenerator.writeNumberField("endIndex", applicationAgentHostList.getEndApplicationIndex());
        jsonGenerator.writeNumberField("totalApplications", applicationAgentHostList.getTotalApplications());

        jsonGenerator.writeArrayFieldStart("applications");
        writeApplicationList(applicationAgentHostList, jsonGenerator);
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
    }

    private void writeApplicationList(ApplicationAgentHostList applicationAgentHostList, JsonGenerator jsonGenerator) throws IOException {
        for (Map.Entry<String, List<AgentInfo>> e : applicationAgentHostList.getMap().entrySet()) {
            jsonGenerator.writeStartObject();
            writeApplication(e.getKey(), e.getValue(), jsonGenerator);
            jsonGenerator.writeEndObject();
        }

    }

    private void writeApplication(String applicationName, List<AgentInfo> agentInfoList, JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStringField("applicationName", applicationName);

        jsonGenerator.writeArrayFieldStart("agents");
        writeAgentList(agentInfoList, jsonGenerator);
        jsonGenerator.writeEndArray();
    }

    private void writeAgentList(List<AgentInfo> agentInfoList, JsonGenerator jsonGenerator) throws IOException {
        for (AgentInfo agentInfo : agentInfoList) {
            jsonGenerator.writeStartObject();
            writeAgent(agentInfo, jsonGenerator);
            jsonGenerator.writeEndObject();
        }
    }

    private void writeAgent(AgentInfo agentInfo, JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStringField("agentId", StringUtils.defaultString(agentInfo.getAgentId(), ""));

        final ServiceType serviceType = serviceTypeRegistryService.findServiceType(agentInfo.getServiceTypeCode());
        jsonGenerator.writeStringField("serviceType", StringUtils.defaultString(serviceType.getDesc(), ""));
        jsonGenerator.writeStringField("hostName", StringUtils.defaultString(agentInfo.getHostName(), ""));
        jsonGenerator.writeStringField("ip", StringUtils.defaultString(agentInfo.getIp(), ""));
    }

}