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

import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.bootstrap.util.spring.PropertyPlaceholderHelper;
import com.navercorp.pinpoint.common.util.logger.CommonLogger;
import com.navercorp.pinpoint.common.util.PropertyUtils;
import com.navercorp.pinpoint.common.util.logger.StdoutCommonLoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * @author emeroad
 * @author netspider
 */
public class DefaultProfilerConfig implements ProfilerConfig {
    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE.getLogger(DefaultProfilerConfig.class.getName());
    private static final String DEFAULT_IP = "127.0.0.1";

    private final Properties properties;
    private final PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper("${", "}");

    @Deprecated
    public static final String INSTRUMENT_ENGINE_JAVASSIST = "JAVASSIST";
    public static final String INSTRUMENT_ENGINE_ASM = "ASM";

    public static final int DEFAULT_AGENT_STAT_COLLECTION_INTERVAL_MS = 5 * 1000;
    public static final int DEFAULT_NUM_AGENT_STAT_BATCH_SEND = 6;

    public interface ValueResolver {
        String resolve(String value, Properties properties);
    }

    private static class BypassResolver implements ValueResolver {
        public static final ValueResolver RESOLVER = new BypassResolver();

        @Override
        public String resolve(String value, Properties properties) {
            return value;
        }
    }

    private class PlaceHolderResolver implements ValueResolver {
        @Override
        public String resolve(String value, Properties properties) {
            if (value == null) {
                return null;
            }
            return propertyPlaceholderHelper.replacePlaceholders(value, properties);
        }
    }

    public static ProfilerConfig load(String pinpointConfigFileName) throws IOException {
        try {
            Properties properties = PropertyUtils.loadProperty(pinpointConfigFileName);
            return new DefaultProfilerConfig(properties);
        } catch (FileNotFoundException fe) {
            if (logger.isWarnEnabled()) {
                logger.warn(pinpointConfigFileName + " file does not exist. Please check your configuration.");
            }
            throw fe;
        } catch (IOException e) {
            if (logger.isWarnEnabled()) {
                logger.warn(pinpointConfigFileName + " file I/O error. Error:" + e.getMessage(), e);
            }
            throw e;
        }
    }

    private boolean profileEnable = false;

    private String profileInstrumentEngine = INSTRUMENT_ENGINE_ASM;
    private boolean instrumentMatcherEnable = true;
    private InstrumentMatcherCacheConfig instrumentMatcherCacheConfig = new InstrumentMatcherCacheConfig();

    private int interceptorRegistrySize = 1024*8;

    private String collectorSpanServerIp = DEFAULT_IP;
    private int collectorSpanServerPort = 9996;

    private String collectorStatServerIp = DEFAULT_IP;
    private int collectorStatServerPort = 9995;

    private String collectorTcpServerIp = DEFAULT_IP;
    private int collectorTcpServerPort = 9994;

    private int spanDataSenderWriteQueueSize = 1024 * 5;
    private int spanDataSenderSocketSendBufferSize = 1024 * 64 * 16;
    private int spanDataSenderSocketTimeout = 1000 * 3;
    private int spanDataSenderChunkSize = 1024 * 16;
    private String spanDataSenderSocketType = "OIO";

    private int statDataSenderWriteQueueSize = 1024 * 5;
    private int statDataSenderSocketSendBufferSize = 1024 * 64 * 16;
    private int statDataSenderSocketTimeout = 1000 * 3;
    private int statDataSenderChunkSize = 1024 * 16;
    private String statDataSenderSocketType = "OIO";

    private boolean tcpDataSenderCommandAcceptEnable = false;
    private boolean tcpDataSenderCommandActiveThreadEnable = false;
    private boolean tcpDataSenderCommandActiveThreadCountEnable = false;
    private boolean tcpDataSenderCommandActiveThreadDumpEnable = false;
    private boolean tcpDataSenderCommandActiveThreadLightDumpEnable = false;

    private boolean traceAgentActiveThread = true;

