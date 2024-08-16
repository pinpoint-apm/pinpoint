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

import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class DefaultPingEventHandlerFactory implements PingEventHandlerFactory {
    private final PingSessionRegistry pingSessionRegistry;
    private final LifecycleListener lifecycleListener;

    public DefaultPingEventHandlerFactory(PingSessionRegistry pingSessionRegistry, LifecycleListener lifecycleListener) {
        this.pingSessionRegistry = Objects.requireNonNull(pingSessionRegistry, "pingSessionRegistry");
        this.lifecycleListener = Objects.requireNonNull(lifecycleListener, "lifecycleListener");
    }

    @Override
    public PingEventHandler createPingEventHandler() {
        final TransportMetadata transportMetadata = ServerContext.getTransportMetadata();
        final com.navercorp.pinpoint.grpc.Header header = ServerContext.getAgentInfo();
        return new DefaultPingEventHandler(pingSessionRegistry, lifecycleListener, transportMetadata.getTransportId(), header);
    }
}
