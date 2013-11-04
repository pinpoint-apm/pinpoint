package com.nhn.pinpoint.profiler.context;


import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.exception.PinpointException;
import com.nhn.pinpoint.profiler.AgentInformation;
import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.metadata.SimpleCache;
import com.nhn.pinpoint.profiler.sender.EnhancedDataSender;
import com.nhn.pinpoint.thrift.dto.TApiMetaData;
import com.nhn.pinpoint.thrift.dto.TSqlMetaData;
import com.nhn.pinpoint.common.util.ParsingResult;
import com.nhn.pinpoint.common.util.SqlParser;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.metadata.LRUCache;
import com.nhn.pinpoint.profiler.metadata.Result;
import com.nhn.pinpoint.profiler.modifier.db.JDBCUrlParser;
import com.nhn.pinpoint.profiler.sampler.Sampler;
import com.nhn.pinpoint.profiler.sender.DataSender;
import com.nhn.pinpoint.profiler.util.NamedThreadLocal;
import com.nhn.pinpoint.thrift.dto.TStringMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public class DefaultTraceContext implements TraceContext {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ThreadLocal<Trace> threadLocal = new NamedThreadLocal<Trace>("Trace");

    private final ActiveThreadCounter activeThreadCounter = new ActiveThreadCounter();

    // internal stacktrace 추적때 필요한 unique 아이디, activethreadcount의  slow 타임 계산의 위해서도 필요할듯 함.
    private final AtomicInteger transactionId = new AtomicInteger(0);

//    private GlobalCallTrace globalCallTrace = new GlobalCallTrace();

    private AgentInformation agentInformation;

    private EnhancedDataSender priorityDataSender;

    private StorageFactory storageFactory;

    private final LRUCache<String> sqlCache;
    private final SqlParser sqlParser = new SqlParser();

    private final SimpleCache<String> apiCache = new SimpleCache<String>();
    private final SimpleCache<String> stringCache = new SimpleCache<String>();

    private final JDBCUrlParser jdbcUrlParser = new JDBCUrlParser();

    private Sampler sampler;

    private ProfilerConfig profilerConfig;


    public DefaultTraceContext() {
        this(LRUCache.DEFAULT_CACHE_SIZE);
    }

    public DefaultTraceContext(final int sqlCacheSize) {
        this.sqlCache = new LRUCache<String>(sqlCacheSize);
    }


    /**
     * sampling 여부까지 체크하여 유효성을 검증한 후 Trace를 리턴한다.
     * @return
     */
    public Trace currentTraceObject() {
        final Trace trace = threadLocal.get();
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
    @Override
    public Trace currentRawTraceObject() {
        return threadLocal.get();
    }

    @Override
    public void disableSampling() {
        checkBeforeTraceObject();
        threadLocal.set(DisableTrace.INSTANCE);
    }

    public void setProfilerConfig(final ProfilerConfig profilerConfig) {
        this.profilerConfig = profilerConfig;
    }

    @Override
    public ProfilerConfig getProfilerConfig() {
        return profilerConfig;
    }

    // remote 에서 샘플링 대상으로 선정된 경우.
    public Trace continueTraceObject(final TraceId traceID) {
        checkBeforeTraceObject();

        // datasender연결 부분 수정 필요.
        final DefaultTrace trace = new DefaultTrace(this, traceID);
        final Storage storage = storageFactory.createStorage();
        trace.setStorage(storage);
        // remote에 의해 trace가 continue될때는  sampling flag를 좀더 상위에서 하므로 무조껀 true여야함.
        // TODO remote에서 sampling flag로 마크가되는 대상으로 왔을 경우도 추가로 샘플링 칠수 있어야 할것으로 보임.
        trace.setSampling(true);

        threadLocal.set(trace);
        return trace;
    }

    private void checkBeforeTraceObject() {
        final Trace old = this.threadLocal.get();
        if (old != null) {
            // 잘못된 상황의 old를 덤프할것.
            if (logger.isWarnEnabled()) {
                logger.warn("beforeTrace:{}", old);
            }
            throw new PinpointException("already Trace Object exist.");
        }
    }

    public Trace newTraceObject() {
        checkBeforeTraceObject();
        // datasender연결 부분 수정 필요.
        final boolean sampling = this.sampler.isSampling();
        if (sampling) {
            final Storage storage = storageFactory.createStorage();
            final DefaultTrace trace = new DefaultTrace(this, agentInformation.getAgentId(), agentInformation.getStartTime(), this.transactionId.getAndIncrement());
            trace.setStorage(storage);
            trace.setSampling(sampling);
            threadLocal.set(trace);
            return trace;
        } else {
            final DisableTrace instance = DisableTrace.INSTANCE;
            threadLocal.set(DisableTrace.INSTANCE);
            return instance;
        }
    }


    @Override
    public void detachTraceObject() {
        this.threadLocal.remove();
    }

//    public GlobalCallTrace getGlobalCallTrace() {
//        return globalCallTrace;
//    }

    //@Override
    public ActiveThreadCounter getActiveThreadCounter() {
        return activeThreadCounter;
    }

    public AgentInformation getAgentInformation() {
        return agentInformation;
    }

    @Override
    public String getAgentId() {
        return this.agentInformation.getAgentId();
    }

    @Override
    public String getApplicationName() {
        return this.agentInformation.getApplicationName();
    }

    @Override
    public long getAgentStartTime() {
        return this.agentInformation.getStartTime();
    }

    @Override
    public short getServerTypeCode() {
        return this.agentInformation.getServerType();
    }

    @Override
    public String getServerType() {
        return ServiceType.findServiceType(this.agentInformation.getServerType()).getDesc();
    }


    public void setStorageFactory(final StorageFactory storageFactory) {
        if (storageFactory == null) {
            throw new NullPointerException("storageFactory must not be null");
        }
        this.storageFactory = storageFactory;
    }

    public void setSampler(final Sampler sampler) {
        if (sampler == null) {
            throw new NullPointerException("sampler must not be null");
        }
        this.sampler = sampler;
    }


    @Override
    public int cacheApi(final MethodDescriptor methodDescriptor) {
        final String fullName = methodDescriptor.getFullName();
        final Result result = this.apiCache.put(fullName);
        if (result.isNewValue()) {
            methodDescriptor.setApiId(result.getId());

            final TApiMetaData apiMetadata = new TApiMetaData();
            apiMetadata.setAgentId(this.agentInformation.getAgentId());
            apiMetadata.setAgentStartTime(this.agentInformation.getStartTime());

            apiMetadata.setApiId(result.getId());
            apiMetadata.setApiInfo(methodDescriptor.getApiDescriptor());
            apiMetadata.setLine(methodDescriptor.getLineNumber());

            this.priorityDataSender.request(apiMetadata);
        }
        return result.getId();
    }

    @Override
    public int cacheString(final String value) {
        if (value == null) {
            return 0;
        }
        final Result result = this.stringCache.put(value);
        if(result.isNewValue()) {
            final TStringMetaData stringMetaData = new TStringMetaData();
            stringMetaData.setAgentId(this.agentInformation.getAgentId());
            stringMetaData.setAgentStartTime(this.agentInformation.getStartTime());

            stringMetaData.setStringId(result.getId());
            stringMetaData.setStringValue(value);
            this.priorityDataSender.request(stringMetaData);
        }
        return result.getId();
    }

    @Override
    public TraceId createTraceId(final String transactionId, final long parentSpanID, final long spanID, final short flags) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }
        // TODO parse error 때 예외 처리 필요.
        return DefaultTraceId.parse(transactionId, parentSpanID, spanID, flags);
    }


    @Override
    public ParsingResult parseSql(final String sql) {

        final ParsingResult parsingResult = this.sqlParser.normalizedSql(sql);
        final String normalizedSql = parsingResult.getSql();
        // 파싱시 변경되지 않았다면 동일 객체를 리턴하므로 그냥 ==비교를 하면 됨

        final boolean newValue = this.sqlCache.put(normalizedSql);
        if (newValue) {
            if (isDebug) {
                // TODO hit% 로그를 남겨야 문제 발생시 도움이 될듯 하다.
                logger.debug("NewSQLParsingResult:{}", parsingResult);
            }
            // newValue란 의미는 cache에 인입됬다는 의미이고 이는 신규 sql문일 가능성이 있다는 의미임.
            // 그러므로 메타데이터를 서버로 전송해야 한다.


            final TSqlMetaData sqlMetaData = new TSqlMetaData();
            sqlMetaData.setAgentId(this.agentInformation.getAgentId());
            sqlMetaData.setAgentStartTime(this.agentInformation.getStartTime());

            sqlMetaData.setHashCode(normalizedSql.hashCode());
            sqlMetaData.setSql(normalizedSql);

            // 좀더 신뢰성이 있는 tcp connection이 필요함.
            this.priorityDataSender.request(sqlMetaData);
        }
        // hashId그냥 return String에서 까보면 됨.
        return parsingResult;
    }

    @Override
    public DatabaseInfo parseJdbcUrl(final String url) {
        return this.jdbcUrlParser.parse(url);
    }

    public void setPriorityDataSender(final EnhancedDataSender priorityDataSender) {
        this.priorityDataSender = priorityDataSender;
    }


    public void setAgentInformation(final AgentInformation agentInformation) {
        if (agentInformation == null) {
            throw new NullPointerException("agentInformation must not be null");
        }
        this.agentInformation = agentInformation;
    }

}
