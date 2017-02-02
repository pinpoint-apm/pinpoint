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

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.PluginMonitorContext;
import com.navercorp.pinpoint.common.annotations.InterfaceAudience;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.metadata.Result;
import com.navercorp.pinpoint.profiler.metadata.SimpleCache;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.thrift.dto.TApiMetaData;
import com.navercorp.pinpoint.thrift.dto.TSqlMetaData;
import com.navercorp.pinpoint.thrift.dto.TStringMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 * @author HyunGil Jeong
 * @author Taejin Koo
 */
public class DefaultTraceContext implements TraceContext {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceFactory traceFactory;

    private final AgentInformation agentInformation;

    private EnhancedDataSender priorityDataSender;

    private final CachingSqlNormalizer cachingSqlNormalizer;

    private final SimpleCache<String> apiCache = new SimpleCache<String>();
    private final SimpleCache<String> stringCache = new SimpleCache<String>();

    private final ProfilerConfig profilerConfig;

    private final ServerMetaDataHolder serverMetaDataHolder;

    private final PluginMonitorContext pluginMonitorContext;

    private final AsyncIdGenerator asyncIdGenerator = new AsyncIdGenerator();

    public DefaultTraceContext(ProfilerConfig profilerConfig, IdGenerator idGenerator, final AgentInformation agentInformation, TraceFactoryBuilder traceFactoryBuilder,
                               PluginMonitorContext pluginMonitorContext, ServerMetaDataHolder serverMetaDataHolder) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (agentInformation == null) {
            throw new NullPointerException("agentInformation must not be null");
        }
        if (idGenerator == null) {
            throw new NullPointerException("idGenerator must not be null");
        }
        if (traceFactoryBuilder == null) {
            throw new NullPointerException("traceFactoryBuilder must not be null");
        }

        this.profilerConfig = profilerConfig;
        this.agentInformation = agentInformation;
        this.serverMetaDataHolder = serverMetaDataHolder;

        this.cachingSqlNormalizer = createSqlNormalizer(profilerConfig);

        this.traceFactory = traceFactoryBuilder.build(this);
        this.pluginMonitorContext = pluginMonitorContext;
    }

    private CachingSqlNormalizer createSqlNormalizer(ProfilerConfig profilerConfig) {
        final int jdbcSqlCacheSize = profilerConfig.getJdbcSqlCacheSize();
        return new DefaultCachingSqlNormalizer(jdbcSqlCacheSize);
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


    @Override
    public ProfilerConfig getProfilerConfig() {
        return profilerConfig;
    }

    @Override
    public Trace continueTraceObject(final TraceId traceId) {
        return traceFactory.continueTraceObject(traceId);
    }


    @Override
    public Trace continueTraceObject(Trace trace) {
        return traceFactory.continueTraceObject(trace);
    }


    @Override
    public Trace newTraceObject() {
        return traceFactory.newTraceObject();
    }

    @InterfaceAudience.LimitedPrivate("vert.x")
    @Override
    public Trace newAsyncTraceObject() {
        return traceFactory.newAsyncTraceObject();
    }

    @InterfaceAudience.LimitedPrivate("vert.x")
    @Override
    public Trace continueAsyncTraceObject(final TraceId traceId) {
        return traceFactory.continueAsyncTraceObject(traceId);
    }

    @Override
    public Trace continueAsyncTraceObject(AsyncTraceId traceId, int asyncId, long startTime) {
        return traceFactory.continueAsyncTraceObject(traceId, asyncId, startTime);
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
    public TraceId createTraceId(final String transactionId, final long parentSpanID, final long spanId, final short flags) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }
        // TODO Should handle exception when parsing failed.
        return DefaultTraceId.parse(transactionId, parentSpanID, spanId, flags);
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

    @Deprecated
    public void setPriorityDataSender(final EnhancedDataSender priorityDataSender) {
        this.priorityDataSender = priorityDataSender;
    }

    @Override
    public ServerMetaDataHolder getServerMetaDataHolder() {
        return this.serverMetaDataHolder;
    }

    @Override
    public int getAsyncId() {
        return this.asyncIdGenerator.nextAsyncId();
    }

    @Override
    public PluginMonitorContext getPluginMonitorContext() {
        return pluginMonitorContext;
    }

}
