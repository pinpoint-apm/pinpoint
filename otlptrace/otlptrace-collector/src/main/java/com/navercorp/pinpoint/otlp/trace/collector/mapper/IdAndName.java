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

import com.navercorp.pinpoint.common.server.uid.ServiceUidSupplier;

import java.util.Objects;

/**
 * Pinpoint identity of one OTLP {@code Resource}: the four names plus the serviceUid the
 * serviceName resolved to. {@link OtlpTraceMapperUtils#getId} produces the names with the
 * serviceUid still at {@link ServiceUidSupplier#DEFAULT}; {@link OtlpResourceIdResolver} attaches
 * the looked-up uid.
 */
public record IdAndName(String agentId, String agentName, String applicationName, String serviceName,
                        ServiceUidSupplier serviceUid) {

    public IdAndName {
        Objects.requireNonNull(serviceUid, "serviceUid");
    }

    /** Names only — the serviceUid stays DEFAULT until the resolver attaches the looked-up one. */
    public IdAndName(String agentId, String agentName, String applicationName, String serviceName) {
        this(agentId, agentName, applicationName, serviceName, ServiceUidSupplier.DEFAULT);
    }

    public IdAndName withServiceUid(ServiceUidSupplier serviceUid) {
        return new IdAndName(agentId, agentName, applicationName, serviceName, serviceUid);
    }
}
