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

import com.navercorp.pinpoint.collector.dao.AgentEventDao;
import com.navercorp.pinpoint.collector.service.AgentEventService;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.server.util.AgentEventMessageSerializer;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
@RunWith(MockitoJUnitRunner.class)
public class AgentEventHandlerTest {
    @Mock
    private PinpointServer pinpointServer;

    @Mock
    private AgentEventDao agentEventDao;

    @Mock
    private AgentEventService agentEventService;

    @Mock
    private AgentEventMessageSerializer agentEventMessageSerializer;

    @InjectMocks
    private AgentEventHandler agentEventHandler = new AgentEventHandler();

    private static final String TEST_AGENT_ID = "TEST_AGENT";
    private static final long TEST_START_TIMESTAMP = System.currentTimeMillis();
    private static final long TEST_EVENT_TIMESTAMP = TEST_START_TIMESTAMP + 10;

    private static final Map<Object, Object> TEST_CHANNEL_PROPERTIES = createTestChannelProperties();

    @Before
    public void setUp() {
        when(this.pinpointServer.getChannelProperties()).thenReturn(TEST_CHANNEL_PROPERTIES);
    }

    @Test
    public void handler_should_handle_events_with_empty_message_body() throws Exception {
        // given
        final AgentEventType expectedEventType = AgentEventType.AGENT_CONNECTED;
        ArgumentCaptor<AgentEventBo> argCaptor = ArgumentCaptor.forClass(AgentEventBo.class);
        // when
        this.agentEventHandler.handleEvent(this.pinpointServer, TEST_EVENT_TIMESTAMP, expectedEventType);
        verify(this.agentEventService, times(1)).insert(argCaptor.capture());
        // then
        AgentEventBo actualAgentEventBo = argCaptor.getValue();
        assertEquals(TEST_AGENT_ID, actualAgentEventBo.getAgentId());
        assertEquals(TEST_START_TIMESTAMP, actualAgentEventBo.getStartTimestamp());
        assertEquals(TEST_EVENT_TIMESTAMP, actualAgentEventBo.getEventTimestamp());
        assertEquals(expectedEventType, actualAgentEventBo.getEventType());
        assertNull(actualAgentEventBo.getEventBody());
    }

    @Test
    public void handler_should_handle_serialization_of_messages_appropriately() throws Exception {
        // given
        final AgentEventType expectedEventType = AgentEventType.OTHER;
        final String expectedMessageBody = "test event message";
        final byte[] expectedMessageBodyBytes = BytesUtils.toBytes(expectedMessageBody);
        ArgumentCaptor<AgentEventBo> argCaptor = ArgumentCaptor.forClass(AgentEventBo.class);
        when(this.agentEventMessageSerializer.serialize(expectedEventType, expectedMessageBody)).thenReturn(
                expectedMessageBodyBytes);
        // when
        this.agentEventHandler.handleEvent(this.pinpointServer, TEST_EVENT_TIMESTAMP, expectedEventType,
                expectedMessageBody);
        verify(this.agentEventService, times(1)).insert(argCaptor.capture());
        // then
        AgentEventBo actualAgentEventBo = argCaptor.getValue();
        assertEquals(TEST_AGENT_ID, actualAgentEventBo.getAgentId());
        assertEquals(TEST_START_TIMESTAMP, actualAgentEventBo.getStartTimestamp());
        assertEquals(TEST_EVENT_TIMESTAMP, actualAgentEventBo.getEventTimestamp());
        assertEquals(expectedEventType, actualAgentEventBo.getEventType());
        assertEquals(expectedMessageBodyBytes, actualAgentEventBo.getEventBody());
    }

    private static Map<Object, Object> createTestChannelProperties() {
        return createChannelProperties(TEST_AGENT_ID, TEST_START_TIMESTAMP);
    }

    private static Map<Object, Object> createChannelProperties(String agentId, long startTimestamp) {
        Map<Object, Object> map = new HashMap<>();
        map.put(HandshakePropertyType.AGENT_ID.getName(), agentId);
        map.put(HandshakePropertyType.START_TIMESTAMP.getName(), startTimestamp);
        return map;
    }
}