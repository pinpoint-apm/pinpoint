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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanOwner;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OtlpAgentInfoMapper {

    public AgentInfoBo map(SpanBo spanBo, Map<String, AttributeValue> resourceAttributeMap) {
        final SpanOwner owner = spanBo.getSpanOwner();
        // Register the agent/application under the application-level type, not the span-level
        // serviceType — a specialized root (e.g. a kafka CONSUMER as the first-seen span) must
        // not decide the application's registered type.
        return map(owner.getAgentId(), owner.getAgentName(), owner.getApplicationName(), owner.getServiceUid(),
                spanBo.getApplicationServiceType(), owner.getAgentStartTime(), resourceAttributeMap);
    }

    /**
     * For a Resource seen without spans (the OTLP metrics receiver): the same AgentInfo the first
     * span would have registered, under the OTel application type.
     *
     * @param agentStartTime epoch millis, or 0 when the resource gives no process start time
     */
    public AgentInfoBo map(IdAndName idAndName, long agentStartTime, Map<String, AttributeValue> resourceAttributeMap) {
        return map(idAndName.agentId(), idAndName.agentName(), idAndName.applicationName(), idAndName.serviceUid().get(),
                ServiceType.OPENTELEMETRY_SERVER.getCode(), agentStartTime, resourceAttributeMap);
    }

    private AgentInfoBo map(String agentId, String agentName, String applicationName, ServiceUid serviceUid,
                            int serviceTypeCode, long agentStartTime, Map<String, AttributeValue> resourceAttributeMap) {
        final AgentInfoBo.Builder builder = new AgentInfoBo.Builder();
        builder.setAgentId(agentId);
        if (agentName != null) {
            builder.setAgentName(agentName);
        }
        builder.setApplicationName(applicationName);
        builder.setServiceTypeCode(serviceTypeCode);
        builder.setServiceUid(serviceUid);
        builder.setStartTime(agentStartTime);

        final String hostName = AttributeUtils.getAttributeStringValue(resourceAttributeMap, OtlpTraceConstants.ATTRIBUTE_KEY_HOST_NAME, null);
        if (hostName != null) {
            builder.setHostName(hostName);
        }
        final long pid = AttributeUtils.getAttributeIntValue(resourceAttributeMap, OtlpTraceConstants.ATTRIBUTE_KEY_PROCESS_PID, 0L);
        builder.setPid((int) pid);
        final String vmVersion = AttributeUtils.getAttributeStringValue(resourceAttributeMap, OtlpTraceConstants.ATTRIBUTE_KEY_PROCESS_RUNTIME_DESCRIPTION, null);
        builder.setVmVersion(vmVersion);
        final String agentVersion = AttributeUtils.getAttributeStringValue(resourceAttributeMap, OtlpTraceConstants.ATTRIBUTE_KEY_TELEMETRY_SDK_VERSION, null);
        if (agentVersion != null) {
            builder.setAgentVersion(agentVersion);
        }

        return builder.build();
    }
}
