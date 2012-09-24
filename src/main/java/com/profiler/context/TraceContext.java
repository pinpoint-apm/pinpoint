package com.profiler.context;

import com.profiler.util.NamedThreadLocal;

public class TraceContext {

    private static TraceContext CONTEXT  = new TraceContext();

    public static TraceContext initialize() {
        return CONTEXT = new TraceContext();
    }

    public static TraceContext getTraceContext() {
        return CONTEXT;
    }

//    private ThreadLocal<ThreadLocalTraceContext> threadLocal = new NamedThreadLocal<ThreadLocalTraceContext>("threadLocalTraceContext");
    private final ActiveThreadCounter activeThreadCounter = new ActiveThreadCounter();

    public TraceContext() {
    }

    public ActiveThreadCounter getActiveThreadCounter() {
        return activeThreadCounter;
    }
}
