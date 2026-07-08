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

package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.server.io.ServerHeader;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.NumberPrecondition;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Identifies the source of a span: the service/application and the agent
 * instance (agentId + agentStartTime) that produced it.
 * <p>
 * Mutable by design: the write path fills every field at once from
 * {@link ServerHeader} ({@link #from(ServerHeader)}), while the read path
 * (span decoder) restores only agentId/applicationName/agentStartTime from
 * storage and leaves the service fields at their defaults.
 */
public class SpanOwner {

    @NonNull
    private String agentId;
    private String agentName;

    @NonNull
    private String applicationName;

    @NonNull
    private String serviceName = ServiceUid.DEFAULT_SERVICE_UID_NAME;
    private Supplier<ServiceUid> serviceUidSupplier = () -> ServiceUid.DEFAULT;

    private long agentStartTime;

    public static SpanOwner from(ServerHeader header) {
        Objects.requireNonNull(header, "header");

        final SpanOwner owner = new SpanOwner();
        owner.setServiceName(header.getServiceName());
        owner.setServiceUid(header.getServiceUid());
        owner.setApplicationName(header.getApplicationName());
        owner.setAgentId(header.getAgentId());
        owner.setAgentName(header.getAgentName());
        owner.setAgentStartTime(header.getAgentStartTime());
        return owner;
    }

    @NonNull
    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    @NonNull
    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
    }

    @NonNull
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = StringPrecondition.requireHasLength(serviceName, "serviceName");
    }

    public ServiceUid getServiceUid() {
        return serviceUidSupplier.get();
    }

    public void setServiceUid(Supplier<ServiceUid> serviceUidSupplier) {
        this.serviceUidSupplier = Objects.requireNonNull(serviceUidSupplier, "serviceUidSupplier");
    }

    public long getAgentStartTime() {
        return agentStartTime;
    }

    public void setAgentStartTime(long agentStartTime) {
        this.agentStartTime = NumberPrecondition.requirePositiveOrZero(agentStartTime, "agentStartTime");
    }

    @Override
    public String toString() {
        return "SpanOwner{" +
                "agentId='" + agentId + '\'' +
                ", agentName='" + agentName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", serviceUid='" + serviceUidSupplier.get() + '\'' +
                ", agentStartTime=" + agentStartTime +
                '}';
    }
}
