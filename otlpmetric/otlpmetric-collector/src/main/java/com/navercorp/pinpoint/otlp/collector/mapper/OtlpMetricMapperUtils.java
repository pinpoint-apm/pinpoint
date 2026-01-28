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

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.profiler.name.Base64Utils;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import io.opentelemetry.proto.common.v1.KeyValue;

import java.util.List;

public class OtlpMetricMapperUtils {
    public static AgentIdAndName getAgentId(List<KeyValue> attributesList) {
        final String agentId = attributesList.stream().filter(kv -> kv.getKey().equals("pinpoint.agentId")).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
        if (agentId == null) {
            final String agentName = attributesList.stream().filter(kv -> kv.getKey().equals("service.instance.id")).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
            if (agentName == null) {
                throw new IllegalStateException("not found agentId");
            }
            if (!IdValidateUtils.validateId(agentName, PinpointConstants.AGENT_NAME_MAX_LEN_V4)) {
                throw new IllegalStateException("invalid agentName=" + agentName);
            }
            final String encodedAgentId = Base64Utils.encode(agentName);
            if (!IdValidateUtils.validateId(encodedAgentId, PinpointConstants.AGENT_ID_MAX_LEN)) {
                throw new IllegalStateException("invalid agentId=" + encodedAgentId);
            }
            return new AgentIdAndName(encodedAgentId, agentName);
        }

        if (!IdValidateUtils.validateId(agentId, PinpointConstants.AGENT_ID_MAX_LEN)) {
            throw new IllegalStateException("invalid agentId=" + agentId);
        }

        return new AgentIdAndName(agentId, null);
    }

    public static String getApplicationName(List<KeyValue> attributesList) {
        String applicationName = attributesList.stream().filter(kv -> kv.getKey().equals("pinpoint.applicationName")).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
        if (applicationName == null) {
            applicationName = attributesList.stream().filter(kv -> kv.getKey().equals("service.name")).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
            if (applicationName == null) {
                throw new IllegalStateException("not found applicationName");
            }
        }
        if (!IdValidateUtils.validateId(applicationName, PinpointConstants.APPLICATION_NAME_MAX_LEN_V3)) {
            throw new IllegalStateException("invalid applicationName=" + applicationName);
        }

        return applicationName;
    }
}
