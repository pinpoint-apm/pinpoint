package com.nhn.pinpoint.bootstrap;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.context.TraceContext;

/**
 * @author emeroad
 * @author hyungil.jeong
 */
public interface Agent {

    void start();

    void stop();
    
    void addConnector(String protocol, int port);

    TraceContext getTraceContext();

    ProfilerConfig getProfilerConfig();

}
