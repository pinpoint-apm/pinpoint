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

import com.navercorp.pinpoint.collector.util.ManagedAgentLifeCycle;
import com.navercorp.pinpoint.grpc.server.lifecycle.Lifecycle;
import com.navercorp.pinpoint.grpc.server.lifecycle.LifecycleListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentLifecycleListener implements LifecycleListener {
    private final KeepAliveService lifecycleService;

    @Autowired
    public AgentLifecycleListener(KeepAliveService lifecycleService) {
        this.lifecycleService = Objects.requireNonNull(lifecycleService, "lifecycleService must not be null");
    }

    @Override
    public void connect(Lifecycle lifecycle) {

    }

    @Override
    public void handshake(Lifecycle lifecycle) {
        lifecycleService.updateState(lifecycle, ManagedAgentLifeCycle.RUNNING);
    }

    @Override
    public void close(Lifecycle lifecycle) {
        lifecycleService.updateState(lifecycle, ManagedAgentLifeCycle.CLOSED_BY_CLIENT);
    }
}
