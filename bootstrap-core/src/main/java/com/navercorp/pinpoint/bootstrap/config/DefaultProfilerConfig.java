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

import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.bootstrap.util.spring.PropertyPlaceholderHelper;
import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.common.util.logger.CommonLogger;
import com.navercorp.pinpoint.common.util.PropertyUtils;
import com.navercorp.pinpoint.common.util.logger.StdoutCommonLoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
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
    public static final String PROFILER_INTERCEPTOR_EXCEPTION_PROPAGATE = "profiler.interceptor.exception.propagate";

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE.getLogger(DefaultProfilerConfig.class.getName());

    public static final String PLUGIN_DISABLE = "profiler.plugin.disable";
    // TestAgent only
    public static final String IMPORT_PLUGIN = "profiler.plugin.import-plugin";

    private final Properties properties;

    public static final String INSTRUMENT_ENGINE_ASM = "ASM";

    private static final TransportModule DEFAULT_TRANSPORT_MODULE = TransportModule.THRIFT;

    public static final int DEFAULT_AGENT_STAT_COLLECTION_INTERVAL_MS = 5 * 1000;
    public static final int DEFAULT_NUM_AGENT_STAT_BATCH_SEND = 6;

    private static class BypassResolver implements ValueResolver {
        public static final ValueResolver RESOLVER = new BypassResolver();

        @Override
        public String resolve(String value, Properties properties) {
            return value;
        }
    }

    public static class PlaceHolderResolver implements ValueResolver {
        @Override
        public String resolve(String value, Properties properties) {
            if (value == null) {
                return null;
            }
            PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper("${", "}");
            return propertyPlaceholderHelper.replacePlaceholders(value, properties);
        }
    }

    public static ProfilerConfig load(String pinpointConfigFileName) throws IOException {
        final Properties properties = loadProperties(pinpointConfigFileName);
        return new DefaultProfilerConfig(properties);
    }

    private static Properties loadProperties(String pinpointConfigFileName) throws IOException {
        try {
            return PropertyUtils.loadProperty(pinpointConfigFileName);
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

    private String activeProfile = Profiles.DEFAULT_ACTIVE_PROFILE;

    private String profileInstrumentEngine = INSTRUMENT_ENGINE_ASM;
    private boolean instrumentMatcherEnable = true;
    private InstrumentMatcherCacheConfig instrumentMatcherCacheConfig = new InstrumentMatcherCacheConfig();

    private int interceptorRegistrySize = 1024 * 8;

    @VisibleForTesting
    private boolean staticResourceCleanup = false;

    private TransportModule transportModule = DEFAULT_TRANSPORT_MODULE;

    private ThriftTransportConfig thriftTransportConfig;

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
    private int samplingNewThroughput = 0;
    private int samplingContinueThroughput = 0;

    // span buffering
    private boolean ioBufferingEnable;
    private int ioBufferingBufferSize;

    private String profileJvmVendorName;
    private String profileOsName;
    private int profileJvmStatCollectIntervalMs = DEFAULT_AGENT_STAT_COLLECTION_INTERVAL_MS;
    private int profileJvmStatBatchSendCount = DEFAULT_NUM_AGENT_STAT_BATCH_SEND;
    private boolean profilerJvmStatCollectDetailedMetrics;

    private Filter<String> profilableClassFilter = new SkipFilter<String>();

    private final long DEFAULT_AGENT_INFO_SEND_RETRY_INTERVAL = 5 * 60 * 1000L;
    private long agentInfoSendRetryInterval = DEFAULT_AGENT_INFO_SEND_RETRY_INTERVAL;

    private String applicationServerType;
    @Deprecated // As of 1.9.0, set application type in plugins
    private List<String> applicationTypeDetectOrder = Collections.emptyList();
    private List<String> pluginLoadOrder = Collections.emptyList();
    private List<String> disabledPlugins = Collections.emptyList();

    private boolean propagateInterceptorException = false;
    private boolean supportLambdaExpressions = true;

    private boolean proxyHttpHeaderEnable = true;

    private HttpStatusCodeErrors httpStatusCodeErrors = new HttpStatusCodeErrors();

    private String injectionModuleFactoryClazzName = null;
    private String applicationNamespace = "";

    public DefaultProfilerConfig() {
        this.properties = new Properties();
        this.thriftTransportConfig = new DefaultThriftTransportConfig();
    }

    public DefaultProfilerConfig(Properties properties) {
        if (properties == null) {
            throw new NullPointerException("properties");
        }
        this.properties = properties;
        readPropertyValues();
    }


    @Override
    public String getActiveProfile() {
        return activeProfile;
    }

    @Override
    public TransportModule getTransportModule() {
        return transportModule;
    }

    public void setTransportModule(TransportModule transportModule) {
        this.transportModule = transportModule;
    }

    @Override
    public int getInterceptorRegistrySize() {
        return interceptorRegistrySize;
    }

    @Override
    public ThriftTransportConfig getThriftTransportConfig() {
//        if (thriftTransportConfig == null){
//          // TODO ?
//        }
        return thriftTransportConfig;
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public String getCollectorSpanServerIp() {
        return getThriftTransportConfig().getCollectorSpanServerIp();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public int getCollectorSpanServerPort() {
        return getThriftTransportConfig().getCollectorSpanServerPort();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public String getCollectorStatServerIp() {
        return getThriftTransportConfig().getCollectorStatServerIp();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public int getCollectorStatServerPort() {
        return getThriftTransportConfig().getCollectorStatServerPort();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public String getCollectorTcpServerIp() {
        return getThriftTransportConfig().getCollectorTcpServerIp();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public int getCollectorTcpServerPort() {
        return getThriftTransportConfig().getCollectorTcpServerPort();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public int getStatDataSenderWriteQueueSize() {
        return getThriftTransportConfig().getStatDataSenderWriteQueueSize();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public int getStatDataSenderSocketSendBufferSize() {
        return getThriftTransportConfig().getStatDataSenderSocketSendBufferSize();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public int getStatDataSenderSocketTimeout() {
        return getThriftTransportConfig().getStatDataSenderSocketTimeout();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public String getStatDataSenderSocketType() {
        return getThriftTransportConfig().getStatDataSenderSocketType();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public String getStatDataSenderTransportType() {
        return getThriftTransportConfig().getStatDataSenderTransportType();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public int getSpanDataSenderWriteQueueSize() {
        return getThriftTransportConfig().getSpanDataSenderWriteQueueSize();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public int getSpanDataSenderSocketSendBufferSize() {
        return getThriftTransportConfig().getSpanDataSenderSocketSendBufferSize();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public boolean isTcpDataSenderCommandAcceptEnable() {
        return getThriftTransportConfig().isTcpDataSenderCommandAcceptEnable();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public boolean isTcpDataSenderCommandActiveThreadEnable() {
        return getThriftTransportConfig().isTcpDataSenderCommandActiveThreadEnable();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public boolean isTcpDataSenderCommandActiveThreadCountEnable() {
        return getThriftTransportConfig().isTcpDataSenderCommandActiveThreadCountEnable();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public boolean isTcpDataSenderCommandActiveThreadDumpEnable() {
        return getThriftTransportConfig().isTcpDataSenderCommandActiveThreadDumpEnable();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public boolean isTcpDataSenderCommandActiveThreadLightDumpEnable() {
        return getThriftTransportConfig().isTcpDataSenderCommandActiveThreadLightDumpEnable();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public long getTcpDataSenderPinpointClientWriteTimeout() {
        return getThriftTransportConfig().getTcpDataSenderPinpointClientWriteTimeout();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public long getTcpDataSenderPinpointClientRequestTimeout() {
        return getThriftTransportConfig().getTcpDataSenderPinpointClientRequestTimeout();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public long getTcpDataSenderPinpointClientReconnectInterval() {
        return getThriftTransportConfig().getTcpDataSenderPinpointClientReconnectInterval();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public long getTcpDataSenderPinpointClientPingInterval() {
        return getThriftTransportConfig().getTcpDataSenderPinpointClientPingInterval();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public long getTcpDataSenderPinpointClientHandshakeInterval() {
        return getThriftTransportConfig().getTcpDataSenderPinpointClientHandshakeInterval();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public int getSpanDataSenderSocketTimeout() {
        return getThriftTransportConfig().getSpanDataSenderSocketTimeout();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public String getSpanDataSenderSocketType() {
        return getThriftTransportConfig().getSpanDataSenderSocketType();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public String getSpanDataSenderTransportType() {
        return getThriftTransportConfig().getSpanDataSenderTransportType();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public int getSpanDataSenderChunkSize() {
        return getThriftTransportConfig().getSpanDataSenderChunkSize();
    }

    /**
     * @deprecated Use {@link #getThriftTransportConfig()} instead.
     */
    @Deprecated
    @Override
    public int getStatDataSenderChunkSize() {
        return getThriftTransportConfig().getStatDataSenderChunkSize();
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
    public int getSamplingNewThroughput() {
        return samplingNewThroughput;
    }

    @Override
    public int getSamplingContinueThroughput() {
        return samplingContinueThroughput;
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
    public String getProfilerOSName() {
        return profileOsName;
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
    public boolean getStaticResourceCleanup() {
        return staticResourceCleanup;
    }

    @Deprecated
    public void setStaticResourceCleanup(boolean staticResourceCleanup) {
        this.staticResourceCleanup = staticResourceCleanup;
    }


    @Override
    public Filter<String> getProfilableClassFilter() {
        return profilableClassFilter;
    }

    /**
     * @deprecated As of 1.9.0, set application type in plugins
     */
    @Deprecated
    @Override
    public List<String> getApplicationTypeDetectOrder() {
        return applicationTypeDetectOrder;
    }

    @Override
    public List<String> getPluginLoadOrder() {
        return pluginLoadOrder;
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

    @Deprecated
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
    public boolean isProxyHttpHeaderEnable() {
        return proxyHttpHeaderEnable;
    }

    @Override
    public HttpStatusCodeErrors getHttpStatusCodeErrors() {
        return httpStatusCodeErrors;
    }

    @Override
    public String getInjectionModuleFactoryClazzName() {
        return injectionModuleFactoryClazzName;
    }

    @Override
    public String getApplicationNamespace() {
        return applicationNamespace;
    }

    // for test
    void readPropertyValues() {

        this.profileEnable = readBoolean("profiler.enable", true);
        this.activeProfile = readString(Profiles.ACTIVE_PROFILE_KEY, Profiles.DEFAULT_ACTIVE_PROFILE);
        this.profileInstrumentEngine = readString("profiler.instrument.engine", INSTRUMENT_ENGINE_ASM);
        this.instrumentMatcherEnable = readBoolean("profiler.instrument.matcher.enable", true);

        this.instrumentMatcherCacheConfig.setInterfaceCacheSize(readInt("profiler.instrument.matcher.interface.cache.size", 4));
        this.instrumentMatcherCacheConfig.setInterfaceCacheEntrySize(readInt("profiler.instrument.matcher.interface.cache.entry.size", 16));
        this.instrumentMatcherCacheConfig.setAnnotationCacheSize(readInt("profiler.instrument.matcher.annotation.cache.size", 4));
        this.instrumentMatcherCacheConfig.setAnnotationCacheEntrySize(readInt("profiler.instrument.matcher.annotation.cache.entry.size", 4));
        this.instrumentMatcherCacheConfig.setSuperCacheSize(readInt("profiler.instrument.matcher.super.cache.size", 4));
        this.instrumentMatcherCacheConfig.setSuperCacheEntrySize(readInt("profiler.instrument.matcher.super.cache.entry.size", 4));

        this.interceptorRegistrySize = readInt("profiler.interceptorregistry.size", 1024 * 8);

        final String transportModuleString = readString("profiler.transport.module", DEFAULT_TRANSPORT_MODULE.name());
        this.transportModule = TransportModule.parse(transportModuleString, DEFAULT_TRANSPORT_MODULE);
        this.thriftTransportConfig = readThriftTransportConfig(this);

        this.traceAgentActiveThread = readBoolean("profiler.pinpoint.activethread", true);

        this.traceAgentDataSource = readBoolean("profiler.pinpoint.datasource", false);
        this.dataSourceTraceLimitSize = readInt("profiler.pinpoint.datasource.tracelimitsize", 20);

        this.deadlockMonitorEnable = readBoolean("profiler.monitor.deadlock.enable", true);
        this.deadlockMonitorInterval = readLong("profiler.monitor.deadlock.interval", 60000L);

        // CallStack
        this.callStackMaxDepth = readInt("profiler.callstack.max.depth", 64);
        if (this.callStackMaxDepth != -1 && this.callStackMaxDepth < 2) {
            this.callStackMaxDepth = 2;
        }

        // JDBC
        this.jdbcSqlCacheSize = readInt("profiler.jdbc.sqlcachesize", 1024);
        this.traceSqlBindValue = readBoolean("profiler.jdbc.tracesqlbindvalue", false);


        this.samplingEnable = readBoolean("profiler.sampling.enable", true);
        this.samplingRate = readInt("profiler.sampling.rate", 1);
        // Throughput sampling
        this.samplingNewThroughput = readInt("profiler.sampling.new.throughput", 0);
        this.samplingContinueThroughput = readInt("profiler.sampling.continue.throughput", 0);

        // configuration for sampling and IO buffer 
        this.ioBufferingEnable = readBoolean("profiler.io.buffering.enable", true);

        // it may be a problem to be here.  need to modify(delete or move or .. )  this configuration.
        this.ioBufferingBufferSize = readInt("profiler.io.buffering.buffersize", 20);

        //OS
        this.profileOsName = readString("profiler.os.name", null);

        // JVM
        this.profileJvmVendorName = readString("profiler.jvm.vendor.name", null);
        this.profileJvmStatCollectIntervalMs = readInt("profiler.jvm.stat.collect.interval", DEFAULT_AGENT_STAT_COLLECTION_INTERVAL_MS);
        this.profileJvmStatBatchSendCount = readInt("profiler.jvm.stat.batch.send.count", DEFAULT_NUM_AGENT_STAT_BATCH_SEND);
        this.profilerJvmStatCollectDetailedMetrics = readBoolean("profiler.jvm.stat.collect.detailed.metrics", false);

        this.agentInfoSendRetryInterval = readLong("profiler.agentInfo.send.retry.interval", DEFAULT_AGENT_INFO_SEND_RETRY_INTERVAL);

        // service type
        this.applicationServerType = readString("profiler.applicationservertype", null);

        // application type detector order
        this.applicationTypeDetectOrder = readList("profiler.type.detect.order");

        this.pluginLoadOrder = readList("profiler.plugin.load.order");

        this.disabledPlugins = readList(PLUGIN_DISABLE);

        // TODO have to remove        
        // profile package included in order to test "call stack view".
        // this config must not be used in service environment because the size of  profiling information will get heavy.
        // We may need to change this configuration to regular expression.
        final String profilableClass = readString("profiler.include", "");
        if (!profilableClass.isEmpty()) {
            this.profilableClassFilter = new ProfilableClassFilter(profilableClass);
        }

        this.propagateInterceptorException = readBoolean(PROFILER_INTERCEPTOR_EXCEPTION_PROPAGATE, false);
        this.supportLambdaExpressions = readBoolean("profiler.lambda.expressions.support", true);

        // proxy http header names
        this.proxyHttpHeaderEnable = readBoolean("profiler.proxy.http.header.enable", true);

        this.httpStatusCodeErrors = new HttpStatusCodeErrors(readList("profiler.http.status.code.errors"));

        this.injectionModuleFactoryClazzName = readString("profiler.guice.module.factory", null);

        this.applicationNamespace = readString("profiler.application.namespace", "");

        logger.info("configuration loaded successfully.");
    }

    private ThriftTransportConfig readThriftTransportConfig(DefaultProfilerConfig profilerConfig) {
        DefaultThriftTransportConfig binaryTransportConfig = new DefaultThriftTransportConfig();
        binaryTransportConfig.read(profilerConfig);
        return binaryTransportConfig;
    }


    @Override
    public String readString(String propertyName, String defaultValue) {
        return readString(propertyName, defaultValue, BypassResolver.RESOLVER);
    }

    public String readString(String propertyName, String defaultValue, ValueResolver valueResolver) {
        if (valueResolver == null) {
            throw new NullPointerException("valueResolver");
        }
        String value = properties.getProperty(propertyName, defaultValue);
        value = valueResolver.resolve(value, properties);
        if (logger.isDebugEnabled()) {
            logger.debug(propertyName + "=" + value);
        }
        return value;
    }

    @Override
    public int readInt(String propertyName, int defaultValue) {
        String value = properties.getProperty(propertyName);
        int result = NumberUtils.parseInteger(value, defaultValue);
        if (logger.isDebugEnabled()) {
            logger.debug(propertyName + "=" + result);
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
        if (logger.isDebugEnabled()) {
            logger.debug(propertyName + "=" + result);
        }
        return result;
    }

    @Override
    public long readLong(String propertyName, long defaultValue) {
        String value = properties.getProperty(propertyName);
        long result = NumberUtils.parseLong(value, defaultValue);
        if (logger.isDebugEnabled()) {
            logger.debug(propertyName + "=" + result);
        }
        return result;
    }

    @Override
    public List<String> readList(String propertyName) {
        String value = properties.getProperty(propertyName);
        if (StringUtils.isEmpty(value)) {
            return Collections.emptyList();
        }
        return StringUtils.tokenizeToStringList(value, ",");
    }

    @Override
    public boolean readBoolean(String propertyName, boolean defaultValue) {
        String value = properties.getProperty(propertyName, Boolean.toString(defaultValue));
        boolean result = Boolean.parseBoolean(value);
        if (logger.isDebugEnabled()) {
            logger.debug(propertyName + "=" + result);
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

        if (logger.isDebugEnabled()) {
            logger.debug(propertyNamePatternRegex + "=" + result);
        }

        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultProfilerConfig{");
        sb.append("properties=").append(properties);
        sb.append(", profileEnable='").append(profileEnable).append('\'');
        sb.append(", activeProfile=").append(activeProfile);
        sb.append(", profileInstrumentEngine='").append(profileInstrumentEngine).append('\'');
        sb.append(", instrumentMatcherEnable=").append(instrumentMatcherEnable);
        sb.append(", instrumentMatcherCacheConfig=").append(instrumentMatcherCacheConfig);
        sb.append(", interceptorRegistrySize=").append(interceptorRegistrySize);
        sb.append(", thriftTransportConfig=").append(thriftTransportConfig).append('\'');
        sb.append(", staticResourceCleanup=").append(staticResourceCleanup);
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
        sb.append(", samplingNewThroughput=").append(samplingNewThroughput);
        sb.append(", samplingContinueThroughput=").append(samplingContinueThroughput);
        sb.append(", ioBufferingEnable=").append(ioBufferingEnable);
        sb.append(", ioBufferingBufferSize=").append(ioBufferingBufferSize);
        sb.append(", profileJvmVendorName='").append(profileJvmVendorName).append('\'');
        sb.append(", profileOsName='").append(profileOsName).append('\'');
        sb.append(", profileJvmStatCollectIntervalMs=").append(profileJvmStatCollectIntervalMs);
        sb.append(", profileJvmStatBatchSendCount=").append(profileJvmStatBatchSendCount);
        sb.append(", profilerJvmStatCollectDetailedMetrics=").append(profilerJvmStatCollectDetailedMetrics);
        sb.append(", profilableClassFilter=").append(profilableClassFilter);
        sb.append(", agentInfoSendRetryInterval=").append(agentInfoSendRetryInterval);
        sb.append(", applicationServerType='").append(applicationServerType).append('\'');
        sb.append(", applicationTypeDetectOrder=").append(applicationTypeDetectOrder);
        sb.append(", pluginLoadOrder=").append(pluginLoadOrder);
        sb.append(", disabledPlugins=").append(disabledPlugins);
        sb.append(", propagateInterceptorException=").append(propagateInterceptorException);
        sb.append(", supportLambdaExpressions=").append(supportLambdaExpressions);
        sb.append(", proxyHttpHeaderEnable=").append(proxyHttpHeaderEnable);
        sb.append(", httpStatusCodeErrors=").append(httpStatusCodeErrors);
        sb.append(", injectionModuleFactoryClazzName='").append(injectionModuleFactoryClazzName).append('\'');
        sb.append(", applicationNamespace='").append(applicationNamespace).append('\'');
        sb.append('}');
        return sb.toString();
    }

}