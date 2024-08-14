/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.grpc.server.lifecycle;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.grpc.Header;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class PingSession {
    private static final AtomicLongFieldUpdater<PingSession> UPDATER = AtomicLongFieldUpdater.newUpdater(PingSession.class, "eventIdAllocator");

    private final Long id;
    private final String applicationName;
    private final String agentId;
    private final long agentStartTime;
    private final long socketId;
    private final Map<String, Object> properties;

    private short serviceType;

    private volatile long eventIdAllocator = 0;

    private boolean updated = false;
    private long lastPingTimeMillis;

    public static PingSession of(Long id, Header header) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(header, "header");

        return new PingSession(id, header.getApplicationName(), header.getAgentId(), header.getAgentStartTime(),
                (short) header.getServiceType(), header.getSocketId(), header.getProperties());
    }

    public PingSession(Long id, String applicationName, String agentId, long agentStartTime, short serviceType, long socketId, Map<String, Object> properties) {
        this.id = Objects.requireNonNull(id, "id");

        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.agentStartTime = agentStartTime;
        this.serviceType = serviceType;
        this.socketId = socketId;
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public Long getId() {
        return id;
    }

    public long nextEventIdAllocator() {
        return UPDATER.incrementAndGet(this);
    }

    public short getServiceType() {
        synchronized (this) {
            return serviceType;
        }
    }

    public void setServiceType(short serviceType) {
        synchronized (this) {
            if (this.serviceType == ServiceType.UNDEFINED.getCode()) {
                this.serviceType = serviceType;
            }
        }
    }

    public boolean isUndefinedServiceType() {
        synchronized (this) {
            return this.serviceType == ServiceType.UNDEFINED.getCode();
        }
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getAgentId() {
        return agentId;
    }

    public long getAgentStartTime() {
        return agentStartTime;
    }

    public long getSocketId() {
        return socketId;
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    // Flag to avoid duplication.
    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public long getLastPingTimeMillis() {
        return lastPingTimeMillis;
    }

    public void setLastPingTimeMillis(long lastPingTimeMillis) {
        this.lastPingTimeMillis = lastPingTimeMillis;
    }

    @Override
    public String toString() {
        return "PingSession{" +
                "id=" + id +
                ", applicationName='" + applicationName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", agentStartTime=" + agentStartTime +
                ", socketId=" + socketId +
                ", properties=" + properties +
                ", eventIdAllocator=" + eventIdAllocator +
                ", serviceType=" + serviceType +
                ", updated=" + updated +
                ", lastPingTimeMillis=" + lastPingTimeMillis +
                '}';
    }
}