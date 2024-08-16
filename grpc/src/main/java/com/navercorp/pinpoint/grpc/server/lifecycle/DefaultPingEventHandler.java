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

import com.navercorp.pinpoint.grpc.Header;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class DefaultPingEventHandler implements PingEventHandler {
    private static final long PING_MIN_TIME_MILLIS = 60 * 1000; // 1min

    private static final AtomicIntegerFieldUpdater<DefaultPingEventHandler> UPDATER = AtomicIntegerFieldUpdater.newUpdater(DefaultPingEventHandler.class, "connect");
    private static final int INIT = 0;
    private static final int CONNECTED = 1;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final PingSessionRegistry pingSessionRegistry;
    private final LifecycleListener lifecycleListener;

    private volatile int connect = INIT;

    private final PingSession pingSession;


    public DefaultPingEventHandler(PingSessionRegistry pingSessionRegistry, LifecycleListener lifecycleListener,
                                   long transportId, Header header) {
        this.pingSessionRegistry = Objects.requireNonNull(pingSessionRegistry, "pingSessionRegistry");
        this.lifecycleListener = Objects.requireNonNull(lifecycleListener, "lifecycleListener");

        this.pingSession = PingSession.of(transportId, header);
        pingSession.setLastPingTimeMillis(getCurrentPingTimeMillis());
    }

    public PingSession getPingSession() {
        return pingSession;
    }

    public void ping() {
        if (UPDATER.compareAndSet(this, INIT, CONNECTED)) {
            connect0();
        } else {
            ping0();
        }
    }

    public void connect0() {
        logger.debug("connect {}", pingSession);
        pingSession.setLastPingTimeMillis(getCurrentPingTimeMillis());


        final PingSession oldSession = pingSessionRegistry.add(pingSession.getId(), pingSession);
        if (oldSession != null) {
            logger.warn("Duplicated ping session old={}, new={}", oldSession, pingSession);
        }
        lifecycleListener.connect(pingSession);
    }

    long getCurrentPingTimeMillis() {
        return System.currentTimeMillis();
    }


    public void ping0() {

        // Avoid too frequent updates.
        final long currentTimeMillis = getCurrentPingTimeMillis();
        if (PING_MIN_TIME_MILLIS < (currentTimeMillis - pingSession.getLastPingTimeMillis())) {
            return;
        }
        pingSession.setLastPingTimeMillis(currentTimeMillis);

        lifecycleListener.handshake(pingSession);
    }

    @Override
    public void close() {

        pingSessionRegistry.remove(pingSession.getId());

        if (logger.isDebugEnabled()) {
            logger.debug("Remove ping session. pingSession={}", pingSession);
        }
        lifecycleListener.close(pingSession);
    }

    @Override
    public void update() {
        if (logger.isDebugEnabled()) {
            logger.debug("Update ping session. PingSession={}", pingSession);
        }
        if (!pingSession.isUpdated()) {
            lifecycleListener.connect(pingSession);
            pingSession.setUpdated(true);
        }
    }
}