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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class PingSession {
    private final Long id;
    private final Header header;
    private final AtomicLong eventIdAllocator;
    private short serviceType = ServiceType.UNDEFINED.getCode();
    private boolean updated = false;
    private long lastPingTimeMillis;

    public PingSession(Long id, Header header) {
        this.id = Objects.requireNonNull(id, "transportMetadata");
        this.header = Objects.requireNonNull(header, "header");
        this.eventIdAllocator = new AtomicLong();
    }

    public Header getHeader() {
        return header;
    }

    public Long getId() {
        return id;
    }

    public long nextEventIdAllocator() {
        return eventIdAllocator.incrementAndGet();
    }

    public short getServiceType() {
        return serviceType;
    }

    public void setServiceType(short serviceType) {
        this.serviceType = serviceType;
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
                ", header=" + header +
                ", eventIdAllocator=" + eventIdAllocator +
                ", serviceType=" + serviceType +
                ", updated=" + updated +
                ", lastPingTimeMillis=" + lastPingTimeMillis +
                '}';
    }
}