    private boolean traceAgentDataSource = false;
    private int dataSourceTraceLimitSize = 20;

    private boolean deadlockMonitorEnable = true;
    private long deadlockMonitorInterval = 60000L;

    private int callStackMaxDepth = 512;

    private int jdbcSqlCacheSize = 1024;
    private boolean traceSqlBindValue = false;
    private int maxSqlBindValueSize = 1024;

    // Sampling
    private boolean samplingEnable = true;
    private int samplingRate = 1;

    // span buffering
    private boolean ioBufferingEnable;
    private int ioBufferingBufferSize;

    private String profileJvmVendorName;
    private int profileJvmStatCollectIntervalMs = DEFAULT_AGENT_STAT_COLLECTION_INTERVAL_MS;
    private int profileJvmStatBatchSendCount = DEFAULT_NUM_AGENT_STAT_BATCH_SEND;
    private boolean profilerJvmStatCollectDetailedMetrics;

    private Filter<String> profilableClassFilter = new SkipFilter<String>();

    private final long DEFAULT_AGENT_INFO_SEND_RETRY_INTERVAL = 5 * 60 * 1000L;
    private long agentInfoSendRetryInterval = DEFAULT_AGENT_INFO_SEND_RETRY_INTERVAL;

    private String applicationServerType;
    private List<String> applicationTypeDetectOrder = Collections.emptyList();
    private List<String> disabledPlugins = Collections.emptyList();

    private boolean propagateInterceptorException = false;
    private boolean supportLambdaExpressions = true;

    private boolean proxyHttpHeaderEnable = true;
    private List<String> proxyHttpHeaderNames = Collections.emptyList();
    private boolean proxyHttpHeaderHidden = true;

    public DefaultProfilerConfig() {
        this.properties = new Properties();
    }

    public DefaultProfilerConfig(Properties properties) {
        if (properties == null) {
            throw new NullPointerException("properties must not be null");
        }
        this.properties = properties;
        readPropertyValues();
    }

    @Override
    public int getInterceptorRegistrySize() {
        return interceptorRegistrySize;
    }

    @Override
    public String getCollectorSpanServerIp() {
        return collectorSpanServerIp;
    }

    @Override
    public int getCollectorSpanServerPort() {
        return collectorSpanServerPort;
    }

    @Override
    public String getCollectorStatServerIp() {
        return collectorStatServerIp;
    }

    @Override
    public int getCollectorStatServerPort() {
        return collectorStatServerPort;
    }

    @Override
    public String getCollectorTcpServerIp() {
        return collectorTcpServerIp;
    }

    @Override
    public int getCollectorTcpServerPort() {
        return collectorTcpServerPort;
    }

    @Override
    public int getStatDataSenderWriteQueueSize() {
        return statDataSenderWriteQueueSize;
    }

    @Override
    public int getStatDataSenderSocketSendBufferSize() {
        return statDataSenderSocketSendBufferSize;
    }

    @Override
    public int getStatDataSenderSocketTimeout() {
        return statDataSenderSocketTimeout;
    }

    @Override
    public String getStatDataSenderSocketType() {
        return statDataSenderSocketType;
    }

    @Override
    public int getSpanDataSenderWriteQueueSize() {
        return spanDataSenderWriteQueueSize;
    }

    @Override
    public int getSpanDataSenderSocketSendBufferSize() {
        return spanDataSenderSocketSendBufferSize;
    }

    @Override
    public boolean isTcpDataSenderCommandAcceptEnable() {
        return tcpDataSenderCommandAcceptEnable;
    }

    @Override
    public boolean isTcpDataSenderCommandActiveThreadEnable() {
        return tcpDataSenderCommandActiveThreadEnable;
    }

    @Override
    public boolean isTcpDataSenderCommandActiveThreadCountEnable() {
        return tcpDataSenderCommandActiveThreadCountEnable;
    }

