package com.nhn.pinpoint.profiler.context;


import com.nhn.pinpoint.bootstrap.context.*;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.AgentInformation;
import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.context.storage.LogStorageFactory;
import com.nhn.pinpoint.profiler.metadata.SimpleCache;
import com.nhn.pinpoint.profiler.modifier.db.DefaultDatabaseInfo;
import com.nhn.pinpoint.profiler.monitor.metric.MetricRegistry;
import com.nhn.pinpoint.profiler.sampler.TrueSampler;
import com.nhn.pinpoint.profiler.sender.EnhancedDataSender;
import com.nhn.pinpoint.thrift.dto.TApiMetaData;
import com.nhn.pinpoint.thrift.dto.TSqlMetaData;
import com.nhn.pinpoint.common.util.ParsingResult;
import com.nhn.pinpoint.common.util.SqlParser;
import com.nhn.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.metadata.LRUCache;
import com.nhn.pinpoint.profiler.metadata.Result;
import com.nhn.pinpoint.profiler.modifier.db.JDBCUrlParser;
import com.nhn.pinpoint.bootstrap.sampler.Sampler;
import com.nhn.pinpoint.thrift.dto.TStringMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class DefaultTraceContext implements TraceContext {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceFactory traceFactory;

    private final ActiveThreadCounter activeThreadCounter = new ActiveThreadCounter();


//    private GlobalCallTrace globalCallTrace = new GlobalCallTrace();

    private AgentInformation agentInformation;

    private EnhancedDataSender priorityDataSender;

    private final ServiceType contextServiceType;

    private final MetricRegistry metricRegistry;

    private final LRUCache<String> sqlCache;
    private final SqlParser sqlParser = new SqlParser();

    private final SimpleCache<String> apiCache = new SimpleCache<String>();
    private final SimpleCache<String> stringCache = new SimpleCache<String>();

    private final JDBCUrlParser jdbcUrlParser = new JDBCUrlParser();

    private ProfilerConfig profilerConfig;

    // for test
    public DefaultTraceContext() {
        this(LRUCache.DEFAULT_CACHE_SIZE, ServiceType.STAND_ALONE.getCode(), new LogStorageFactory(), new TrueSampler());
    }

    public DefaultTraceContext(final int sqlCacheSize, final short contextServiceType, StorageFactory storageFactory, Sampler sampler) {
        if (storageFactory == null) {
            throw new NullPointerException("storageFactory must not be null");
        }
        if (sampler == null) {
            throw new NullPointerException("sampler must not be null");
        }
        this.sqlCache = new LRUCache<String>(sqlCacheSize);
        this.contextServiceType = ServiceType.findServiceType(contextServiceType);
        this.metricRegistry = new MetricRegistry(this.contextServiceType);

        this.traceFactory = new ThreadLocalTraceFactory(this, metricRegistry, storageFactory, sampler);
    }

    /**
     * sampling 여부까지 체크하여 유효성을 검증한 후 Trace를 리턴한다.
     * @return
     */
    public Trace currentTraceObject() {
        return traceFactory.currentTraceObject();
    }

    public Trace currentRpcTraceObject() {
        return traceFactory.currentTraceObject();
    }

    /**
     * 유효성을 검증하지 않고 Trace를 리턴한다.
     * @return
     */
    @Override
    public Trace currentRawTraceObject() {
        return traceFactory.currentRawTraceObject();
    }

    @Override
    public Trace disableSampling() {
        return traceFactory.disableSampling();
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
        return traceFactory.continueTraceObject(traceID);
    }

    public Trace newTraceObject() {
        return traceFactory.newTraceObject();
    }


    @Override
    public void detachTraceObject() {
        this.traceFactory.detachTraceObject();
    }


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


    @Override
    public int cacheApi(final MethodDescriptor methodDescriptor) {
        final String fullName = methodDescriptor.getFullName();
        final Result result = this.apiCache.put(fullName);
        if (result.isNewValue()) {
            methodDescriptor.setApiId(result.getId());

            final TApiMetaData apiMetadata = new TApiMetaData();
            apiMetadata.setAgentId(getAgentId());
            apiMetadata.setAgentStartTime(getAgentStartTime());

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
            stringMetaData.setAgentId(getAgentId());
            stringMetaData.setAgentStartTime(getAgentStartTime());

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
            sqlMetaData.setAgentId(getAgentId());
            sqlMetaData.setAgentStartTime(getAgentStartTime());

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

    @Override
    public DatabaseInfo createDatabaseInfo(ServiceType type, ServiceType executeQueryType, String url, int port, String databaseId) {
        List<String> host = new ArrayList<String>();
        host.add(url + ":" + port);
        DatabaseInfo databaseInfo = new DefaultDatabaseInfo(type, executeQueryType, url, url, host, databaseId);
        return databaseInfo;
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

    @Override
    public Metric getRpcMetric(ServiceType serviceType) {
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }

        return this.metricRegistry.getRpcMetric(serviceType);
    }

    @Override
    public Metric getContextMetric() {
        return this.metricRegistry.getResponseMetric();
    }
}
