/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.thrift.tcp;

import com.navercorp.pinpoint.collector.receiver.AgentLifeCycleChangeEventHandler;
import com.navercorp.pinpoint.collector.service.async.AgentEventAsyncTaskService;
import com.navercorp.pinpoint.collector.service.async.AgentLifeCycleAsyncTaskService;
import com.navercorp.pinpoint.collector.service.async.AgentProperty;
import com.navercorp.pinpoint.collector.util.ManagedAgentLifeCycle;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.server.ChannelProperties;
import com.navercorp.pinpoint.rpc.server.ChannelPropertiesFactory;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
@ExtendWith(MockitoExtension.class)
public class AgentLifeCycleChangeEventHandlerTest {

    @Mock
    private AgentLifeCycleAsyncTaskService agentLifeCycleAsyncTaskService;

    @Mock
    private AgentEventAsyncTaskService agentEventAsyncTaskService;

    @Mock
    private PinpointServer server;

    @Mock
    private ChannelPropertiesFactory channelPropertiesFactory;

    @InjectMocks
    private AgentLifeCycleChangeEventHandler lifeCycleChangeEventHandler;

    @BeforeEach
    public void setUp() throws Exception {
        doReturn("TestPinpointServer").when(this.server).toString();
        when(channelPropertiesFactory.newChannelProperties(anyMap())).thenReturn(mock(ChannelProperties.class));
    }

//    public ChannelProperties newDefaultChannelProperties() {
//        return new DefaultChannelProperties("agentId", "appName", ServiceType.STAND_ALONE.getCode(),
//                Version.VERSION, "localhost", 127.0.0.1, 1, system, socketId, supportCommandList, customProperty);
//    }

    @Test
    public void runningStatesShouldBeHandledCorrectly() {
        // given
        final Set<SocketStateCode> runningStates = ManagedAgentLifeCycle.RUNNING.getManagedStateCodes();
        runAndVerifyByStateCodes(runningStates);
    }

    @Test
    public void closedByClientStatesShouldBeHandledCorrectly() {
        // given
        final Set<SocketStateCode> closedByClientStates = ManagedAgentLifeCycle.CLOSED_BY_CLIENT.getManagedStateCodes();
        runAndVerifyByStateCodes(closedByClientStates);
    }

    @Test
    public void unexpectedCloseByClientStatesShouldBeHandledCorrectly() {
        // given
        final Set<SocketStateCode> unexpectedCloseByClientStates = ManagedAgentLifeCycle.UNEXPECTED_CLOSE_BY_CLIENT.getManagedStateCodes();
        runAndVerifyByStateCodes(unexpectedCloseByClientStates);
    }

    @Test
    public void closedByServerStatesShouldBeHandledCorrectly() {
        // given
        final Set<SocketStateCode> closedByServerStates = ManagedAgentLifeCycle.CLOSED_BY_SERVER.getManagedStateCodes();
        runAndVerifyByStateCodes(closedByServerStates);
    }

    @Test
    public void unexpectedCloseByServerStatesShouldBeHandledCorrectly() {
        // given
        final Set<SocketStateCode> unexpectedCloseByServerStates = ManagedAgentLifeCycle.UNEXPECTED_CLOSE_BY_SERVER.getManagedStateCodes();
        runAndVerifyByStateCodes(unexpectedCloseByServerStates);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void unmanagedStatesShouldNotBeHandled() {
        // given
        final Set<SocketStateCode> unmanagedStates = new HashSet<>();
        for (SocketStateCode socketStateCode : SocketStateCode.values()) {
            if (ManagedAgentLifeCycle.getManagedAgentLifeCycleByStateCode(socketStateCode) == AgentLifeCycleChangeEventHandler.STATE_NOT_MANAGED) {
                unmanagedStates.add(socketStateCode);
            }
        }
        for (SocketStateCode unmanagedState : unmanagedStates) {
            // when
            this.lifeCycleChangeEventHandler.stateUpdated(this.server, unmanagedState);
            // then
            verify(this.agentLifeCycleAsyncTaskService, never()).handleLifeCycleEvent(any(), anyLong(), any(AgentLifeCycleState.class), anyLong());
            verify(this.agentEventAsyncTaskService, never()).handleEvent(any(AgentProperty.class), anyLong(), any(AgentEventType.class));
        }
    }

    private void runAndVerifyByStateCodes(Set<SocketStateCode> socketStates) {
        int testCount = 0;
        for (SocketStateCode socketState : socketStates) {
            this.lifeCycleChangeEventHandler.stateUpdated(this.server, socketState);
            testCount++;
            verify(this.agentLifeCycleAsyncTaskService, times(testCount))
                    .handleLifeCycleEvent(any(), anyLong(), any(AgentLifeCycleState.class), anyLong());
            verify(this.agentEventAsyncTaskService, times(testCount)).handleEvent(any(AgentProperty.class), anyLong(), any(AgentEventType.class));
        }
    }

}