    @Override
    public boolean isTcpDataSenderCommandActiveThreadDumpEnable() {
        return tcpDataSenderCommandActiveThreadDumpEnable;
    }

    @Override
    public boolean isTcpDataSenderCommandActiveThreadLightDumpEnable() {
        return tcpDataSenderCommandActiveThreadLightDumpEnable;
    }

    @Override
    public boolean isTraceAgentActiveThread() {
        return traceAgentActiveThread;
    }

    @Override
    public boolean isTraceAgentDataSource() {
        return traceAgentDataSource;
    }

    @Override
    public int getDataSourceTraceLimitSize() {
        return dataSourceTraceLimitSize;
    }

    @Override
    public boolean isDeadlockMonitorEnable() {
        return deadlockMonitorEnable;
    }

    @Override
    public long getDeadlockMonitorInterval() {
        return deadlockMonitorInterval;
    }

    @Override
    public int getSpanDataSenderSocketTimeout() {
        return spanDataSenderSocketTimeout;
    }

    @Override
    public String getSpanDataSenderSocketType() {
        return spanDataSenderSocketType;
    }

    @Override
    public int getSpanDataSenderChunkSize() {
        return spanDataSenderChunkSize;
    }

    @Override
    public int getStatDataSenderChunkSize() {
        return statDataSenderChunkSize;
    }

    @Override
    public boolean isProfileEnable() {
        return profileEnable;
    }

    @Override
    public int getJdbcSqlCacheSize() {
        return jdbcSqlCacheSize;
    }

    @Override
    public boolean isTraceSqlBindValue() {
        return traceSqlBindValue;
    }

    @Override
    public int getMaxSqlBindValueSize() {
        return maxSqlBindValueSize;
    }

    @Override
    public boolean isSamplingEnable() {
        return samplingEnable;
    }


    @Override
    public int getSamplingRate() {
        return samplingRate;
    }

    @Override
    public boolean isIoBufferingEnable() {
        return ioBufferingEnable;
    }

    @Override
    public int getIoBufferingBufferSize() {
        return ioBufferingBufferSize;
    }

    @Override
    public String getProfilerJvmVendorName() {
        return profileJvmVendorName;
    }

    @Override
    public int getProfileJvmStatCollectIntervalMs() {
        return profileJvmStatCollectIntervalMs;
    }

    @Override
    public int getProfileJvmStatBatchSendCount() {
        return profileJvmStatBatchSendCount;
    }

    @Override
    public boolean isProfilerJvmStatCollectDetailedMetrics() {
        return profilerJvmStatCollectDetailedMetrics;
    }

    @Override
    public long getAgentInfoSendRetryInterval() {
        return agentInfoSendRetryInterval;
    }


    @Override
    public Filter<String> getProfilableClassFilter() {
        return profilableClassFilter;
    }
    
    @Override
    public List<String> getApplicationTypeDetectOrder() {
        return applicationTypeDetectOrder;
    }
    
    @Override
    public List<String> getDisabledPlugins() {
        return disabledPlugins;
    }

    @Override
    public String getApplicationServerType() {
        return applicationServerType;
    }

    public void setApplicationServerType(String applicationServerType) {
        this.applicationServerType = applicationServerType;
    }

    @Override
    public int getCallStackMaxDepth() {
        return callStackMaxDepth;
    }

    public void setCallStackMaxDepth(int callStackMaxDepth) {
        this.callStackMaxDepth = callStackMaxDepth;
    }
    
    @Override
    public boolean isPropagateInterceptorException() {
        return propagateInterceptorException;
    }

    @Override
    public String getProfileInstrumentEngine() {
        return profileInstrumentEngine;
    }

    @Override
    public boolean isSupportLambdaExpressions() {
        return supportLambdaExpressions;
    }

    @Override
    public boolean isInstrumentMatcherEnable() {
        return instrumentMatcherEnable;
    }

    @Override
    public InstrumentMatcherCacheConfig getInstrumentMatcherCacheConfig() {
        return instrumentMatcherCacheConfig;
    }

