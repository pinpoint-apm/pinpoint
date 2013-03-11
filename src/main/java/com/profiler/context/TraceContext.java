package com.profiler.context;


import com.profiler.Agent;
import com.profiler.common.dto.thrift.ApiMetaData;
import com.profiler.common.dto.thrift.SqlMetaData;
import com.profiler.common.util.ParsingResult;
import com.profiler.common.util.SqlParser;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.metadata.LRUCache;
import com.profiler.metadata.Result;
import com.profiler.metadata.StringCache;
import com.profiler.sender.DataSender;
import com.profiler.util.Assert;
import com.profiler.util.NamedThreadLocal;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TraceContext {

    private final Logger logger = Logger.getLogger(TraceContext.class.getName());

    private static TraceContext CONTEXT = new TraceContext();

    // initailze관련 생명주기가 뭔가 애매함. 추후 방안을 더 고려해보자.
    public static TraceContext initialize() {
        return CONTEXT = new TraceContext();
    }

    // 얻는 것도 뭔가 모양이 마음에 안듬.
    public static TraceContext getTraceContext() {
        return CONTEXT;
    }

    private final ThreadLocal<Trace> threadLocal = new NamedThreadLocal<Trace>("Trace");

    private final ActiveThreadCounter activeThreadCounter = new ActiveThreadCounter();

    // internal stacktrace 추적때 필요한 unique 아이디, activethreadcount의  slow 타임 계산의 위해서도 필요할듯 함.
    private final AtomicInteger transactionId = new AtomicInteger(0);

    private GlobalCallTrace globalCallTrace = new GlobalCallTrace();

    private String agentId;

    private String applicationId;

    private DataSender priorityDataSender;

    private StorageFactory storageFactory;

    private final LRUCache<String> sqlCache = new LRUCache<String>(1000);
    private final SqlParser sqlParser = new SqlParser();

    private final StringCache apiCache = new StringCache();

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
        trace.setTraceContext(this);

        //
//        trace.setTransactionId(transactionId.getAndIncrement());
        threadLocal.set(trace);
    }

    public void detachTraceObject() {
        this.threadLocal.remove();
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


    public int cacheApi(MethodDescriptor methodDescriptor) {
        String fullName = methodDescriptor.getFullName();
        Result result = this.apiCache.put(fullName);
        if (result.isNewValue()) {
            ApiMetaData apiMetadata = new ApiMetaData();
            Agent agent = Agent.getInstance();
            apiMetadata.setAgentId(agent.getAgentId());
            apiMetadata.setAgentIdentifier(agent.getIdentifier());

            apiMetadata.setStartTime(agent.getStartTime());
            apiMetadata.setApiId(result.getId());
            apiMetadata.setApiInfo(methodDescriptor.getApiDescriptor());
            apiMetadata.setLine(methodDescriptor.getLineNumber());

            this.priorityDataSender.send(apiMetadata);
            methodDescriptor.setApiId(result.getId());
        }
        return result.getId();
    }



    public ParsingResult parseSql(String sql) {

        ParsingResult parsingResult = this.sqlParser.normalizedSql(sql);
        String normalizedSql = parsingResult.getSql();
        // 파싱시 변경되지 않았다면 동일 객체를 리턴하므로 그냥 ==비교를 하면 됨
        boolean newValue = this.sqlCache.put(normalizedSql);
        if (newValue) {
            if (logger.isLoggable(Level.FINE)) {
                // TODO hit% 로그를 남겨야 문제 발생시 도움이 될듯 하다.
                logger.fine("NewSQLParsingResult:" + parsingResult);
            }
            // newValue란 의미는 cache에 인입됬다는 의미이고 이는 신규 sql문일 가능성이 있다는 의미임.
            // 그러므로 메타데이터를 서버로 전송해야 한다.

            // 프로파일 데이터를 보내는데 사용되는 queue가 아니고,
            // 좀더 급한 메시지만 별도 처리할수 있는 상대적으로 더 한가한 queue와 datasender를 별도로 가지고 있는게 좋을듯 하다.
            SqlMetaData sqlMetaData = new SqlMetaData();
            sqlMetaData.setAgentId(Agent.getInstance().getAgentId());
            sqlMetaData.setAgentIdentifier(Agent.getInstance().getIdentifier());

            sqlMetaData.setStartTime(Agent.getInstance().getStartTime());
            sqlMetaData.setHashCode(normalizedSql.hashCode());
            sqlMetaData.setSql(normalizedSql);

            this.priorityDataSender.send(sqlMetaData);
        }
        // hashId그냥 return String에서 까보면 됨.
        return parsingResult;
    }

    public void setPriorityDataSender(DataSender priorityDataSender) {
        this.priorityDataSender = priorityDataSender;
    }
}
