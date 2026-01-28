/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.otlp.collector.mapper;

import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import io.opentelemetry.proto.common.v1.KeyValue;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OtlpMetricAgentInfoMapper {

    public AgentInfoBo map(List<KeyValue> attributesList, long agentStartTime) {
        final AgentInfoBo.Builder builder = new AgentInfoBo.Builder();
        final AgentIdAndName agentIdAndName = OtlpMetricMapperUtils.getAgentId(attributesList);
        builder.setAgentId(agentIdAndName.agentId());
        if (agentIdAndName.agentName() != null) {
            builder.setAgentName(agentIdAndName.agentName());
        }
        builder.setApplicationName(OtlpMetricMapperUtils.getApplicationName(attributesList));
        builder.setServiceTypeCode(ServiceType.OPENTELEMETRY_SERVER.getCode());

        final String hostName = attributesList.stream().filter(kv -> kv.getKey().equals("host.name")).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
        if (hostName != null) {
            builder.setHostName(hostName);
        }
        final long pid = attributesList.stream().filter(kv -> kv.getKey().equals("process.pid")).findFirst().map(kv -> kv.getValue().getIntValue()).orElse(0L);
        builder.setPid((int) pid);
        final String vmVersion = attributesList.stream().filter(kv -> kv.getKey().equals("process.runtime.description")).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
        builder.setVmVersion(vmVersion);
        final String agentVersion = attributesList.stream().filter(kv -> kv.getKey().equals("telemetry.sdk.version")).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
        if (agentVersion != null) {
            builder.setAgentVersion(agentVersion);
        }
        builder.setStartTime(agentStartTime);

        return builder.build();
    }
}
