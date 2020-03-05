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

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultPingEventHandler implements PingEventHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final PingSessionRegistry pingSessionRegistry;
    private final LifecycleListener lifecycleListener;

    public DefaultPingEventHandler(PingSessionRegistry pingSessionRegistry, LifecycleListener lifecycleListener) {
        this.pingSessionRegistry = Assert.requireNonNull(pingSessionRegistry, "pingSessionRegistry");
        this.lifecycleListener = Assert.requireNonNull(lifecycleListener, "lifecycleListener");
    }

    @Override
    public void connect() {
        final TransportMetadata transportMetadata = ServerContext.getTransportMetadata();
        if (transportMetadata == null) {
            logger.info("TransportMetadata not found");
            return;
        }

        final Long transportId = transportMetadata.getTransportId();
        final Header header = ServerContext.getAgentInfo();
        final PingSession pingSession = new PingSession(transportId, header);

        final PingSession oldSession = pingSessionRegistry.add(pingSession.getId(), pingSession);
        if (oldSession != null) {
            logger.warn("PingSession duplicated:{}", oldSession);
//                    cleanup old session
//                    oldSession.forceClose();
        }
        lifecycleListener.connect(pingSession);

    }

    @Override
    public void ping() {
//        lifecycleListener.handshake();
    }

    @Override
    public void close() {
        final TransportMetadata transportMetadata = ServerContext.getTransportMetadata();
        if (transportMetadata == null) {
            logger.info("TransportMetadata not found");
            return;
        }

        final PingSession removedSession = pingSessionRegistry.remove(transportMetadata.getTransportId());
        if (removedSession == null) {
            return;
        }
        logger.debug("remove PingSession:{}", removedSession);
        lifecycleListener.close(removedSession);
    }


}
