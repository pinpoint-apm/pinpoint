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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.uid.ServiceUidSupplier;

import java.util.Objects;

/**
 * An agent discovered in an export request together with the serviceUid it must be registered
 * under. {@link AgentInfoBo} itself carries no service, so the pair is kept here until
 * {@code OtlpTraceExportService} writes the application/agent index.
 */
public record OtlpAgentInfo(ServiceUidSupplier serviceUid, AgentInfoBo agentInfoBo) {

    public OtlpAgentInfo {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(agentInfoBo, "agentInfoBo");
    }
}
