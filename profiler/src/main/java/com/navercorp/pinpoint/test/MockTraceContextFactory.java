package com.navercorp.pinpoint.test;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.profiler.context.DefaultTraceContext;

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
