package com.profiler.context;


import com.profiler.DefaultAgent;
import com.profiler.common.dto.thrift.ApiMetaData;
import com.profiler.common.dto.thrift.SqlMetaData;
import com.profiler.common.util.ParsingResult;
import com.profiler.common.util.SqlParser;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;
import com.profiler.metadata.LRUCache;
import com.profiler.metadata.Result;
import com.profiler.metadata.StringCache;
import com.profiler.sampler.Sampler;
import com.profiler.sender.DataSender;
import com.profiler.util.Assert;
import com.profiler.util.NamedThreadLocal;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultTraceContext implements TraceContext {

    private final Logger logger = LoggerFactory.getLogger(DefaultTraceContext.class.getName());


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

    private Sampler sampler;

    public DefaultTraceContext() {
    }

    public Trace currentTraceObject() {
        return threadLocal.get();
    }

    public Trace continueTraceObject(TraceID traceID) {
        Trace old = this.threadLocal.get();
        if (old != null) {
            // 잘못된 상황의 old를 덤프할것.
            throw new IllegalStateException("already Trace Object exist.");
        }
        // datasender연결 부분 수정 필요.
        DefaultTrace trace = new DefaultTrace(traceID);
        Storage storage = storageFactory.createStorage();
        trace.setStorage(storage);
        trace.setTraceContext(this);
        trace.setSampling(this.sampler.isSampling());

        threadLocal.set(trace);
        return trace;
    }

    public Trace newTraceObject() {
        Trace old = this.threadLocal.get();
        if (old != null) {
            // 잘못된 상황의 old를 덤프할것.
            throw new IllegalStateException("already Trace Object exist.");
        }
        // datasender연결 부분 수정 필요.
        DefaultTrace trace = new DefaultTrace();
        Storage storage = storageFactory.createStorage();
        trace.setStorage(storage);
        trace.setTraceContext(this);
        trace.setSampling(this.sampler.isSampling());


        threadLocal.set(trace);
        return trace;
    }


    @Override
    public void detachTraceObject() {
        this.threadLocal.remove();
    }

    public GlobalCallTrace getGlobalCallTrace() {
        return globalCallTrace;
    }

    //@Override
    public ActiveThreadCounter getActiveThreadCounter() {
        return activeThreadCounter;
    }

    @Override
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    @Override
    public String getApplicationId() {
        return applicationId;
    }

    public void setStorageFactory(StorageFactory storageFactory) {
        Assert.notNull(storageFactory, "storageFactory myst not be null");
        this.storageFactory = storageFactory;
    }

    public void setSampler(Sampler sampler) {
        this.sampler = sampler;
    }


    @Override
    public int cacheApi(MethodDescriptor methodDescriptor) {
        String fullName = methodDescriptor.getFullName();
        Result result = this.apiCache.put(fullName);
        if (result.isNewValue()) {
            ApiMetaData apiMetadata = new ApiMetaData();
            DefaultAgent agent = DefaultAgent.getInstance();
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

    @Override
    public TraceID createTraceId(UUID uuid, int parentSpanID, int spanID, boolean sampled, short flags) {
        return new DefaultTraceID(uuid, parentSpanID, spanID, sampled, flags);
    }


    @Override
    public ParsingResult parseSql(String sql) {

        ParsingResult parsingResult = this.sqlParser.normalizedSql(sql);
        String normalizedSql = parsingResult.getSql();
        // 파싱시 변경되지 않았다면 동일 객체를 리턴하므로 그냥 ==비교를 하면 됨
        boolean newValue = this.sqlCache.put(normalizedSql);
        if (newValue) {
            if (logger.isDebugEnabled()) {
                // TODO hit% 로그를 남겨야 문제 발생시 도움이 될듯 하다.
                logger.debug("NewSQLParsingResult:{}", parsingResult);
            }
            // newValue란 의미는 cache에 인입됬다는 의미이고 이는 신규 sql문일 가능성이 있다는 의미임.
            // 그러므로 메타데이터를 서버로 전송해야 한다.

            // 프로파일 데이터를 보내는데 사용되는 queue가 아니고,
            // 좀더 급한 메시지만 별도 처리할수 있는 상대적으로 더 한가한 queue와 datasender를 별도로 가지고 있는게 좋을듯 하다.
            SqlMetaData sqlMetaData = new SqlMetaData();
            sqlMetaData.setAgentId(DefaultAgent.getInstance().getAgentId());
            sqlMetaData.setAgentIdentifier(DefaultAgent.getInstance().getIdentifier());

            sqlMetaData.setStartTime(DefaultAgent.getInstance().getStartTime());
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