    @Override
    public List<String> getProxyHttpHeaderNames() {
        return proxyHttpHeaderNames;
    }

    @Override
    public boolean isProxyHttpHeaderEnable() {
        return proxyHttpHeaderEnable;
    }

    @Override
    public boolean isProxyHttpHeaderHidden() {
        return proxyHttpHeaderHidden;
    }

    // for test
    void readPropertyValues() {
        // TODO : use Properties' default value instead of using a temp variable.
        final ValueResolver placeHolderResolver = new PlaceHolderResolver();

        this.profileEnable = readBoolean("profiler.enable", true);
        this.profileInstrumentEngine = readString("profiler.instrument.engine", INSTRUMENT_ENGINE_ASM);
        this.instrumentMatcherEnable = readBoolean("profiler.instrument.matcher.enable", true);

        this.instrumentMatcherCacheConfig.setInterfaceCacheSize(readInt("profiler.instrument.matcher.interface.cache.size", 4));
        this.instrumentMatcherCacheConfig.setInterfaceCacheEntrySize(readInt("profiler.instrument.matcher.interface.cache.entry.size", 16));
        this.instrumentMatcherCacheConfig.setAnnotationCacheSize(readInt("profiler.instrument.matcher.annotation.cache.size", 4));
        this.instrumentMatcherCacheConfig.setAnnotationCacheEntrySize(readInt("profiler.instrument.matcher.annotation.cache.entry.size", 4));
        this.instrumentMatcherCacheConfig.setSuperCacheSize(readInt("profiler.instrument.matcher.super.cache.size", 4));
        this.instrumentMatcherCacheConfig.setSuperCacheEntrySize(readInt("profiler.instrument.matcher.super.cache.entry.size", 4));

        this.interceptorRegistrySize = readInt("profiler.interceptorregistry.size", 1024*8);

        this.collectorSpanServerIp = readString("profiler.collector.span.ip", DEFAULT_IP, placeHolderResolver);
        this.collectorSpanServerPort = readInt("profiler.collector.span.port", 9996);

        this.collectorStatServerIp = readString("profiler.collector.stat.ip", DEFAULT_IP, placeHolderResolver);
        this.collectorStatServerPort = readInt("profiler.collector.stat.port", 9995);

        this.collectorTcpServerIp = readString("profiler.collector.tcp.ip", DEFAULT_IP, placeHolderResolver);
        this.collectorTcpServerPort = readInt("profiler.collector.tcp.port", 9994);

        this.spanDataSenderWriteQueueSize = readInt("profiler.spandatasender.write.queue.size", 1024 * 5);
        this.spanDataSenderSocketSendBufferSize = readInt("profiler.spandatasender.socket.sendbuffersize", 1024 * 64 * 16);
        this.spanDataSenderSocketTimeout = readInt("profiler.spandatasender.socket.timeout", 1000 * 3);
        this.spanDataSenderChunkSize = readInt("profiler.spandatasender.chunk.size", 1024 * 16);
        this.spanDataSenderSocketType = readString("profiler.spandatasender.socket.type", "OIO");

        this.statDataSenderWriteQueueSize = readInt("profiler.statdatasender.write.queue.size", 1024 * 5);
        this.statDataSenderSocketSendBufferSize = readInt("profiler.statdatasender.socket.sendbuffersize", 1024 * 64 * 16);
        this.statDataSenderSocketTimeout = readInt("profiler.statdatasender.socket.timeout", 1000 * 3);
        this.statDataSenderChunkSize = readInt("profiler.statdatasender.chunk.size", 1024 * 16);
        this.statDataSenderSocketType = readString("profiler.statdatasender.socket.type", "OIO");

        this.tcpDataSenderCommandAcceptEnable = readBoolean("profiler.tcpdatasender.command.accept.enable", false);
        this.tcpDataSenderCommandActiveThreadEnable = readBoolean("profiler.tcpdatasender.command.activethread.enable", false);
        this.tcpDataSenderCommandActiveThreadCountEnable = readBoolean("profiler.tcpdatasender.command.activethread.count.enable", false);
        this.tcpDataSenderCommandActiveThreadDumpEnable = readBoolean("profiler.tcpdatasender.command.activethread.threaddump.enable", false);
        this.tcpDataSenderCommandActiveThreadLightDumpEnable = readBoolean("profiler.tcpdatasender.command.activethread.threadlightdump.enable", false);

        this.traceAgentActiveThread = readBoolean("profiler.pinpoint.activethread", true);

        this.traceAgentDataSource = readBoolean("profiler.pinpoint.datasource", false);
        this.dataSourceTraceLimitSize = readInt("profiler.pinpoint.datasource.tracelimitsize", 20);

        this.deadlockMonitorEnable = readBoolean("profiler.monitor.deadlock.enable", true);
        this.deadlockMonitorInterval = readLong("profiler.monitor.deadlock.interval", 60000L);

        // CallStack
        this.callStackMaxDepth = readInt("profiler.callstack.max.depth", 64);
        if (this.callStackMaxDepth < 2) {
            this.callStackMaxDepth = 2;
        }
        
        // JDBC
        this.jdbcSqlCacheSize = readInt("profiler.jdbc.sqlcachesize", 1024);
        this.traceSqlBindValue = readBoolean("profiler.jdbc.tracesqlbindvalue", false);


        this.samplingEnable = readBoolean("profiler.sampling.enable", true);
        this.samplingRate = readInt("profiler.sampling.rate", 1);

        // configuration for sampling and IO buffer 
        this.ioBufferingEnable = readBoolean("profiler.io.buffering.enable", true);

        // it may be a problem to be here.  need to modify(delete or move or .. )  this configuration.
        this.ioBufferingBufferSize = readInt("profiler.io.buffering.buffersize", 20);

        // JVM
        this.profileJvmVendorName = readString("profiler.jvm.vendor.name", null);
        this.profileJvmStatCollectIntervalMs = readInt("profiler.jvm.stat.collect.interval", DEFAULT_AGENT_STAT_COLLECTION_INTERVAL_MS);
        this.profileJvmStatBatchSendCount = readInt("profiler.jvm.stat.batch.send.count", DEFAULT_NUM_AGENT_STAT_BATCH_SEND);
        this.profilerJvmStatCollectDetailedMetrics = readBoolean("profiler.stat.jvm.collect.detailed.metrics", false);

        this.agentInfoSendRetryInterval = readLong("profiler.agentInfo.send.retry.interval", DEFAULT_AGENT_INFO_SEND_RETRY_INTERVAL);

        // service type
        this.applicationServerType = readString("profiler.applicationservertype", null);

        // application type detector order
        this.applicationTypeDetectOrder = readList("profiler.type.detect.order");
        
        this.disabledPlugins = readList("profiler.plugin.disable");
        
        // TODO have to remove        
        // profile package included in order to test "call stack view".
        // this config must not be used in service environment because the size of  profiling information will get heavy.
        // We may need to change this configuration to regular expression.
        final String profilableClass = readString("profiler.include", "");
        if (!profilableClass.isEmpty()) {
            this.profilableClassFilter = new ProfilableClassFilter(profilableClass);
        }
        
        this.propagateInterceptorException = readBoolean("profiler.interceptor.exception.propagate", false);
        this.supportLambdaExpressions = readBoolean("profiler.lambda.expressions.support", true);

        // proxy http header names
        this.proxyHttpHeaderEnable = readBoolean("profiler.proxy.http.header.enable", true);
        this.proxyHttpHeaderNames = readList("profiler.proxy.http.header.names");
        this.proxyHttpHeaderHidden = readBoolean("profiler.proxy.http.header.hidden", true);

        logger.info("configuration loaded successfully.");
    }


