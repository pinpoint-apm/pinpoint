package com.profiler.context;


import com.profiler.common.util.SqlParser;
import com.profiler.metadata.SqlCacheTable;
import com.profiler.util.Assert;
import com.profiler.util.NamedThreadLocal;

import java.util.concurrent.atomic.AtomicInteger;

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

    private ThreadLocal<Trace> threadLocal = new NamedThreadLocal<Trace>("Trace");

    private final ActiveThreadCounter activeThreadCounter = new ActiveThreadCounter();

    // internal stacktrace 추적때 필요한 unique 아이디, activethreadcount의  slow 타임 계산의 위해서도 필요할듯 함.
    private final AtomicInteger transactionId = new AtomicInteger(0);

    private GlobalCallTrace globalCallTrace = new GlobalCallTrace();

    private String agentId;

    private String applicationId;

    private StorageFactory storageFactory;

    private SqlCacheTable sqlTable = new SqlCacheTable(1000);
    private SqlParser sqlParser = new SqlParser();

    public TraceContext() {
    }

    public Trace currentTraceObject() {
        return threadLocal.get();
    }

    public void attachTraceObject(Trace trace) {
        Trace old = this.threadLocal.get();
        if (old != null) {
            // 잘못된 상황의 old를 덤프할것.
            throw new IllegalStateException("already Trace Object exist.");
        }
        // datasender연결 부분 수정 필요.
//        trace.setDataSender(this.dataSender);
        Storage storage = storageFactory.createStorage();
        trace.setStorage(storage);
        trace.setSqlCacheTable(this.sqlTable);
        trace.setSqlParser(this.sqlParser);
        //
//        trace.setTransactionId(transactionId.getAndIncrement());
        threadLocal.set(trace);
    }

    public void detachTraceObject() {
        this.threadLocal.set(null);
    }

    public GlobalCallTrace getGlobalCallTrace() {
        return globalCallTrace;
    }

    public ActiveThreadCounter getActiveThreadCounter() {
        return activeThreadCounter;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setStorageFactory(StorageFactory storageFactory) {
        Assert.notNull(storageFactory, "storageFactory myst not be null");
        this.storageFactory = storageFactory;
    }
}
