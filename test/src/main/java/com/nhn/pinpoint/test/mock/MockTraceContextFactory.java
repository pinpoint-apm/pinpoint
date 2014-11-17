package com.nhn.pinpoint.test.mock;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.profiler.context.DefaultTraceContext;

/**
 * @author emeroad
 */
public class MockTraceContextFactory {
    public TraceContext create() {
        DefaultTraceContext traceContext = new DefaultTraceContext() ;
		ProfilerConfig profilerConfig = new ProfilerConfig();
		traceContext.setProfilerConfig(profilerConfig);
		return traceContext;
    }
}
