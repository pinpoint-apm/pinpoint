package com.navercorp.pinpoint.profiler.context.id;

import com.navercorp.pinpoint.bootstrap.context.TraceId;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultTraceIdTest {
    @Test
    public void testMode() {
        TraceId traceId = new DefaultTraceId("agentId", 1600686110110L, 1);
        assertTrue("not full profiling mode", traceId.isFullModeTrace());
        assertFalse("should not be lite mode profiling", traceId.isLiteModeTrace());
        short liteMode = 1;
        TraceId liteTraceId = new DefaultTraceId("agentId", 1600686110110L, 2, liteMode);
        assertTrue("should be lite mode profiling", liteTraceId.isLiteModeTrace());
        assertFalse("should not be full profiling mode", liteTraceId.isFullModeTrace());

    }
}
