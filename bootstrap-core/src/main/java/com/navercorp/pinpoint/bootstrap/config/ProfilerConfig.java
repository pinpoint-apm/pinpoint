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

package com.navercorp.pinpoint.bootstrap.config;

import java.util.List;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface ProfilerConfig {
    int getInterceptorRegistrySize();

    String getCollectorSpanServerIp();

    int getCollectorSpanServerPort();

    String getCollectorStatServerIp();

    int getCollectorStatServerPort();

    String getCollectorTcpServerIp();

    int getCollectorTcpServerPort();

    int getStatDataSenderWriteQueueSize();

    int getStatDataSenderSocketSendBufferSize();

    int getStatDataSenderSocketTimeout();

    int getSpanDataSenderWriteQueueSize();

    int getSpanDataSenderSocketSendBufferSize();

    boolean isTcpDataSenderCommandAcceptEnable();

    boolean isTraceAgentActiveThread();

    int getSpanDataSenderSocketTimeout();

    int getSpanDataSenderChunkSize();

    int getStatDataSenderChunkSize();

    boolean isProfileEnable();

    int getJdbcSqlCacheSize();

    boolean isTraceSqlBindValue();

    int getMaxSqlBindValueSize();

    boolean isSamplingEnable();

    int getSamplingRate();

    boolean isIoBufferingEnable();

    int getIoBufferingBufferSize();

    int getProfileJvmCollectInterval();

    boolean isProfilerJvmCollectDetailedMetrics();

    void setProfilerJvmCollectDetailedMetrics(boolean profilerJvmCollectDetailedMetrics);

    long getAgentInfoSendRetryInterval();

    boolean isTomcatHidePinpointHeader();

    boolean isTomcatTraceRequestParam();

    Filter<String> getTomcatExcludeUrlFilter();

    String getTomcatRealIpHeader();

    String getTomcatRealIpEmptyValue();

    Filter<String> getTomcatExcludeProfileMethodFilter();

    boolean isApacheHttpClient3Profile();

    boolean isApacheHttpClient3ProfileCookie();

    DumpType getApacheHttpClient3ProfileCookieDumpType();

    int getApacheHttpClient3ProfileCookieSamplingRate();

    boolean isApacheHttpClient3ProfileEntity();

    DumpType getApacheHttpClient3ProfileEntityDumpType();

    int getApacheHttpClient3ProfileEntitySamplingRate();

    boolean isApacheHttpClient3ProfileIo();

    //-----------------------------------------
    // http apache client 4
    boolean isApacheHttpClient4Profile();

    boolean isApacheHttpClient4ProfileCookie();

    DumpType getApacheHttpClient4ProfileCookieDumpType();

    int getApacheHttpClient4ProfileCookieSamplingRate();

    boolean isApacheHttpClient4ProfileEntity();

    DumpType getApacheHttpClient4ProfileEntityDumpType();

    int getApacheHttpClient4ProfileEntitySamplingRate();

    boolean isApacheHttpClient4ProfileStatusCode();

    boolean isApacheHttpClient4ProfileIo();

    //-----------------------------------------
    // org/apache/http/impl/nio/*
    boolean getApacheNIOHttpClient4Profile();

    boolean isIBatisEnabled();

    boolean isMyBatisEnabled();

    boolean isRedisEnabled();

    boolean isRedisPipelineEnabled();

    Filter<String> getProfilableClassFilter();

    List<String> getApplicationTypeDetectOrder();

    List<String> getDisabledPlugins();

    void setDisabledPlugins(List<String> disabledPlugins);

    String getApplicationServerType();

    void setApplicationServerType(String applicationServerType);

    boolean isLog4jLoggingTransactionInfo();

    boolean isLogbackLoggingTransactionInfo();

    int getCallStackMaxDepth();

    void setCallStackMaxDepth(int callStackMaxDepth);

    boolean isPropagateInterceptorException();

    String readString(String propertyName, String defaultValue);

    int readInt(String propertyName, int defaultValue);

    DumpType readDumpType(String propertyName, DumpType defaultDump);

    long readLong(String propertyName, long defaultValue);

    List<String> readList(String propertyName);

    boolean readBoolean(String propertyName, boolean defaultValue);

    Map<String, String> readPattern(String propertyNamePatternRegex);
}
