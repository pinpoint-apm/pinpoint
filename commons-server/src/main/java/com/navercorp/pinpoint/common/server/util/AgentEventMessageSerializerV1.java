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

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.event.DeadlockBo;
import com.navercorp.pinpoint.common.server.bo.event.MonitorInfoBo;
import com.navercorp.pinpoint.common.server.bo.event.ThreadDumpBo;

/**
 * @author jaehong.kim
 * AgentEventBo.version is 1
 */
public class AgentEventMessageSerializerV1 {

    public byte[] serialize(AgentEventType agentEventType, Object eventMessage) {
        if (agentEventType == null) {
            throw new NullPointerException("agentEventType");
        }

        if (eventMessage instanceof DeadlockBo) {
            return serializeDeadlockBo((DeadlockBo) eventMessage);
        }

        throw new IllegalArgumentException("Unsupported event message type [" + eventMessage.getClass().getName() + "]");
    }

    private byte[] serializeDeadlockBo(final DeadlockBo deadlockBo) {
        final Buffer buffer = new AutomaticBuffer();
        buffer.putInt(deadlockBo.getDeadlockedThreadCount());
        // Put ThreadDumpBoList
        final int threadDumpBoListSize = deadlockBo.getThreadDumpBoList() == null ? 0 : deadlockBo.getThreadDumpBoList().size();
        buffer.putVInt(threadDumpBoListSize);
        if (threadDumpBoListSize > 0) {
            for (ThreadDumpBo threadDumpBo : deadlockBo.getThreadDumpBoList()) {
                putThreadDumpBo(buffer, threadDumpBo);
            }
        }
        return buffer.getBuffer();
    }

    private void putThreadDumpBo(final Buffer buffer, final ThreadDumpBo threadDumpBo) {
        buffer.putPrefixedString(threadDumpBo.getThreadName());
        buffer.putLong(threadDumpBo.getThreadId());
        buffer.putLong(threadDumpBo.getBlockedTime());
        buffer.putLong(threadDumpBo.getBlockedCount());
        buffer.putLong(threadDumpBo.getWaitedTime());
        buffer.putLong(threadDumpBo.getWaitedCount());
        buffer.putPrefixedString(threadDumpBo.getLockName());
        buffer.putLong(threadDumpBo.getLockOwnerId());
        buffer.putPrefixedString(threadDumpBo.getLockOwnerName());
        buffer.putBoolean(threadDumpBo.isInNative());
        buffer.putBoolean(threadDumpBo.isSuspended());
        buffer.putInt(threadDumpBo.getThreadState().getValue());
        final int stackTraceSize = threadDumpBo.getStackTraceList() == null ? 0 : threadDumpBo.getStackTraceList().size();
        buffer.putVInt(stackTraceSize);
        if (stackTraceSize > 0) {
            for (String string : threadDumpBo.getStackTraceList()) {
                buffer.putPrefixedString(string);
            }
        }

        final int lockedMonitorListSize = threadDumpBo.getLockedMonitorInfoList() == null ? 0 : threadDumpBo.getLockedMonitorInfoList().size();
        buffer.putVInt(lockedMonitorListSize);
        if (lockedMonitorListSize > 0) {
            for (MonitorInfoBo monitorInfoBo : threadDumpBo.getLockedMonitorInfoList()) {
                putMonitorInfoBo(buffer, monitorInfoBo);
            }
        }

        final int lockedSynchronizerListSize = threadDumpBo.getLockedSynchronizerList() == null ? 0 : threadDumpBo.getLockedSynchronizerList().size();
        buffer.putVInt(lockedSynchronizerListSize);
        if (lockedSynchronizerListSize > 0) {
            for (String string : threadDumpBo.getLockedSynchronizerList()) {
                buffer.putPrefixedString(string);
            }
        }
    }

    private void putMonitorInfoBo(final Buffer buffer, final MonitorInfoBo monitorInfoBo) {
        buffer.putInt(monitorInfoBo.getStackDepth());
        buffer.putPrefixedString(monitorInfoBo.getStackFrame());
    }
}