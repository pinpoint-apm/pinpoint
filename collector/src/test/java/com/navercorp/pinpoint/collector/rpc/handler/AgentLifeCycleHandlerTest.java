/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.collector.rpc.handler;

import com.navercorp.pinpoint.collector.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.collector.util.ManagedAgentLifeCycle;
import com.navercorp.pinpoint.common.server.bo.AgentLifeCycleBo;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author HyunGil Jeong
 */
@RunWith(MockitoJUnitRunner.class)
public class AgentLifeCycleHandlerTest {
    // FIX guava 19.0.0 update error. MoreExecutors.sameThreadExecutor(); change final class
    @Spy
    private Executor executor = new DirectExecutor();

    @Mock
    private PinpointServer pinpointServer;

    @Mock
    private AgentLifeCycleDao agentLifeCycleDao;

    @InjectMocks
    private AgentLifeCycleHandler agentLifeCycleHandler = new AgentLifeCycleHandler();

    private static final String TEST_AGENT_ID = "TEST_AGENT";
    private static final long TEST_START_TIMESTAMP = System.currentTimeMillis();
    private static final long TEST_EVENT_TIMESTAMP = TEST_START_TIMESTAMP + 10;
    private static final int TEST_SOCKET_ID = 999;
    private static final Map<Object, Object> TEST_CHANNEL_PROPERTIES = createTestChannelProperties();

    @Before
    public void setUp() throws Exception {
        when(this.pinpointServer.getChannelProperties()).thenReturn(TEST_CHANNEL_PROPERTIES);
    }

    @Test
    public void runningStateShouldBeInserted() {
        runAndVerifyAgentLifeCycle(ManagedAgentLifeCycle.RUNNING);
    }

    @Test
    public void closedByClientStateShouldBeInserted() {
        runAndVerifyAgentLifeCycle(ManagedAgentLifeCycle.CLOSED_BY_CLIENT);
    }

    @Test
    public void unexpectedCloseByClientStateShouldBeInserted() {
        runAndVerifyAgentLifeCycle(ManagedAgentLifeCycle.UNEXPECTED_CLOSE_BY_CLIENT);
    }

    @Test
    public void closedByServerStateShouldBeInserted() {
        runAndVerifyAgentLifeCycle(ManagedAgentLifeCycle.CLOSED_BY_SERVER);
    }

    @Test
    public void unexpectedCloseByServerStateShouldBeInserted() {
        runAndVerifyAgentLifeCycle(ManagedAgentLifeCycle.UNEXPECTED_CLOSE_BY_SERVER);
    }

    @Test
    public void testValidTimestampCreation() {
        // socketId = 1, eventCounter = 1 -> timestamp = 0x100000001
        int givenSocketId = 1;
        int givenEventCounter = 1;
        long expectedTimestamp = new BigInteger("100000001", 16).longValue();
        long timestamp = this.agentLifeCycleHandler.createEventIdentifier(givenSocketId, givenEventCounter);
        assertEquals(expectedTimestamp, timestamp);
        // socketId = 0, eventCounter = 0 -> timestamp = 0x0
        givenSocketId = 0;
        givenEventCounter = 0;
        expectedTimestamp = new BigInteger("0000000000000000", 16).longValue();
        timestamp = this.agentLifeCycleHandler.createEventIdentifier(givenSocketId, givenEventCounter);
        assertEquals(expectedTimestamp, timestamp);
        // socketId = Integer.MAX_VALUE, eventCounter = 0 -> timestamp = 0x7fffffff00000000
        givenSocketId = Integer.MAX_VALUE;
        givenEventCounter = 0;
        expectedTimestamp = new BigInteger("7fffffff00000000", 16).longValue();
        timestamp = this.agentLifeCycleHandler.createEventIdentifier(givenSocketId, givenEventCounter);
        assertEquals(expectedTimestamp, timestamp);
        // socketId = Integer.MAX_VALUE, eventCounter = Integer.MAX_VALUE -> timestamp = 0x7fffffff7fffffff
        givenSocketId = Integer.MAX_VALUE;
        givenEventCounter = Integer.MAX_VALUE;
        expectedTimestamp = new BigInteger("7fffffff7fffffff", 16).longValue();
        timestamp = this.agentLifeCycleHandler.createEventIdentifier(givenSocketId, givenEventCounter);
        assertEquals(expectedTimestamp, timestamp);
    }

    @Test
    public void testTimestampOrdering() {
        // 0x7fffffff < 0x100000000
        long smallerTimestamp = this.agentLifeCycleHandler.createEventIdentifier(0, Integer.MAX_VALUE);
        long largerTimestamp = this.agentLifeCycleHandler.createEventIdentifier(1, 0);
        assertTrue(smallerTimestamp < largerTimestamp);
    }

    @Test
    public void testInvalidTimestampCreation() {
        final int negativeSocketId = new BigInteger("ffffffff", 16).intValue();
        final int eventCounter = 0;
        try {
            this.agentLifeCycleHandler.createEventIdentifier(negativeSocketId, eventCounter);
            fail();
        } catch (IllegalArgumentException expected) {
            // expected
        }
        final int socketId = 0;
        final int negativeEventCounter = new BigInteger("ffffffff", 16).intValue();
        try {
            this.agentLifeCycleHandler.createEventIdentifier(socketId, negativeEventCounter);
            fail();
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    private static Map<Object, Object> createTestChannelProperties() {
        return createChannelProperties(TEST_AGENT_ID, TEST_START_TIMESTAMP, TEST_SOCKET_ID);
    }

    private static Map<Object, Object> createChannelProperties(String agentId, long startTimestamp, int socketId) {
        Map<Object, Object> map = new HashMap<>();
        map.put(HandshakePropertyType.AGENT_ID.getName(), agentId);
        map.put(HandshakePropertyType.START_TIMESTAMP.getName(), startTimestamp);
        map.put(AgentLifeCycleHandler.SOCKET_ID_KEY, socketId);
        return map;
    }

    private void runAndVerifyAgentLifeCycle(ManagedAgentLifeCycle managedAgentLifeCycle) {
        // given
        final AgentLifeCycleState expectedLifeCycleState = managedAgentLifeCycle.getMappedState();
        final int expectedEventCounter = managedAgentLifeCycle.getEventCounter();
        final long expectedEventIdentifier = this.agentLifeCycleHandler.createEventIdentifier(TEST_SOCKET_ID, expectedEventCounter);
        ArgumentCaptor<AgentLifeCycleBo> argCaptor = ArgumentCaptor.forClass(AgentLifeCycleBo.class);

        // when
        this.agentLifeCycleHandler.handleLifeCycleEvent(this.pinpointServer, TEST_EVENT_TIMESTAMP, expectedLifeCycleState, expectedEventCounter);
        verify(this.agentLifeCycleDao, times(1)).insert(argCaptor.capture());

        // then
        AgentLifeCycleBo actualAgentLifeCycleBo = argCaptor.getValue();
        assertEquals(AgentLifeCycleBo.CURRENT_VERSION, actualAgentLifeCycleBo.getVersion());
        assertEquals(TEST_AGENT_ID, actualAgentLifeCycleBo.getAgentId());
        assertEquals(TEST_START_TIMESTAMP, actualAgentLifeCycleBo.getStartTimestamp());
        assertEquals(TEST_EVENT_TIMESTAMP, actualAgentLifeCycleBo.getEventTimestamp());
        assertEquals(expectedLifeCycleState, actualAgentLifeCycleBo.getAgentLifeCycleState());
        assertEquals(expectedEventIdentifier, actualAgentLifeCycleBo.getEventIdentifier());
    }

}
