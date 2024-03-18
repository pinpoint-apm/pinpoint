package com.navercorp.pinpoint.profiler.context.grpc;

import com.navercorp.pinpoint.grpc.trace.PThreadDump;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.ThreadDumpMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.ThreadDumpMapperImpl;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.MonitorInfoMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.ThreadDumpMetricSnapshot;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.navercorp.pinpoint.profiler.context.grpc.MapperTestUtil.randomString;
import static com.navercorp.pinpoint.profiler.context.grpc.MapperTestUtil.randomStringList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author intr3p1d
 */
class GrpcThreadDumpMessageConverterTest {

    ThreadDumpMapper mapper = new ThreadDumpMapperImpl();
    GrpcThreadDumpMessageConverter converter = new GrpcThreadDumpMessageConverter(mapper);
    Random random = new Random();

    ThreadDumpMetricSnapshot newThreadDumpMetricSnapshot() {
        ThreadDumpMetricSnapshot snapshot = mock(ThreadDumpMetricSnapshot.class);
        when(snapshot.getThreadName()).thenReturn(randomString());
        when(snapshot.getThreadId()).thenReturn(random.nextLong());
        when(snapshot.getBlockedTime()).thenReturn(random.nextLong());
        when(snapshot.getBlockedCount()).thenReturn(random.nextLong());
        when(snapshot.getLockName()).thenReturn(randomString());
        when(snapshot.getLockOwnerId()).thenReturn(random.nextLong());
        when(snapshot.isInNative()).thenReturn(random.nextBoolean());
        when(snapshot.isSuspended()).thenReturn(random.nextBoolean());
        when(snapshot.getThreadState()).thenReturn(Thread.State.values()[random.nextInt(Thread.State.values().length)]);
        when(snapshot.getStackTrace()).thenReturn(randomStringList());
        when(snapshot.getLockedMonitors()).thenReturn(newLockedMetricSnapshot());
        when(snapshot.getLockedSynchronizers()).thenReturn(randomStringList());
        return snapshot;
    }

    List<MonitorInfoMetricSnapshot> newLockedMetricSnapshot() {
        List<MonitorInfoMetricSnapshot> monitorInfoMetricSnapshots = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            MonitorInfoMetricSnapshot snapshot = new MonitorInfoMetricSnapshot();
            snapshot.setStackDepth(random.nextInt());
            snapshot.setStackFrame(randomString());
            monitorInfoMetricSnapshots.add(snapshot);
        }
        return monitorInfoMetricSnapshots;
    }

    @Test
    void testThreadDump() {
        ThreadDumpMetricSnapshot threadDumpMetricSnapshot = newThreadDumpMetricSnapshot();
        PThreadDump pThreadDump = converter.toMessage(threadDumpMetricSnapshot);

        assertEquals(threadDumpMetricSnapshot.getThreadName(), pThreadDump.getThreadName());
        assertEquals(threadDumpMetricSnapshot.getThreadId(), pThreadDump.getThreadId());
        assertEquals(threadDumpMetricSnapshot.getBlockedTime(), pThreadDump.getBlockedTime());
        assertEquals(threadDumpMetricSnapshot.getBlockedCount(), pThreadDump.getBlockedCount());
        assertEquals(threadDumpMetricSnapshot.getLockName(), pThreadDump.getLockName());
        assertEquals(threadDumpMetricSnapshot.getLockOwnerId(), pThreadDump.getLockOwnerId());
        assertEquals(threadDumpMetricSnapshot.isInNative(), pThreadDump.getInNative());
        assertEquals(threadDumpMetricSnapshot.isSuspended(), pThreadDump.getSuspended());
        assertEquals("THREAD_STATE_" + threadDumpMetricSnapshot.getThreadState().name(), pThreadDump.getThreadState().name());
        assertEquals(threadDumpMetricSnapshot.getStackTrace(), pThreadDump.getStackTraceList());
        assertEquals(threadDumpMetricSnapshot.getLockedSynchronizers(), pThreadDump.getLockedSynchronizerList());
    }

}