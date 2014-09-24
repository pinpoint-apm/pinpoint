package com.nhn.pinpoint.bootstrap;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.context.TraceContext;

import java.lang.instrument.Instrumentation;

/**
 * @author emeroad
 * @author hyungil.jeong
 */
public class DummyAgent implements Agent {

    public DummyAgent(String agentArgs, Instrumentation instrumentation, ProfilerConfig profilerConfig) {

    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void addConnector(String protocol, int port) {
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
