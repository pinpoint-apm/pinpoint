/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.server.bo.event.DeadlockBo;
import com.navercorp.pinpoint.common.server.bo.event.MonitorInfoBo;
import com.navercorp.pinpoint.common.server.bo.event.ThreadDumpBo;
import com.navercorp.pinpoint.common.server.bo.event.ThreadState;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.thrift.dto.TDeadlock;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jaehong.kim
 * AgentEventBo.version is 1
 */
public class AgentEventMessageDeserializerV1 {

    public Object deserialize(AgentEventType agentEventType, byte[] eventBody) throws UnsupportedEncodingException {
        if (agentEventType == null) {
            throw new NullPointerException("agentEventType");
        }
        Class<?> eventMessageType = agentEventType.getMessageType();
        if (eventMessageType == Void.class) {
            return null;
        }

        if (TDeadlock.class.isAssignableFrom(eventMessageType)) {
            return deserializeDeadlockBo(eventBody);
        } else if (String.class.isAssignableFrom(eventMessageType)) {
            return BytesUtils.toString(eventBody);
        }
        throw new UnsupportedEncodingException("Unsupported event message type [" + eventMessageType.getName() + "]");
    }

    private DeadlockBo deserializeDeadlockBo(final byte[] eventBody) {
        final Buffer buffer = new FixedBuffer(eventBody);

        final int deadlockedThreadCount = buffer.readInt();
        final int threadDumpBoListSize = buffer.readVInt();
        final List<ThreadDumpBo> threadDumpBoList = new ArrayList<>(threadDumpBoListSize);
        if (threadDumpBoListSize > 0) {
            threadDumpBoList.add(readThreadDumpBo(buffer));
        }

        final DeadlockBo deadlockBo = new DeadlockBo();
        deadlockBo.setDeadlockedThreadCount(deadlockedThreadCount);
        deadlockBo.setThreadDumpBoList(threadDumpBoList);
        return deadlockBo;
    }

    private ThreadDumpBo readThreadDumpBo(final Buffer buffer) {
        final String threadName = buffer.readPrefixedString();
        final long threadId = buffer.readLong();
        final long blockedTime = buffer.readLong();
        final long blockedCount = buffer.readLong();
        final long waitedTime = buffer.readLong();
        final long waitedCount = buffer.readLong();
        final String lockName = buffer.readPrefixedString();
        final long lockOwnerId = buffer.readLong();
        final String lockOwnerName = buffer.readPrefixedString();
        final boolean inNative = buffer.readBoolean();
        final boolean suspended = buffer.readBoolean();
        final int threadStateValue = buffer.readInt();

        final int stackTraceListSize = buffer.readVInt();
        final List<String> stackTraceList = new ArrayList<>(stackTraceListSize);
        if (stackTraceListSize > 0) {
            for (int i = 0; i < stackTraceListSize; i++) {
                final String string = buffer.readPrefixedString();
                stackTraceList.add(string);
            }
        }

        final int lockedMonitorListSize = buffer.readVInt();
        final List<MonitorInfoBo> monitorInfoBoList = new ArrayList<>(lockedMonitorListSize);
        if (lockedMonitorListSize > 0) {
            for (int i = 0; i < lockedMonitorListSize; i++) {
                final MonitorInfoBo monitorInfoBo = readMonitorInfoBo(buffer);
                monitorInfoBoList.add(monitorInfoBo);
            }
        }

        final int lockedSynchronizerListSize = buffer.readVInt();
        final List<String> lockedSynchronizerList = new ArrayList<>(lockedSynchronizerListSize);
        if (lockedSynchronizerListSize > 0) {
            for (int i = 0; i < lockedSynchronizerListSize; i++) {
                final String string = buffer.readPrefixedString();
                lockedSynchronizerList.add(string);
            }
        }

        final ThreadDumpBo threadDumpBo = new ThreadDumpBo();
        threadDumpBo.setThreadName(threadName);
        threadDumpBo.setThreadId(threadId);
        threadDumpBo.setBlockedTime(blockedTime);
        threadDumpBo.setBlockedCount(blockedCount);
        threadDumpBo.setWaitedTime(waitedTime);
        threadDumpBo.setWaitedCount(waitedCount);
        threadDumpBo.setLockName(lockName);
        threadDumpBo.setLockOwnerId(lockOwnerId);
        threadDumpBo.setLockOwnerName(lockOwnerName);
        threadDumpBo.setInNative(inNative);
        threadDumpBo.setSuspended(suspended);
        threadDumpBo.setThreadState(ThreadState.findByValue(threadStateValue));
        threadDumpBo.setStackTraceList(stackTraceList);
        threadDumpBo.setLockedMonitorInfoList(monitorInfoBoList);
        threadDumpBo.setLockedSynchronizerList(lockedSynchronizerList);

        return threadDumpBo;
    }

    private MonitorInfoBo readMonitorInfoBo(final Buffer buffer) {
        final int stackDepth = buffer.readInt();
        final String stackFrame = buffer.readPrefixedString();

        final MonitorInfoBo monitorInfoBo = new MonitorInfoBo();
        monitorInfoBo.setStackDepth(stackDepth);
        monitorInfoBo.setStackFrame(stackFrame);
        return monitorInfoBo;
    }
}