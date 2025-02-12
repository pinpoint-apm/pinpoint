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

import com.navercorp.pinpoint.io.request.ServerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class DefaultPingEventHandler implements PingEventHandler {
    private static final long PING_MIN_TIME_MILLIS = 60 * 1000; // 1min

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final PingSessionRegistry pingSessionRegistry;
    private final LifecycleListener lifecycleListener;

    public DefaultPingEventHandler(PingSessionRegistry pingSessionRegistry, LifecycleListener lifecycleListener) {
        this.pingSessionRegistry = Objects.requireNonNull(pingSessionRegistry, "pingSessionRegistry");
        this.lifecycleListener = Objects.requireNonNull(lifecycleListener, "lifecycleListener");
    }

    @Override
    public <T> void connect(ServerRequest<T> request) {
        final PingSession pingSession = PingSession.of(request);
        pingSession.setLastPingTimeMillis(request.getRequestTime());
        final PingSession oldSession = pingSessionRegistry.add(pingSession.getId(), pingSession);
        if (oldSession != null) {
            logger.warn("Duplicated ping session old={}, new={}", oldSession, pingSession);
        }
        lifecycleListener.connect(pingSession);
    }

    @Override
    public <T> void ping(ServerRequest<T> request) {
        Long transportId = request.getTransportId();
        final PingSession pingSession = pingSessionRegistry.get(transportId);
        if (pingSession == null) {
            logger.info("Skip ping event handle of ping, not found ping session. header={}", request.getHeader());
            return;
        }
        // Avoid too frequent updates.
        final long currentTimeMillis = System.currentTimeMillis();
        if (PING_MIN_TIME_MILLIS < (currentTimeMillis - pingSession.getLastPingTimeMillis())) {
            return;
        }
        pingSession.setLastPingTimeMillis(currentTimeMillis);

        lifecycleListener.handshake(pingSession);
    }

    @Override
    public <T> void close(ServerRequest<T> request) {
        Long transportId = request.getTransportId();

        final PingSession removedSession = pingSessionRegistry.remove(transportId);
        if (removedSession == null) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Remove ping session. pingSession={}", removedSession);
        }
        lifecycleListener.close(removedSession);
    }

    @Override
    public <T> void update(ServerRequest<T> request) {
        Long transportId = request.getTransportId();
        final PingSession pingSession = pingSessionRegistry.get(transportId);
        if (pingSession == null) {
            logger.info("Skip update event handle of ping, not found ping session. header={}", request.getHeader());
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Update ping session. PingSession={}", pingSession);
        }
        if (!pingSession.isUpdated()) {
            lifecycleListener.connect(pingSession);
            pingSession.setUpdated(true);
        }
    }
}