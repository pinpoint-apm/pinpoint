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

package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.navercorp.pinpoint.collector.receiver.grpc.ShutdownEventListener;
import com.navercorp.pinpoint.collector.util.ManagedAgentLifeCycle;
import com.navercorp.pinpoint.grpc.server.lifecycle.PingSession;
import com.navercorp.pinpoint.grpc.server.lifecycle.LifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentLifecycleListener implements LifecycleListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KeepAliveService lifecycleService;
    private final ShutdownEventListener shutdownEventListener;

    @Autowired
    public AgentLifecycleListener(KeepAliveService lifecycleService, ShutdownEventListener shutdownEventListener) {
        this.lifecycleService = Objects.requireNonNull(lifecycleService, "lifecycleService");
        this.shutdownEventListener = Objects.requireNonNull(shutdownEventListener, "shutdownEventListener");
    }

    @Override
    public void connect(PingSession lifecycle) {
        logger.info("connect:{}", lifecycle);
        lifecycleService.updateState(lifecycle, ManagedAgentLifeCycle.RUNNING);
    }

    @Override
    public void handshake(PingSession lifecycle) {
        logger.info("handshake:{}", lifecycle);

    }

    @Override
    public void close(PingSession lifecycle) {

        logger.info("close:{}/{}", lifecycle, shutdownEventListener);
        ManagedAgentLifeCycle closedByClient = getManagedAgentLifeCycle();

        lifecycleService.updateState(lifecycle, closedByClient);
    }

    private ManagedAgentLifeCycle getManagedAgentLifeCycle() {
        if (shutdownEventListener.isShutdown()) {
            return ManagedAgentLifeCycle.CLOSED_BY_SERVER;
        }
        return ManagedAgentLifeCycle.CLOSED_BY_CLIENT;
    }
}
