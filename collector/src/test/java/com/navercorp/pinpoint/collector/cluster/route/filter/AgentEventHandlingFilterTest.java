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

package com.navercorp.pinpoint.collector.cluster.route.filter;

import com.navercorp.pinpoint.collector.cluster.route.ResponseEvent;
import com.navercorp.pinpoint.collector.dao.AgentEventDao;
import com.navercorp.pinpoint.collector.service.AgentEventService;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.io.header.HeaderEntity;
import com.navercorp.pinpoint.io.header.v1.HeaderV1;
import com.navercorp.pinpoint.io.request.DefaultMessage;
import com.navercorp.pinpoint.io.request.Message;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
public class AgentEventHandlingFilterTest {

    @Mock
    private PinpointServer pinpointServer;

    @Mock
    private AgentEventDao agentEventDao;

    @Mock
    private AgentEventService agentEventService;

    @Mock
    private DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory;

    private AgentEventHandlingFilter agentEventHandlingFilter;

    private static final String TEST_AGENT_ID = "TEST_AGENT";
    private static final long TEST_START_TIMESTAMP = System.currentTimeMillis();
    private static final long TEST_EVENT_TIMESTAMP = TEST_START_TIMESTAMP + 10;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        agentEventHandlingFilter = new AgentEventHandlingFilter(agentEventService, deserializerFactory);
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
        Message<TBase<?, ?>> message = new DefaultMessage<>(new HeaderV1((short)1000), HeaderEntity.EMPTY_HEADER_ENTITY, expectedThreadDumpResponse);
        when(deserializer.deserialize(expectedThreadDumpResponseBody)).thenReturn(message);
        // when
        this.agentEventHandlingFilter.handleResponseEvent(responseEvent, TEST_EVENT_TIMESTAMP);
        // then
        verify(this.agentEventService, atLeast(1)).insert(argCaptor.capture());
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
        Message<TBase<?, ?>> message = new DefaultMessage<>(new HeaderV1((short)1000), HeaderEntity.EMPTY_HEADER_ENTITY, mismatchingResponse);
        when(deserializer.deserialize(mismatchingResponseBody)).thenReturn(message);
        // when
        this.agentEventHandlingFilter.handleResponseEvent(responseEvent, TEST_EVENT_TIMESTAMP);
        // then
        verify(this.agentEventDao, never()).insert(argCaptor.capture());
    }
}