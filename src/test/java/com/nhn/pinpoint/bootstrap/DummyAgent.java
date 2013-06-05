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
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void started() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stop() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addConnector(String protocol, int port) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TraceContext getTraceContext() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object getByteCodeInstrumentor() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ProfilerConfig getProfilerConfig() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
