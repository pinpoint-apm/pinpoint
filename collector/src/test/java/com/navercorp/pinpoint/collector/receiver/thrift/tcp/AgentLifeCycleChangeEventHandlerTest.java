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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * @author HyunGil Jeong
 */
@RunWith(MockitoJUnitRunner.class)
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
    private AgentLifeCycleChangeEventHandler lifeCycleChangeEventHandler = new AgentLifeCycleChangeEventHandler();

    @Before
    public void setUp() throws Exception {
        doReturn("TestPinpointServer").when(this.server).toString();
        when(channelPropertiesFactory.newChannelProperties(anyMap())).thenReturn(mock(ChannelProperties.class));
    }

//    public ChannelProperties newDefaultChannelProperties() {
//        return new DefaultChannelProperties("agentId", "appName", ServiceType.STAND_ALONE.getCode(),
//                Version.VERSION, "localhost", 127.0.0.1, 1, system, socketId, supportCommandList, customProperty);
//    }

    @Test
    public void runningStatesShouldBeHandledCorrectly() throws Exception {
        // given
        final Set<SocketStateCode> runningStates = ManagedAgentLifeCycle.RUNNING.getManagedStateCodes();
        runAndVerifyByStateCodes(runningStates);
    }

    @Test
    public void closedByClientStatesShouldBeHandledCorrectly() throws Exception {
        // given
        final Set<SocketStateCode> closedByClientStates = ManagedAgentLifeCycle.CLOSED_BY_CLIENT.getManagedStateCodes();
        runAndVerifyByStateCodes(closedByClientStates);
    }

    @Test
    public void unexpectedCloseByClientStatesShouldBeHandledCorrectly() throws Exception {
        // given
        final Set<SocketStateCode> unexpectedCloseByClientStates = ManagedAgentLifeCycle.UNEXPECTED_CLOSE_BY_CLIENT.getManagedStateCodes();
        runAndVerifyByStateCodes(unexpectedCloseByClientStates);
    }

    @Test
    public void closedByServerStatesShouldBeHandledCorrectly() throws Exception {
        // given
        final Set<SocketStateCode> closedByServerStates = ManagedAgentLifeCycle.CLOSED_BY_SERVER.getManagedStateCodes();
        runAndVerifyByStateCodes(closedByServerStates);
    }

    @Test
    public void unexpectedCloseByServerStatesShouldBeHandledCorrectly() throws Exception {
        // given
        final Set<SocketStateCode> unexpectedCloseByServerStates = ManagedAgentLifeCycle.UNEXPECTED_CLOSE_BY_SERVER.getManagedStateCodes();
        runAndVerifyByStateCodes(unexpectedCloseByServerStates);
    }

    @Test
    public void unmanagedStatesShouldNotBeHandled() throws Exception {
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

    private void runAndVerifyByStateCodes(Set<SocketStateCode> socketStates) throws Exception {
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