    @Override
    public String readString(String propertyName, String defaultValue) {
        return readString(propertyName, defaultValue, BypassResolver.RESOLVER);
    }

    private String readString(String propertyName, String defaultValue, ValueResolver valueResolver) {
        if (valueResolver == null) {
            throw new NullPointerException("valueResolver must not be null");
        }
        String value = properties.getProperty(propertyName, defaultValue);
        value = valueResolver.resolve(value, properties);
        if (logger.isInfoEnabled()) {
            logger.info(propertyName + "=" + value);
        }
        return value;
    }

    @Override
    public int readInt(String propertyName, int defaultValue) {
        String value = properties.getProperty(propertyName);
        int result = NumberUtils.parseInteger(value, defaultValue);
        if (logger.isInfoEnabled()) {
            logger.info(propertyName + "=" + result);
        }
        return result;
    }

    @Override
    public DumpType readDumpType(String propertyName, DumpType defaultDump) {
        String propertyValue = properties.getProperty(propertyName);
        if (propertyValue == null) {
            propertyValue = defaultDump.name();
        }
        String value = propertyValue.toUpperCase();
        DumpType result;
        try {
            result = DumpType.valueOf(value);
        } catch (IllegalArgumentException e) {
            result = defaultDump;
        }
        if (logger.isInfoEnabled()) {
            logger.info(propertyName + "=" + result);
        }
        return result;
    }

