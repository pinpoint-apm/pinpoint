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

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceFactory;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceLocator;
import com.navercorp.pinpoint.profiler.context.storage.LogStorageFactory;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.metadata.LRUCache;
import com.navercorp.pinpoint.profiler.metadata.Result;
import com.navercorp.pinpoint.profiler.metadata.SimpleCache;
import com.navercorp.pinpoint.profiler.sampler.TrueSampler;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.util.RuntimeMXBeanUtils;
import com.navercorp.pinpoint.thrift.dto.TApiMetaData;
import com.navercorp.pinpoint.thrift.dto.TSqlMetaData;
import com.navercorp.pinpoint.thrift.dto.TStringMetaData;

/**
 * @author emeroad
 * @author HyunGil Jeong
 * @author Taejin Koo
 */
public class DefaultTraceContext implements TraceContext {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private static final boolean TRACE_ACTIVE_THREAD = true;

    private final TraceFactory traceFactory;

    private AgentInformation agentInformation;

    private EnhancedDataSender priorityDataSender;

    private final CachingSqlNormalizer cachingSqlNormalizer;

    private final SimpleCache<String> apiCache = new SimpleCache<String>();
    private final SimpleCache<String> stringCache = new SimpleCache<String>();

    private ProfilerConfig profilerConfig;

    private final ServerMetaDataHolder serverMetaDataHolder;

    private final AtomicInteger asyncId = new AtomicInteger();

    private final IdGenerator idGenerator = new IdGenerator();

    private final TransactionCounter transactionCounter = new DefaultTransactionCounter(this.idGenerator);

    // for test
    public DefaultTraceContext(final AgentInformation agentInformation) {
        this(LRUCache.DEFAULT_CACHE_SIZE, agentInformation, new LogStorageFactory(), new TrueSampler(), new DefaultServerMetaDataHolder(RuntimeMXBeanUtils.getVmArgs()), TRACE_ACTIVE_THREAD);
    }

    public DefaultTraceContext(final int sqlCacheSize, final AgentInformation agentInformation, StorageFactory storageFactory, Sampler sampler, ServerMetaDataHolder serverMetaDataHolder, final boolean traceActiveThread) {
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

        this.cachingSqlNormalizer = new DefaultCachingSqlNormalizer(sqlCacheSize);

        this.traceFactory = createTraceFactory(storageFactory, sampler, traceActiveThread);

        this.serverMetaDataHolder = serverMetaDataHolder;
    }

    private TraceFactory createTraceFactory(StorageFactory storageFactory, Sampler sampler, boolean recordActiveThread) {
        // TODO extract chain TraceFactory??
        final TraceFactory threadLocalTraceFactory = new ThreadLocalTraceFactory(this, storageFactory, sampler, this.idGenerator);
        if (recordActiveThread) {
            ActiveTraceFactory activeTraceFactory = (ActiveTraceFactory) ActiveTraceFactory.wrap(threadLocalTraceFactory);
            return activeTraceFactory;
        } else {
            return threadLocalTraceFactory;
        }
    }

    /**
     * Return trace only if current transaction can be sampled.
     * 
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
     * 
     * @return
     */
    @Override
    public Trace currentRawTraceObject() {
        return traceFactory.currentRawTraceObject();
    }

    @Override
    public Trace disableSampling() {
        // return null; is bug. #93
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

    @Override
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

    @Override
    public Trace newTraceObject() {
        return traceFactory.newTraceObject();
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
        // lazy sql normalization
        return this.cachingSqlNormalizer.wrapSql(sql);
    }

    @Override
    public boolean cacheSql(ParsingResult parsingResult) {
        if (parsingResult == null) {
            return false;
        }
        // lazy sql parsing
        boolean isNewValue = this.cachingSqlNormalizer.normalizedSql(parsingResult);
        if (isNewValue) {
            if (isDebug) {
                // TODO logging hit ratio could help debugging
                logger.debug("NewSQLParsingResult:{}", parsingResult);
            }

            // isNewValue means that the value is newly cached.
            // So the sql could be new one. We have to send sql metadata to collector.
            final TSqlMetaData sqlMetaData = new TSqlMetaData();
            sqlMetaData.setAgentId(getAgentId());
            sqlMetaData.setAgentStartTime(getAgentStartTime());

            sqlMetaData.setSqlId(parsingResult.getId());
            sqlMetaData.setSql(parsingResult.getSql());

            this.priorityDataSender.request(sqlMetaData);
        }
        return isNewValue;
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

    public ActiveTraceLocator getActiveTraceLocator() {
        if (traceFactory instanceof ActiveTraceFactory) {
            return (ActiveTraceLocator) ((ActiveTraceFactory) traceFactory).getActiveTraceLocator();
        } else {
            return null;
        }
    }

    public TransactionCounter getTransactionCounter() {
        return this.transactionCounter;
    }

}
