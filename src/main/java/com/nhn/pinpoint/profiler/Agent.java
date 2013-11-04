package com.nhn.pinpoint.profiler;

import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.context.TraceContext;

/**
 * @author emeroad
 */
public interface Agent {
    // TODO 필요없을것 같음 started를 start로 바꿔도 될 듯...
    void start();

    void started();

    void stop();

    void addConnector(String protocol, int port);

    TraceContext getTraceContext();

    ProfilerConfig getProfilerConfig();

}
