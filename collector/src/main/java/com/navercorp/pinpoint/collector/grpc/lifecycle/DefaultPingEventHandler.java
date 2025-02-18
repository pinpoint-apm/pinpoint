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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class DefaultPingEventHandler implements PingEventHandler {
    private static final long PING_MIN_TIME_MILLIS = 60 * 1000; // 1min

    private static final AtomicLong sessionIdAllocator = new AtomicLong();

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final PingSessionRegistry pingSessionRegistry;
    private final LifecycleListener lifecycleListener;

    public DefaultPingEventHandler(PingSessionRegistry pingSessionRegistry, LifecycleListener lifecycleListener) {
        this.pingSessionRegistry = Objects.requireNonNull(pingSessionRegistry, "pingSessionRegistry");
        this.lifecycleListener = Objects.requireNonNull(lifecycleListener, "lifecycleListener");
    }

    @Override
    public PingSession newPingSession(Long id, Header header) {
        Objects.requireNonNull(header, "header");
        Objects.requireNonNull(id, "transport");

        PingSession pingSession = new PingSession(id, nextSessionId(), header);
        pingSessionRegistry.add(pingSession);

        return pingSession;
    }

    private long nextSessionId() {
        return sessionIdAllocator.incrementAndGet();
    }

    @Override
    public void ping(PingSession pingSession) {
        Objects.requireNonNull(pingSession, "pingSession");
        if (pingSession.firstPing()) {
            connect(pingSession);
        } else {
            updatePing(pingSession);
        }
    }

    private void connect(PingSession pingSession) {
        pingSession.setLastPingTimeMillis(System.currentTimeMillis());
        lifecycleListener.connect(pingSession);
    }

    private void updatePing(PingSession pingSession) {
        // Avoid too frequent updates.
        final long currentTimeMillis = System.currentTimeMillis();
        if (PING_MIN_TIME_MILLIS < (currentTimeMillis - pingSession.getLastPingTimeMillis())) {
            return;
        }
        pingSession.setLastPingTimeMillis(currentTimeMillis);

        lifecycleListener.handshake(pingSession);
    }

    @Override
    public void close(PingSession pingSession) {
        Objects.requireNonNull(pingSession, "pingSession");

        pingSessionRegistry.remove(pingSession);

        if (logger.isDebugEnabled()) {
            logger.debug("Remove ping session. pingSession={}", pingSession);
        }
        lifecycleListener.close(pingSession);
    }

    @Override
    public void update(PingSession pingSession) {
        Objects.requireNonNull(pingSession, "pingSession");

        if (logger.isDebugEnabled()) {
            logger.debug("Update ping session. header={}", pingSession);
        }
        if (!pingSession.isUpdated()) {
            lifecycleListener.connect(pingSession);
            pingSession.setUpdated(true);
        }
    }

    @Override
    public void update(Long id) {
        Objects.requireNonNull(id, "id");

        final PingSession pingSession = pingSessionRegistry.get(id);
        if (pingSession == null) {
            logger.info("Skip update event handle of ping, not found ping session. transportMetadata={}", id);
            return;
        }
        update(pingSession);
    }
}