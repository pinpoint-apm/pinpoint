/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.config;

import com.navercorp.pinpoint.common.annotations.InterfaceAudience;
import com.navercorp.pinpoint.common.annotations.VisibleForTesting;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface ProfilerConfig {

    String getActiveProfile();

//    String[] getOptionalProfiles();

    int getInterceptorRegistrySize();

    TransportModule getTransportModule();

    ThriftTransportConfig getThriftTransportConfig();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    String getCollectorSpanServerIp();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    int getCollectorSpanServerPort();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    String getCollectorStatServerIp();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    int getCollectorStatServerPort();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    String getCollectorTcpServerIp();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    int getCollectorTcpServerPort();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    int getStatDataSenderWriteQueueSize();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    int getStatDataSenderSocketSendBufferSize();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    int getStatDataSenderSocketTimeout();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    String getStatDataSenderSocketType();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    String getStatDataSenderTransportType();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    int getSpanDataSenderWriteQueueSize();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    int getSpanDataSenderSocketSendBufferSize();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    boolean isTcpDataSenderCommandAcceptEnable();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    boolean isTcpDataSenderCommandActiveThreadEnable();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    boolean isTcpDataSenderCommandActiveThreadCountEnable();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    boolean isTcpDataSenderCommandActiveThreadDumpEnable();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    boolean isTcpDataSenderCommandActiveThreadLightDumpEnable();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    long getTcpDataSenderPinpointClientWriteTimeout();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    long getTcpDataSenderPinpointClientRequestTimeout();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    long getTcpDataSenderPinpointClientReconnectInterval();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    long getTcpDataSenderPinpointClientPingInterval();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    long getTcpDataSenderPinpointClientHandshakeInterval();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    int getSpanDataSenderSocketTimeout();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    String getSpanDataSenderSocketType();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    String getSpanDataSenderTransportType();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    int getSpanDataSenderChunkSize();

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} (int)} instead.
     */
    @Deprecated
    int getStatDataSenderChunkSize();


    boolean isTraceAgentActiveThread();

    boolean isTraceAgentDataSource();

    int getDataSourceTraceLimitSize();

    boolean isDeadlockMonitorEnable();

    long getDeadlockMonitorInterval();


    boolean isProfileEnable();

    int getJdbcSqlCacheSize();

    boolean isTraceSqlBindValue();

    int getMaxSqlBindValueSize();

    boolean isSamplingEnable();

    int getSamplingRate();

    int getSamplingNewThroughput();

    int getSamplingContinueThroughput();

    boolean isIoBufferingEnable();

    int getIoBufferingBufferSize();

    String getProfilerJvmVendorName();

    String getProfilerOSName();

    int getProfileJvmStatCollectIntervalMs();

    int getProfileJvmStatBatchSendCount();

    boolean isProfilerJvmStatCollectDetailedMetrics();

    long getAgentInfoSendRetryInterval();

    @InterfaceAudience.Private
    @VisibleForTesting
    boolean getStaticResourceCleanup();


    Filter<String> getProfilableClassFilter();

    List<String> getApplicationTypeDetectOrder();

    List<String> getPluginLoadOrder();

    List<String> getDisabledPlugins();

    String getApplicationServerType();

    int getCallStackMaxDepth();

    boolean isPropagateInterceptorException();

    String getProfileInstrumentEngine();

    boolean isSupportLambdaExpressions();

    boolean isInstrumentMatcherEnable();

    InstrumentMatcherCacheConfig getInstrumentMatcherCacheConfig();

    boolean isProxyHttpHeaderEnable();

    HttpStatusCodeErrors getHttpStatusCodeErrors();

    String getInjectionModuleFactoryClazzName();

    String getApplicationNamespace();

    String readString(String propertyName, String defaultValue);

    String readString(String propertyName, String defaultValue, ValueResolver valueResolver);

    int readInt(String propertyName, int defaultValue);

    DumpType readDumpType(String propertyName, DumpType defaultDump);

    long readLong(String propertyName, long defaultValue);

    List<String> readList(String propertyName);

    boolean readBoolean(String propertyName, boolean defaultValue);

    Map<String, String> readPattern(String propertyNamePatternRegex);

    interface ValueResolver {
        String resolve(String value, Properties properties);
    }

}
