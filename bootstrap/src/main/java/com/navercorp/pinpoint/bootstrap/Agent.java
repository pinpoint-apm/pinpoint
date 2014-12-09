package com.navercorp.pinpoint.bootstrap;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;

/**
 * @author emeroad
 * @author hyungil.jeong
 */
public interface Agent {

    void start();

    void stop();
    
    TraceContext getTraceContext();

    ProfilerConfig getProfilerConfig();

}
