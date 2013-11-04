package com.nhn.pinpoint.bootstrap;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.logging.DummyPLogger;
import com.nhn.pinpoint.profiler.logging.PLogger;
import com.nhn.pinpoint.profiler.logging.PLoggerBinder;

import java.lang.instrument.Instrumentation;

/**
 * @author emeroad
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
    public ProfilerConfig getProfilerConfig() {
        return null;
    }

//    @Override
//    public PLoggerBinder initializeLogger() {
//        return new PLoggerBinder() {
//            @Override
//            public PLogger getLogger(String name) {
//                return new DummyPLogger();
//            }
//
//            @Override
//            public void shutdown() {
//
//            }
//        };
//    }
}
