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
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import io.grpc.Context;
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
    public PingSession newPingSession(Context context) {
        Objects.requireNonNull(context, "context");

        Header header = ServerContext.getAgentInfo(context);
        TransportMetadata transport = ServerContext.getTransportMetadata(context);

        PingSession pingSession = new PingSession(transport.getTransportId(), header);
        final PingSession oldSession = pingSessionRegistry.add(pingSession.getId(), pingSession);
        if (oldSession != null) {
            logger.warn("Duplicated ping session old={}, new={}", oldSession, pingSession);
        }
        return pingSession;
    }

    @Override
    public void connect(PingSession pingSession) {
        Objects.requireNonNull(pingSession, "pingSession");

        pingSession.setLastPingTimeMillis(System.currentTimeMillis());
        lifecycleListener.connect(pingSession);
    }

    @Override
    public void ping(PingSession pingSession) {
        Objects.requireNonNull(pingSession, "pingSession");

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

        pingSessionRegistry.remove(pingSession.getId());

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