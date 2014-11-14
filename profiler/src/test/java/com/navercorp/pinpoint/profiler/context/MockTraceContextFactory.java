package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.Version;
import com.nhn.pinpoint.profiler.AgentInformation;

/**
 * @author emeroad
 */
public class MockTraceContextFactory {
    public TraceContext create() {
        DefaultTraceContext traceContext = new DefaultTraceContext();
        ProfilerConfig profilerConfig = new ProfilerConfig();
        traceContext.setProfilerConfig(profilerConfig);
        AgentInformation agentInformation = createAgentInformation();
        traceContext.setAgentInformation(agentInformation);
        return traceContext;
    }

    private AgentInformation createAgentInformation() {
        return new AgentInformation("testAgent", "testApplicationName", System.currentTimeMillis(), 12, "testMachineName", "127.0.0.1", ServiceType.TEST_STAND_ALONE.getCode(), Version.VERSION);
    }
}
