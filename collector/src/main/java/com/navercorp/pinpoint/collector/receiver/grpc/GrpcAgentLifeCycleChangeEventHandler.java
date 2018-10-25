/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.collector.util.ManagedAgentLifeCycle;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import org.springframework.beans.factory.annotation.Autowired;

public class GrpcAgentLifeCycleChangeEventHandler {

    @Autowired
    private GrpcAgentLifeCycleEventHandler agentLifeCycleEventHandler;
    @Autowired
    private GrpcAgentEventHandler agentEventHandler;

    public void open() {



    }


    public void close() {
    }

    private void handle(final SocketStateCode stateCode) {
//        ManagedAgentLifeCycle managedAgentLifeCycle = ManagedAgentLifeCycle.getManagedAgentLifeCycleByStateCode(stateCode);
//
//        long eventTimestamp = System.currentTimeMillis();
//        AgentLifeCycleState agentLifeCycleState = managedAgentLifeCycle.getMappedState();
//        this.agentLifeCycleEventHandler.handleLifeCycleEvent(pinpointServer, eventTimestamp, agentLifeCycleState, managedAgentLifeCycle.getEventCounter());
//        AgentEventType agentEventType = managedAgentLifeCycle.getMappedEvent();
//        this.agentEventHandler.handleEvent(pinpointServer, eventTimestamp, agentEventType);
    }
}