    @Override
    public long readLong(String propertyName, long defaultValue) {
        String value = properties.getProperty(propertyName);
        long result = NumberUtils.parseLong(value, defaultValue);
        if (logger.isInfoEnabled()) {
            logger.info(propertyName + "=" + result);
        }
        return result;
    }

    @Override
    public List<String> readList(String propertyName) {
        String value = properties.getProperty(propertyName);
        if (value == null) {
            return Collections.emptyList();
        }
        String[] orders = value.trim().split(",");
        final List<String> list = new ArrayList<String>(orders.length);
        for (String order : orders) {
            final String trimmed = order.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }

    @Override
    public boolean readBoolean(String propertyName, boolean defaultValue) {
        String value = properties.getProperty(propertyName, Boolean.toString(defaultValue));
        boolean result = Boolean.parseBoolean(value);
        if (logger.isInfoEnabled()) {
            logger.info(propertyName + "=" + result);
        }
        return result;
    }

    @Override
    public Map<String, String> readPattern(String propertyNamePatternRegex) {
        final Pattern pattern = Pattern.compile(propertyNamePatternRegex);
        final Map<String, String> result = new HashMap<String, String>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                final String key = (String) entry.getKey();
                if (pattern.matcher(key).matches()) {
                    final String value = (String) entry.getValue();
                    result.put(key, value);
                }
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info(propertyNamePatternRegex + "=" + result);
        }

        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(1024);
        sb.append("{");
        sb.append("properties=").append(properties);
        sb.append(", propertyPlaceholderHelper=").append(propertyPlaceholderHelper);
        sb.append(", profileEnable=").append(profileEnable);
        sb.append(", profileInstrumentEngine='").append(profileInstrumentEngine).append('\'');
        sb.append(", instrumentMatcherEnable=").append(instrumentMatcherEnable);
        sb.append(", instrumentMatcherCacheConfig=").append(instrumentMatcherCacheConfig);
        sb.append(", interceptorRegistrySize=").append(interceptorRegistrySize);
        sb.append(", collectorSpanServerIp='").append(collectorSpanServerIp).append('\'');
        sb.append(", collectorSpanServerPort=").append(collectorSpanServerPort);
        sb.append(", collectorStatServerIp='").append(collectorStatServerIp).append('\'');
        sb.append(", collectorStatServerPort=").append(collectorStatServerPort);
        sb.append(", collectorTcpServerIp='").append(collectorTcpServerIp).append('\'');
        sb.append(", collectorTcpServerPort=").append(collectorTcpServerPort);
        sb.append(", spanDataSenderWriteQueueSize=").append(spanDataSenderWriteQueueSize);
        sb.append(", spanDataSenderSocketSendBufferSize=").append(spanDataSenderSocketSendBufferSize);
        sb.append(", spanDataSenderSocketTimeout=").append(spanDataSenderSocketTimeout);
        sb.append(", spanDataSenderChunkSize=").append(spanDataSenderChunkSize);
        sb.append(", spanDataSenderSocketType='").append(spanDataSenderSocketType).append('\'');
        sb.append(", statDataSenderWriteQueueSize=").append(statDataSenderWriteQueueSize);
        sb.append(", statDataSenderSocketSendBufferSize=").append(statDataSenderSocketSendBufferSize);
        sb.append(", statDataSenderSocketTimeout=").append(statDataSenderSocketTimeout);
        sb.append(", statDataSenderChunkSize=").append(statDataSenderChunkSize);
        sb.append(", statDataSenderSocketType='").append(statDataSenderSocketType).append('\'');
        sb.append(", tcpDataSenderCommandAcceptEnable=").append(tcpDataSenderCommandAcceptEnable);
        sb.append(", tcpDataSenderCommandActiveThreadEnable=").append(tcpDataSenderCommandActiveThreadEnable);
        sb.append(", tcpDataSenderCommandActiveThreadCountEnable=").append(tcpDataSenderCommandActiveThreadCountEnable);
        sb.append(", tcpDataSenderCommandActiveThreadDumpEnable=").append(tcpDataSenderCommandActiveThreadDumpEnable);
        sb.append(", tcpDataSenderCommandActiveThreadLightDumpEnable=").append(tcpDataSenderCommandActiveThreadLightDumpEnable);
        sb.append(", traceAgentActiveThread=").append(traceAgentActiveThread);
        sb.append(", traceAgentDataSource=").append(traceAgentDataSource);
        sb.append(", dataSourceTraceLimitSize=").append(dataSourceTraceLimitSize);
        sb.append(", deadlockMonitorEnable=").append(deadlockMonitorEnable);
        sb.append(", deadlockMonitorInterval=").append(deadlockMonitorInterval);
        sb.append(", callStackMaxDepth=").append(callStackMaxDepth);
        sb.append(", jdbcSqlCacheSize=").append(jdbcSqlCacheSize);
        sb.append(", traceSqlBindValue=").append(traceSqlBindValue);
        sb.append(", maxSqlBindValueSize=").append(maxSqlBindValueSize);
        sb.append(", samplingEnable=").append(samplingEnable);
        sb.append(", samplingRate=").append(samplingRate);
        sb.append(", ioBufferingEnable=").append(ioBufferingEnable);
        sb.append(", ioBufferingBufferSize=").append(ioBufferingBufferSize);
        sb.append(", profileJvmVendorName='").append(profileJvmVendorName).append('\'');
        sb.append(", profileJvmStatCollectIntervalMs=").append(profileJvmStatCollectIntervalMs);
        sb.append(", profileJvmStatBatchSendCount=").append(profileJvmStatBatchSendCount);
        sb.append(", profilerJvmStatCollectDetailedMetrics=").append(profilerJvmStatCollectDetailedMetrics);
        sb.append(", profilableClassFilter=").append(profilableClassFilter);
        sb.append(", DEFAULT_AGENT_INFO_SEND_RETRY_INTERVAL=").append(DEFAULT_AGENT_INFO_SEND_RETRY_INTERVAL);
        sb.append(", agentInfoSendRetryInterval=").append(agentInfoSendRetryInterval);
        sb.append(", applicationServerType='").append(applicationServerType).append('\'');
        sb.append(", applicationTypeDetectOrder=").append(applicationTypeDetectOrder);
        sb.append(", disabledPlugins=").append(disabledPlugins);
        sb.append(", propagateInterceptorException=").append(propagateInterceptorException);
        sb.append(", supportLambdaExpressions=").append(supportLambdaExpressions);
        sb.append('}');
        return sb.toString();
    }
}