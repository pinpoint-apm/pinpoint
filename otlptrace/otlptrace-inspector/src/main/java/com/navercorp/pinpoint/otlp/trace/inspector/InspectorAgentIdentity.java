/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.trace.inspector;

import com.navercorp.pinpoint.common.server.util.StringPrecondition;

/**
 * The Pinpoint identity of the OTel Resource whose metrics are being mapped: what the native agent
 * sends in its gRPC header. The caller (the OTLP trace collector) resolves it from the resource
 * attributes with its own rules; this module only needs the resolved values.
 *
 * @param agentStartTime epoch millis of the process start, or 0 when unknown (the AgentStatBo
 *                       builder accepts zero)
 */
public record InspectorAgentIdentity(String serviceName, String applicationName, String agentId, long agentStartTime) {

    public InspectorAgentIdentity {
        StringPrecondition.requireHasLength(serviceName, "serviceName");
        StringPrecondition.requireHasLength(applicationName, "applicationName");
        StringPrecondition.requireHasLength(agentId, "agentId");
        if (agentStartTime < 0) {
            throw new IllegalArgumentException("agentStartTime must be >= 0");
        }
    }

    /**
     * Prefix of the per-series keys of the delta state cache; one agent never collides with another,
     * and a restarted agent (new start time) starts its running totals afresh.
     */
    String seriesKeyPrefix() {
        return serviceName + '/' + applicationName + '/' + agentId + '@' + agentStartTime + '|';
    }
}
