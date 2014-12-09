package com.navercorp.pinpoint.bootstrap;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;

import java.lang.instrument.Instrumentation;

/**
 * @author emeroad
 * @author hyungil.jeong
 */
public class DummyAgent implements Agent {

    public DummyAgent(String agentPath, String agentArgs, Instrumentation instrumentation, ProfilerConfig profilerConfig) {

    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public TraceContext getTraceContext() {
        return null;
    }

    @Override
    public ProfilerConfig getProfilerConfig() {
        return null;
    }

    // @Override
    // public PLoggerBinder initializeLogger() {
    // return new PLoggerBinder() {
    // @Override
    // public PLogger getLogger(String name) {
    // return new DummyPLogger();
    // }
    //
    // @Override
    // public void shutdown() {
    //
    // }
    // };
    // }
}
