/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.util.ThreadMXBeanUtils;
import com.navercorp.pinpoint.thrift.dto.command.TCommandThreadDumpResponse;
import com.navercorp.pinpoint.thrift.dto.command.TMonitorInfo;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadState;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.io.TCommandRegistry;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author HyunGil Jeong
 */
public class AgentEventMessageSerDesTest {

    private final TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
    private final TCommandRegistry commandTbaseRegistry = new TCommandRegistry(Arrays.asList(TCommandType.THREAD_DUMP_RESPONSE));

    private final SerializerFactory serializerFactory = new HeaderTBaseSerializerFactory(true,
            HeaderTBaseSerializerFactory.DEFAULT_STREAM_SIZE, true, this.protocolFactory, this.commandTbaseRegistry);
    private final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory = new HeaderTBaseDeserializerFactory(this.protocolFactory,
            this.commandTbaseRegistry);

    private final AgentEventMessageSerializer serializer = new AgentEventMessageSerializer(Arrays.asList(serializerFactory));
    private final AgentEventMessageDeserializer deserializer = new AgentEventMessageDeserializer(deserializerFactory);

    @Test
    public void Void_event_messages_should_serialized_and_deserialize_into_null() throws UnsupportedEncodingException {
        final Class<Void> messageTypeToTest = Void.class;
        final Object expectedEventMessage = null;
        verifyEventMessageSerDer(messageTypeToTest, expectedEventMessage);
    }

    @Test
    public void String_event_messages_should_serialize_and_deserialize_correctly() throws UnsupportedEncodingException {
        final Class<String> messageTypeToTest = String.class;
        final String expectedEventMessage = "TEST_EVENT_MESSAGE";
        verifyEventMessageSerDer(messageTypeToTest, expectedEventMessage);
    }

    @Test
    public void TCommandThreadDumpResponse_event_messages_should_serialize_and_deserialize_correctly() throws UnsupportedEncodingException {
        final Class<TCommandThreadDumpResponse> messageTypeToTest = TCommandThreadDumpResponse.class;
        final TCommandThreadDumpResponse expectedEventMessage = createTCommandThreadDumpResponse();
        verifyEventMessageSerDer(messageTypeToTest, expectedEventMessage);
    }

    private void verifyEventMessageSerDer(Class<?> messageTypeToTest, Object expectedEventMessage) throws UnsupportedEncodingException {
        for (AgentEventType agentEventType : AgentEventType.values()) {
            if (agentEventType.getMessageType() == messageTypeToTest) {
                // when
                byte[] serializedMessage = this.serializer.serialize(agentEventType, expectedEventMessage);
                Object deserializedMessage = this.deserializer.deserialize(agentEventType, serializedMessage);
                // then
                assertEquals(expectedEventMessage, deserializedMessage);
            }
        }
    }

    private TCommandThreadDumpResponse createTCommandThreadDumpResponse() {
        final TCommandThreadDumpResponse threadDumpResponse = new TCommandThreadDumpResponse();
        ThreadInfo[] threadInfos = ThreadMXBeanUtils.dumpAllThread();
        for (ThreadInfo threadInfo : threadInfos) {
            final TThreadDump threadDump = createTThreadDump(threadInfo);
            threadDumpResponse.addToThreadDumps(threadDump);
        }
        return threadDumpResponse;
    }

    private TThreadDump createTThreadDump(ThreadInfo info) {
        TThreadDump dump = new TThreadDump();

        dump.setThreadName(info.getThreadName());
        dump.setThreadId(info.getThreadId());
        dump.setBlockedTime(info.getBlockedTime());
        dump.setBlockedCount(info.getBlockedCount());
        dump.setWaitedTime(info.getWaitedTime());
        dump.setWaitedCount(info.getWaitedCount());

        dump.setLockName(info.getLockName());
        dump.setLockOwnerId(info.getLockOwnerId());
        dump.setLockOwnerName(info.getLockOwnerName());

        dump.setInNative(info.isInNative());
        dump.setSuspended(info.isSuspended());

        dump.setThreadState(getThreadState(info));

        StackTraceElement[] stackTraceElements = info.getStackTrace();
        for (StackTraceElement each : stackTraceElements) {
            dump.addToStackTrace(each.toString());
        }

        MonitorInfo[] monitorInfos = info.getLockedMonitors();
        for (MonitorInfo each : monitorInfos) {
            TMonitorInfo tMonitorInfo = new TMonitorInfo();

            tMonitorInfo.setStackDepth(each.getLockedStackDepth());
            tMonitorInfo.setStackFrame(each.getLockedStackFrame().toString());

            dump.addToLockedMonitors(tMonitorInfo);
        }

        LockInfo[] lockInfos = info.getLockedSynchronizers();
        for (LockInfo lockInfo : lockInfos) {
            dump.addToLockedSynchronizers(lockInfo.toString());
        }
        return dump;
    }

    private TThreadState getThreadState(ThreadInfo info) {
        String stateName = info.getThreadState().name();
        for (TThreadState state : TThreadState.values()) {
            if (state.name().equalsIgnoreCase(stateName)) {
                return state;
            }
        }
        return null;
    }

}
