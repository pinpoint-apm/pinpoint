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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class PingSession {
    private static final AtomicLongFieldUpdater<PingSession> EVENT_UPDATER = AtomicLongFieldUpdater.newUpdater(PingSession.class, "eventIdAllocator");
    private static final AtomicIntegerFieldUpdater<PingSession> PING_UPDATER = AtomicIntegerFieldUpdater.newUpdater(PingSession.class, "pingEvent");

    private volatile long eventIdAllocator = 0;
    private volatile int pingEvent = 0;

    private final Long transportId;
    private final long sessionId;

    private final Header header;

    private boolean updated = false;
    private long lastPingTimeMillis;

    public PingSession(Long transportId, long sessionId, Header header) {
        this.transportId = Objects.requireNonNull(transportId, "transportId");
        this.sessionId = sessionId;
        this.header = Objects.requireNonNull(header, "header");
    }

    public boolean firstPing() {
        return PING_UPDATER.getAndIncrement(this) == 0;
    }

    public Long getTransportId() {
        return transportId;
    }

    public long getSessionId() {
        return sessionId;
    }

    public long nextEventIdAllocator() {
        return EVENT_UPDATER.incrementAndGet(this);
    }

    public Header getHeader() {
        return header;
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
                "transportId=" + transportId +
                ", sessionId=" + sessionId +
                ", header='" + header + '\'' +
                ", eventIdAllocator=" + eventIdAllocator +
                ", updated=" + updated +
                ", lastPingTimeMillis=" + lastPingTimeMillis +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        PingSession that = (PingSession) o;
        return sessionId == that.sessionId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(sessionId);
    }
}