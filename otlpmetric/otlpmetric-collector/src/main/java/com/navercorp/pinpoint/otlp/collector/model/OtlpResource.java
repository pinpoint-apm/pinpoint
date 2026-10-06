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

package com.navercorp.pinpoint.otlp.collector.model;

import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * The resource attributes shared by every metric of an OTLP ResourceMetrics, parsed once per resource,
 * plus the tenant the request is mapped to.
 */
public class OtlpResource {
    /**
     * pinpoint tenant, resolved per request by TenantProvider, not an OTLP attribute
     */
    @Nullable
    private final String tenantId;
    /**
     * otel service.name: ${pinpoint.applicationName}
     */
    @NonNull
    private final String serviceName;
    /**
     * otel service.namespace: ${pinpoint.serviceName}, optional
     */
    @Nullable
    private final String serviceNamespace;
    @NonNull
    private final String agentId;
    @NonNull
    private final String version;
    /**
     * the remaining resource attributes, added to every data point as tags
     */
    @NonNull
    private final Map<String, String> tags;

    public OtlpResource(@Nullable String tenantId, String serviceName, @Nullable String serviceNamespace, String agentId, String version, Map<String, String> tags) {
        this.tenantId = tenantId;
        this.serviceName = StringPrecondition.requireHasLength(serviceName, "serviceName");
        this.serviceNamespace = serviceNamespace;
        this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
        this.version = Objects.requireNonNull(version, "version");
        // shared by every metric of the resource, read-only by convention
        this.tags = Objects.requireNonNull(tags, "tags");
    }

    @Nullable
    public String getTenantId() {
        return tenantId;
    }

    public String getServiceName() {
        return serviceName;
    }

    @Nullable
    public String getServiceNamespace() {
        return serviceNamespace;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return "OtlpResource{" +
                "tenantId='" + tenantId + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", serviceNamespace='" + serviceNamespace + '\'' +
                ", agentId='" + agentId + '\'' +
                ", version='" + version + '\'' +
                ", tags=" + tags +
                '}';
    }
}
