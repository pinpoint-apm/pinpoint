package com.nhn.pinpoint.bootstrap;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.logging.DummyLogger;
import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerBinder;

import java.lang.instrument.Instrumentation;

/**
 *
 */
public class DummyAgent implements Agent {

    public DummyAgent(String agentArgs, Instrumentation instrumentation, ProfilerConfig profilerConfig) {

    }

    @Override
    public void start() {
    }

    @Override
    public void started() {
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
    public Object getByteCodeInstrumentor() {
        return null;
    }

    @Override
    public ProfilerConfig getProfilerConfig() {
        return null;
    }

    @Override
    public LoggerBinder initializeLogger() {
        return new LoggerBinder() {
            @Override
            public Logger getLogger(String name) {
                return new DummyLogger();
            }

            @Override
            public void shutdown() {

            }
        };
    }
}
