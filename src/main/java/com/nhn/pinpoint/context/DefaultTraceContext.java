package com.nhn.pinpoint.context;


import com.nhn.pinpoint.DefaultAgent;
import com.nhn.pinpoint.common.dto.thrift.ApiMetaData;
import com.nhn.pinpoint.common.dto.thrift.SqlMetaData;
import com.nhn.pinpoint.common.util.ParsingResult;
import com.nhn.pinpoint.common.util.SqlParser;
import com.nhn.pinpoint.exception.PinPointException;
import com.nhn.pinpoint.interceptor.MethodDescriptor;
import com.nhn.pinpoint.logging.Logger;
import com.nhn.pinpoint.logging.LoggerFactory;
import com.nhn.pinpoint.metadata.LRUCache;
import com.nhn.pinpoint.metadata.Result;
import com.nhn.pinpoint.metadata.StringCache;
import com.nhn.pinpoint.sampler.Sampler;
import com.nhn.pinpoint.sender.DataSender;
import com.nhn.pinpoint.util.Assert;
import com.nhn.pinpoint.util.NamedThreadLocal;

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

    /**
     * sampling 여부까지 체크하여 유효성을 검증한 후 Trace를 리턴한다.
     * @return
     */
    public Trace currentTraceObject() {
        Trace trace = threadLocal.get();
        if (trace == null) {
            return null;
        }
        if (trace.canSampled()) {
            return trace;
        }
        return null;
    }

    /**
     * 유효성을 검증하지 않고 Trace를 리턴한다.
     * @return
     */
    public Trace currentRawTraceObject() {
        return threadLocal.get();
    }

    public void disableSampling() {
        checkBeforeTraceObject();
        threadLocal.set(DisableTrace.INSTANCE);
    }

    // remote 에서 샘플링 대상으로 선정된 경우.
    public Trace continueTraceObject(TraceID traceID) {
        checkBeforeTraceObject();

        // datasender연결 부분 수정 필요.
        DefaultTrace trace = new DefaultTrace(traceID);
        Storage storage = storageFactory.createStorage();
        trace.setStorage(storage);
        trace.setTraceContext(this);
        // remote에 의해 trace가 continue될때는  sampling flag를 좀더 상위에서 하므로 무조껀 true여야함.
        // TODO remote에서 sampling flag로 마크가되는 대상으로 왔을 경우도 추가로 샘플링 칠수 있어야 할것으로 보임.
        trace.setSampling(true);

        threadLocal.set(trace);
        return trace;
    }

    private void checkBeforeTraceObject() {
        Trace old = this.threadLocal.get();
        if (old != null) {
            // 잘못된 상황의 old를 덤프할것.
            if (logger.isDebugEnabled()) {
                logger.warn("beforeTrace:{}", old);
            }
            throw new PinPointException("already Trace Object exist.");
        }
    }

    public Trace newTraceObject() {
        checkBeforeTraceObject();
        // datasender연결 부분 수정 필요.
        boolean sampling = this.sampler.isSampling();
        if(sampling) {
            DefaultTrace trace = new DefaultTrace();
            Storage storage = storageFactory.createStorage();
            trace.setStorage(storage);
            trace.setTraceContext(this);
            trace.setSampling(sampling);
            threadLocal.set(trace);
            return trace;
        } else {
            DisableTrace instance = DisableTrace.INSTANCE;
            threadLocal.set(instance);
            return instance;
        }
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
            apiMetadata.setAgentStartTime(agent.getStartTime());

            apiMetadata.setApiId(result.getId());
            apiMetadata.setApiInfo(methodDescriptor.getApiDescriptor());
            apiMetadata.setLine(methodDescriptor.getLineNumber());

            this.priorityDataSender.send(apiMetadata);
            methodDescriptor.setApiId(result.getId());
        }
        return result.getId();
    }

    @Override
    public TraceID createTraceId(UUID uuid, int parentSpanID, int spanID, short flags) {
        return new DefaultTraceID(uuid, parentSpanID, spanID, flags);
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


            SqlMetaData sqlMetaData = new SqlMetaData();
            sqlMetaData.setAgentId(DefaultAgent.getInstance().getAgentId());
            sqlMetaData.setAgentStartTime(DefaultAgent.getInstance().getStartTime());

            sqlMetaData.setHashCode(normalizedSql.hashCode());
            sqlMetaData.setSql(normalizedSql);

            // 좀더 신뢰성이 있는 tcp connection이 필요함.
            this.priorityDataSender.send(sqlMetaData);
        }
        // hashId그냥 return String에서 까보면 됨.
        return parsingResult;
    }


    public void setPriorityDataSender(DataSender priorityDataSender) {
        this.priorityDataSender = priorityDataSender;
    }
}
