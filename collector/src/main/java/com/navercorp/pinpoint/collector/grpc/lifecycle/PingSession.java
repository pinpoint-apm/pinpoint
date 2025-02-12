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

package com.navercorp.pinpoint.collector.grpc.lifecycle;

import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.io.request.ServerRequest;

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

    private final Header header;

    private volatile long eventIdAllocator = 0;

    private boolean updated = false;
    private long lastPingTimeMillis;

    public static PingSession of(ServerRequest<?> request) {
        Objects.requireNonNull(request, "request");

        return new PingSession(request.getTransportId(), request.getHeader());
    }

    public PingSession(Long id, Header header) {
        this.id = Objects.requireNonNull(id, "id");
        this.header = Objects.requireNonNull(header, "header");
    }

    public Long getId() {
        return id;
    }

    public long nextEventIdAllocator() {
        return UPDATER.incrementAndGet(this);
    }

    public short getServiceType() {
        return (short) header.getServiceType();
    }

    public String getApplicationName() {
        return header.getApplicationName();
    }

    public String getAgentId() {
        return header.getAgentId();
    }

    public long getAgentStartTime() {
        return header.getAgentStartTime();
    }

    public long getSocketId() {
        return header.getSocketId();
    }

    public Map<String, Object> getProperties() {
        return header.getProperties();
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
                ", header='" + header + '\'' +
                ", eventIdAllocator=" + eventIdAllocator +
                ", updated=" + updated +
                ", lastPingTimeMillis=" + lastPingTimeMillis +
                '}';
    }
}