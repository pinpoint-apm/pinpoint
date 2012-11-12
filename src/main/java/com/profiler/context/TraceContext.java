package com.profiler.context;


import com.profiler.sender.DataSender;
import com.profiler.sender.LoggingDataSender;
import com.profiler.util.NamedThreadLocal;

public class TraceContext {

    private static TraceContext CONTEXT = new TraceContext();

    // initailze관련 생명주기가 뭔가 애매함. 추후 방안을 더 고려해보자.
    public static TraceContext initialize() {
        return CONTEXT = new TraceContext();
    }

    // 얻는 것도 뭔가 모양이 마음에 안듬.
    public static TraceContext getTraceContext() {
        return CONTEXT;
    }

    private ThreadLocal<Trace> threadLocal = new NamedThreadLocal<Trace>("TraceContext");
    private final ActiveThreadCounter activeThreadCounter = new ActiveThreadCounter();

    private static final DataSender DEFAULT_DATA_SENDER = new LoggingDataSender();

    private DataSender dataSender = DEFAULT_DATA_SENDER;

    private GlobalCallTrace globalCallTrace = new GlobalCallTrace();

    public TraceContext() {
    }

    public Trace currentTraceObject() {
        return threadLocal.get();
    }

    public Trace attachTraceObject(TraceID traceID) {
        Trace trace = this.threadLocal.get();
        if (trace == null) {
//            TraceID traceID = TraceID.newTraceId();
            Trace newTrace = new Trace(traceID);
            newTrace.setDataSender(this.dataSender);
            threadLocal.set(newTrace);
            return newTrace;
        }
        throw new IllegalStateException("already Trace Object exist");
    }

    public void detachTraceObject() {
        this.threadLocal.set(null);
    }

    public GlobalCallTrace<AsyncTrace> getGlobalCallTrace() {
        return globalCallTrace;
    }

    public ActiveThreadCounter getActiveThreadCounter() {
        return activeThreadCounter;
    }

    public void setDataSender(DataSender dataSender) {
        this.dataSender = dataSender;
        this.globalCallTrace.setDataSender(dataSender);
    }
}
