/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.Metric;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.context.TraceType;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.DefaultParsingResult;
import com.navercorp.pinpoint.common.util.ParsingResult;
import com.navercorp.pinpoint.common.util.SqlParser;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.context.storage.LogStorageFactory;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.metadata.LRUCache;
import com.navercorp.pinpoint.profiler.metadata.Result;
import com.navercorp.pinpoint.profiler.metadata.SimpleCache;
import com.navercorp.pinpoint.profiler.modifier.db.DefaultDatabaseInfo;
import com.navercorp.pinpoint.profiler.modifier.db.JDBCUrlParser;
import com.navercorp.pinpoint.profiler.sampler.TrueSampler;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.util.RuntimeMXBeanUtils;
import com.navercorp.pinpoint.thrift.dto.TApiMetaData;
import com.navercorp.pinpoint.thrift.dto.TSqlMetaData;
import com.navercorp.pinpoint.thrift.dto.TStringMetaData;

/**
 * @author emeroad
 * @author hyungil.jeong
 * @author Taejin Koo
 */
public class DefaultTraceContext implements TraceContext {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceFactory traceFactory;

    private AgentInformation agentInformation;

    private EnhancedDataSender priorityDataSender;

    private final SimpleCache<String> sqlCache;
    private final SqlParser sqlParser = new SqlParser();

    private final SimpleCache<String> apiCache = new SimpleCache<String>();
    private final SimpleCache<String> stringCache = new SimpleCache<String>();

    private final JDBCUrlParser jdbcUrlParser = new JDBCUrlParser();

    private ProfilerConfig profilerConfig;
    
    private final ServerMetaDataHolder serverMetaDataHolder;
    
    private final AtomicInteger asyncId = new AtomicInteger();
    
    // for test
    public DefaultTraceContext(final AgentInformation agentInformation) {
        this(LRUCache.DEFAULT_CACHE_SIZE, agentInformation, new LogStorageFactory(), new TrueSampler(), new DefaultServerMetaDataHolder(RuntimeMXBeanUtils.getVmArgs()));
    }

    public DefaultTraceContext(final int sqlCacheSize, final AgentInformation agentInformation, StorageFactory storageFactory, Sampler sampler, ServerMetaDataHolder serverMetaDataHolder) {
        if (agentInformation == null) {
            throw new NullPointerException("agentInformation must not be null");
        }
        if (storageFactory == null) {
            throw new NullPointerException("storageFactory must not be null");
        }
        if (sampler == null) {
            throw new NullPointerException("sampler must not be null");
        }
        this.agentInformation = agentInformation;
        this.sqlCache = new SimpleCache<String>(sqlCacheSize);

        this.traceFactory = createTraceFactory(storageFactory, sampler);;
        
        this.serverMetaDataHolder = serverMetaDataHolder;
    }

    private TraceFactory createTraceFactory(StorageFactory storageFactory, Sampler sampler) {

        // TODO extract chain TraceFactory??
        final TraceFactory threadLocalTraceFactory = new ThreadLocalTraceFactory(this, storageFactory, sampler);
//        TODO
//        TraceFactory metricTraceFactory =  MetricTraceFactory.wrap(threadLocalTraceFactory, this.agentInformation.getServerType());
//        final TraceFactory activeTraceFactory = ActiveTraceFactory.wrap(metircTraceFactory);

//        TODO disable option
        return ActiveTraceFactory.wrap(threadLocalTraceFactory);
    }

    /**
     * Return trace only if current transaction can be sampled.
     * @return
     */
    public Trace currentTraceObject() {
        return traceFactory.currentTraceObject();
    }

    public Trace currentRpcTraceObject() {
        return traceFactory.currentTraceObject();
    }

    /**
     * Return trace without sampling check.
     * @return
     */
    @Override
    public Trace currentRawTraceObject() {
        return traceFactory.currentRawTraceObject();
    }

    @Override
    public Trace disableSampling() {
        // return null; is bug.  #93
        return traceFactory.disableSampling();
    }

    public void setProfilerConfig(final ProfilerConfig profilerConfig) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        this.profilerConfig = profilerConfig;
    }

    @Override
    public ProfilerConfig getProfilerConfig() {
        return profilerConfig;
    }

    // Will be invoked when current transaction is picked as sampling target at remote.
    public Trace continueTraceObject(final TraceId traceID) {
        return traceFactory.continueTraceObject(traceID);
    }

    @Override
    public Trace continueTraceObject(Trace trace) {
        return traceFactory.continueTraceObject(trace);
    }
    
    @Override
    public Trace continueAsyncTraceObject(AsyncTraceId traceId, int asyncId, long startTime) {
        return traceFactory.continueAsyncTraceObject(traceId, asyncId, startTime);
    }
    
    public Trace newTraceObject() {
        return traceFactory.newTraceObject();
    }

    public Trace newTraceObject(TraceType traceType) {
        return traceFactory.newTraceObject(traceType);
    }

    @Override
    public Trace removeTraceObject() {
        return traceFactory.removeTraceObject();
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
        return this.agentInformation.getServerType().getCode();
    }

    @Override
    public String getServerType() {
        return this.agentInformation.getServerType().getDesc();
    }


    @Override
    public int cacheApi(final MethodDescriptor methodDescriptor) {
        final String fullName = methodDescriptor.getFullName();
        final Result result = this.apiCache.put(fullName);
        
        methodDescriptor.setApiId(result.getId());

        if (result.isNewValue()) {
            final TApiMetaData apiMetadata = new TApiMetaData();
            apiMetadata.setAgentId(getAgentId());
            apiMetadata.setAgentStartTime(getAgentStartTime());

            apiMetadata.setApiId(result.getId());
            apiMetadata.setApiInfo(methodDescriptor.getApiDescriptor());
            apiMetadata.setLine(methodDescriptor.getLineNumber());
            apiMetadata.setType(methodDescriptor.getType());

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
        if (result.isNewValue()) {
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
        // TODO Should handle exception when parsing failed.
        return DefaultTraceId.parse(transactionId, parentSpanID, spanID, flags);
    }


    @Override
    public ParsingResult parseSql(final String sql) {

        final DefaultParsingResult parsingResult = this.sqlParser.normalizedSql(sql);
        return parsingResult;
    }

    @Override
    public boolean cacheSql(ParsingResult parsingResult) {
        if (parsingResult == null) {
            return false;
        }
        if (parsingResult.getId() != ParsingResult.ID_NOT_EXIST) {
            // already cached
            return false;
        }
        final String normalizedSql = parsingResult.getSql();

        final Result cachingResult = this.sqlCache.put(normalizedSql);
        if (cachingResult.isNewValue()) {
            if (isDebug) {
                // TODO logging hit ratio could help debugging
                logger.debug("NewSQLParsingResult:{}", parsingResult);
            }

            // isNewValue means that the value is newly cached.
            // So the sql could be new one. We have to send sql metadata to collector.
            final TSqlMetaData sqlMetaData = new TSqlMetaData();
            sqlMetaData.setAgentId(getAgentId());
            sqlMetaData.setAgentStartTime(getAgentStartTime());

            sqlMetaData.setSqlId(cachingResult.getId());
            sqlMetaData.setSql(normalizedSql);

            this.priorityDataSender.request(sqlMetaData);
        }
        return parsingResult.setId(cachingResult.getId());
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


    @Override
    public ServerMetaDataHolder getServerMetaDataHolder() {
        return this.serverMetaDataHolder;
    }


    @Override
    public int getAsyncId() {
        final int id = asyncId.incrementAndGet();
        return id == -1 ? asyncId.incrementAndGet() : id;
    }

}
