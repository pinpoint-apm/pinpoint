/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.cluster.route.ResponseEvent;
import com.navercorp.pinpoint.collector.dao.AgentEventDao;
import com.navercorp.pinpoint.collector.rpc.handler.DirectExecutor;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.server.util.AgentEventMessageSerializer;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.thrift.dto.command.TCommandEcho;
import com.navercorp.pinpoint.thrift.dto.command.TCommandThreadDumpResponse;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransferResponse;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import org.apache.thrift.TBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author HyunGil Jeong
 */
@RunWith(MockitoJUnitRunner.class)
public class AgentEventServiceTest {
    // FIX guava 19.0.0 update error. MoreExecutors.sameThreadExecutor(); change final class
    @Spy
    private Executor executor = new DirectExecutor();

    @Mock
    private PinpointServer pinpointServer;

    @Mock
    private AgentEventDao agentEventDao;

    @Mock
    private AgentEventMessageSerializer agentEventMessageSerializer;

    @Mock
    private DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory;

    @InjectMocks
    private AgentEventService agentEventService = new AgentEventService();

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
        this.agentEventService.handleEvent(this.pinpointServer, TEST_EVENT_TIMESTAMP, expectedEventType);
        verify(this.agentEventDao, times(1)).insert(argCaptor.capture());
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
        this.agentEventService.handleEvent(this.pinpointServer, TEST_EVENT_TIMESTAMP, expectedEventType,
                expectedMessageBody);
        verify(this.agentEventDao, times(1)).insert(argCaptor.capture());
        // then
        AgentEventBo actualAgentEventBo = argCaptor.getValue();
        assertEquals(TEST_AGENT_ID, actualAgentEventBo.getAgentId());
        assertEquals(TEST_START_TIMESTAMP, actualAgentEventBo.getStartTimestamp());
        assertEquals(TEST_EVENT_TIMESTAMP, actualAgentEventBo.getEventTimestamp());
        assertEquals(expectedEventType, actualAgentEventBo.getEventType());
        assertEquals(expectedMessageBodyBytes, actualAgentEventBo.getEventBody());
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void handler_should_handle_serialization_of_request_events() throws Exception {
        // given
        final AgentEventType expectedEventType = AgentEventType.USER_THREAD_DUMP;
        final TCommandThreadDumpResponse expectedThreadDumpResponse = new TCommandThreadDumpResponse();
        final byte[] expectedThreadDumpResponseBody = new byte[0];

        final TCommandTransfer tCommandTransfer = new TCommandTransfer();
        tCommandTransfer.setAgentId(TEST_AGENT_ID);
        tCommandTransfer.setStartTime(TEST_START_TIMESTAMP);

        final TCommandTransferResponse tCommandTransferResponse = new TCommandTransferResponse();
        tCommandTransferResponse.setRouteResult(TRouteResult.OK);
        tCommandTransferResponse.setPayload(expectedThreadDumpResponseBody);

        final ResponseEvent responseEvent = new ResponseEvent(tCommandTransfer, null, 0, tCommandTransferResponse);

        ArgumentCaptor<AgentEventBo> argCaptor = ArgumentCaptor.forClass(AgentEventBo.class);
        HeaderTBaseDeserializer deserializer = mock(HeaderTBaseDeserializer.class);
        when(this.deserializerFactory.createDeserializer()).thenReturn(deserializer);
        when(deserializer.deserialize(expectedThreadDumpResponseBody)).thenReturn((TBase)expectedThreadDumpResponse);
        // when
        this.agentEventService.handleResponseEvent(responseEvent, TEST_EVENT_TIMESTAMP);
        // then
        verify(this.agentEventDao, atLeast(1)).insert(argCaptor.capture());
        AgentEventBo actualAgentEventBo = argCaptor.getValue();
        assertEquals(TEST_AGENT_ID, actualAgentEventBo.getAgentId());
        assertEquals(TEST_START_TIMESTAMP, actualAgentEventBo.getStartTimestamp());
        assertEquals(TEST_EVENT_TIMESTAMP, actualAgentEventBo.getEventTimestamp());
        assertEquals(expectedEventType, actualAgentEventBo.getEventType());
        assertArrayEquals(expectedThreadDumpResponseBody, actualAgentEventBo.getEventBody());
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void handler_should_ignore_request_events_with_unsupported_message_types() throws Exception {
        // given
        final TCommandEcho mismatchingResponse = new TCommandEcho();
        final byte[] mismatchingResponseBody = new byte[0];

        final TCommandTransfer tCommandTransfer = new TCommandTransfer();
        tCommandTransfer.setAgentId(TEST_AGENT_ID);
        tCommandTransfer.setStartTime(TEST_START_TIMESTAMP);

        final TCommandTransferResponse tCommandTransferResponse = new TCommandTransferResponse();
        tCommandTransferResponse.setRouteResult(TRouteResult.OK);
        tCommandTransferResponse.setPayload(mismatchingResponseBody);

        final ResponseEvent responseEvent = new ResponseEvent(tCommandTransfer, null, 0, tCommandTransferResponse);

        ArgumentCaptor<AgentEventBo> argCaptor = ArgumentCaptor.forClass(AgentEventBo.class);
        HeaderTBaseDeserializer deserializer = mock(HeaderTBaseDeserializer.class);
        when(this.deserializerFactory.createDeserializer()).thenReturn(deserializer);
        when(deserializer.deserialize(mismatchingResponseBody)).thenReturn((TBase)mismatchingResponse);
        // when
        this.agentEventService.handleResponseEvent(responseEvent, TEST_EVENT_TIMESTAMP);
        // then
        verify(this.agentEventDao, never()).insert(argCaptor.capture());
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
