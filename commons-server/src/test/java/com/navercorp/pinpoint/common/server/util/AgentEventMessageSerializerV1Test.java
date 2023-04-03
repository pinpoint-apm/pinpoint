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

import com.navercorp.pinpoint.common.server.bo.event.DeadlockBo;
import com.navercorp.pinpoint.common.server.bo.event.MonitorInfoBo;
import com.navercorp.pinpoint.common.server.bo.event.ThreadDumpBo;
import com.navercorp.pinpoint.common.server.bo.event.ThreadState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jaehong.kim
 */
public class AgentEventMessageSerializerV1Test {

    @Test
    public void serialize() throws Exception {
        AgentEventMessageSerializerV1 serializer = new AgentEventMessageSerializerV1();
        // Mock
        ThreadDumpBo threadDumpBo = new ThreadDumpBo();
        threadDumpBo.setThreadName("threadName");
        threadDumpBo.setThreadId(0);
        threadDumpBo.setBlockedTime(1);
        threadDumpBo.setBlockedCount(2);
        threadDumpBo.setWaitedTime(3);
        threadDumpBo.setWaitedCount(4);
        threadDumpBo.setLockName("lockName");
        threadDumpBo.setLockOwnerId(5);
        threadDumpBo.setLockOwnerName("lockOwnerName");
        threadDumpBo.setInNative(Boolean.TRUE);
        threadDumpBo.setSuspended(Boolean.FALSE);
        threadDumpBo.setThreadState(ThreadState.RUNNABLE);
        threadDumpBo.setStackTraceList(List.of("foo", "bar"));

        MonitorInfoBo monitorInfoBo = new MonitorInfoBo(9, "Frame");
        List<MonitorInfoBo> monitorInfoBoList = List.of(monitorInfoBo);
        threadDumpBo.setLockedMonitorInfoList(monitorInfoBoList);
        threadDumpBo.setLockedSynchronizerList(List.of("foo", "bar"));

        List<ThreadDumpBo> threadDumpBoList = List.of(threadDumpBo);
        final DeadlockBo deadlockBo = new DeadlockBo(1, threadDumpBoList);

        byte[] bytes = serializer.serialize(AgentEventType.AGENT_DEADLOCK_DETECTED, deadlockBo);

        // deserialize
        AgentEventMessageDeserializerV1 deserializer = new AgentEventMessageDeserializerV1();
        Object object = deserializer.deserialize(AgentEventType.AGENT_DEADLOCK_DETECTED, bytes);

        assertThat(object).isInstanceOf(DeadlockBo.class);

        DeadlockBo result = (DeadlockBo) object;

        assertEquals(1, result.getDeadlockedThreadCount());
        assertThat(result.getThreadDumpBoList()).hasSize(1);
        assertThreadDumpBo(threadDumpBo, result.getThreadDumpBoList().get(0));
    }

    private void assertThreadDumpBo(ThreadDumpBo expectedThreadDumpBo, ThreadDumpBo threadDumpBo) {
        assertEquals(expectedThreadDumpBo.getThreadName(), threadDumpBo.getThreadName());
        assertEquals(expectedThreadDumpBo.getThreadId(), threadDumpBo.getThreadId());
        assertEquals(expectedThreadDumpBo.getBlockedTime(), threadDumpBo.getBlockedTime());
        assertEquals(expectedThreadDumpBo.getBlockedCount(), threadDumpBo.getBlockedCount());
        assertEquals(expectedThreadDumpBo.getWaitedTime(), threadDumpBo.getWaitedTime());
        assertEquals(expectedThreadDumpBo.getWaitedCount(), threadDumpBo.getWaitedCount());
        assertEquals(expectedThreadDumpBo.getLockName(), threadDumpBo.getLockName());
        assertEquals(expectedThreadDumpBo.getLockOwnerId(), threadDumpBo.getLockOwnerId());
        assertEquals(expectedThreadDumpBo.getLockOwnerName(), threadDumpBo.getLockOwnerName());
        assertEquals(expectedThreadDumpBo.isInNative(), threadDumpBo.isInNative());
        assertEquals(expectedThreadDumpBo.isSuspended(), threadDumpBo.isSuspended());
        assertEquals(expectedThreadDumpBo.getThreadState().getValue(), threadDumpBo.getThreadState().getValue());
        assertThat(expectedThreadDumpBo.getStackTraceList()).hasSameSizeAs(threadDumpBo.getStackTraceList());
        for (int i = 0; i < expectedThreadDumpBo.getStackTraceList().size(); i++) {
            final String expectedStackTrace = expectedThreadDumpBo.getStackTraceList().get(i);
            final String stackTrace = threadDumpBo.getStackTraceList().get(i);
            assertEquals(expectedStackTrace, stackTrace);
        }
        assertEquals(expectedThreadDumpBo.getLockedMonitorInfoList().size(), threadDumpBo.getLockedMonitorInfoList().size());
        for (int i = 0; i < expectedThreadDumpBo.getLockedMonitorInfoList().size(); i++) {
            final MonitorInfoBo expectedMonitorInfoBo = expectedThreadDumpBo.getLockedMonitorInfoList().get(i);
            final MonitorInfoBo monitorInfoBo = threadDumpBo.getLockedMonitorInfoList().get(i);
            assertEquals(expectedMonitorInfoBo.getStackDepth(), monitorInfoBo.getStackDepth());
            assertEquals(expectedMonitorInfoBo.getStackFrame(), monitorInfoBo.getStackFrame());
        }
        assertEquals(expectedThreadDumpBo.getLockedSynchronizerList().size(), threadDumpBo.getLockedSynchronizerList().size());
        for (int i = 0; i < expectedThreadDumpBo.getLockedSynchronizerList().size(); i++) {
            final String expectedLockedSynchronizer = expectedThreadDumpBo.getLockedSynchronizerList().get(i);
            final String lockedSynchronizer = threadDumpBo.getLockedSynchronizerList().get(i);
            assertEquals(expectedLockedSynchronizer, lockedSynchronizer);
        }
    }
}