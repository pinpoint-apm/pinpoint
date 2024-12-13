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
import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.config.Value;
import com.navercorp.pinpoint.common.config.util.BypassResolver;
import com.navercorp.pinpoint.common.config.util.ValueResolver;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * @author emeroad
 * @author netspider
 */
public class DefaultProfilerConfig implements ProfilerConfig {
    public static final String PROFILER_INTERCEPTOR_EXCEPTION_PROPAGATE = "profiler.interceptor.exception.propagate";

    // TestAgent only
    public static final String IMPORT_PLUGIN = "profiler.plugin.import-plugin";

    public static final String AGENT_CLASSLOADER_ADDITIONAL_LIBS = "profiler.agent.classloader.additional-libs";

    private final Properties properties;

    @Value("${pinpoint.disable:false}")
    private String pinpointDisable = "false";

    @Value("${profiler.logdir.maxbackupsize}")
    private int logDirMaxBackupSize = 5;

    @Value("${" + Profiles.ACTIVE_PROFILE_KEY + " }")
    private String activeProfile = "";

    @VisibleForTesting
    private boolean staticResourceCleanup = false;

    @Value("${profiler.jdbc.sqlcachesize}")
    private int jdbcSqlCacheSize = 1024;
    @Value("${profiler.jdbc.tracesqlbindvalue}")
    private boolean traceSqlBindValue = false;
    @Value("${profiler.jdbc.maxsqlbindvaluesize}")
    private int maxSqlBindValueSize = 1024;
    @Value("${profiler.jdbc.sqlcachelengthlimit}")
    private int maxSqlCacheLength = 2048;
    @Value("${profiler.jdbc.maxsqllength}")
    private int maxSqlLength = 65536;

    @Value("${profiler.transport.grpc.stats.logging.period}")
    private String grpcStatLoggingPeriod = "PT1M";


    private HttpStatusCodeErrors httpStatusCodeErrors = new HttpStatusCodeErrors();

    @Value("${profiler.guice.module.factory}")
    private String injectionModuleFactoryClazzName = null;
    @Value("${profiler.application.namespace}")
    private String applicationNamespace = "";

    @Value("${" + AGENT_CLASSLOADER_ADDITIONAL_LIBS + "}")
    private String agentClassloaderAdditionalLibs = "";


    public DefaultProfilerConfig() {
        this.properties = new Properties();
    }

    DefaultProfilerConfig(Properties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public String getActiveProfile() {
        return activeProfile;
    }

    @Override
    public String getPinpointDisable() {
        return pinpointDisable;
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
    public int getMaxSqlCacheLength() {
        return maxSqlCacheLength;
    }

    @Override
    public int getMaxSqlLength() {
        return maxSqlLength;
    }

    @Override
    public String getGrpcStatLoggingPeriod() {
        return grpcStatLoggingPeriod;
    }

    @Override
    public boolean getStaticResourceCleanup() {
        return staticResourceCleanup;
    }

    @Override
    public HttpStatusCodeErrors getHttpStatusCodeErrors() {
        return httpStatusCodeErrors;
    }

    @Value("${profiler.http.status.code.errors}")
    void setHttpStatusCodeErrors(String httpStatusCodeErrors) {
        List<String> httpStatusCodeErrorList = StringUtils.tokenizeToStringList(httpStatusCodeErrors, ",");
        this.httpStatusCodeErrors = new HttpStatusCodeErrors(httpStatusCodeErrorList);
    }

    @Override
    public String getInjectionModuleFactoryClazzName() {
        return injectionModuleFactoryClazzName;
    }

    @Override
    public String getApplicationNamespace() {
        return applicationNamespace;
    }


    @Override
    public int getLogDirMaxBackupSize() {
        return logDirMaxBackupSize;
    }

    @Override
    public List<String> getAgentClassloaderAdditionalLibs() {
        return StringUtils.tokenizeToStringList(agentClassloaderAdditionalLibs, ",");
    }

    @Override
    public String readString(String propertyName, String defaultValue) {
        return readString(propertyName, defaultValue, BypassResolver.RESOLVER);
    }

    public String readString(String propertyName, String defaultValue, ValueResolver valueResolver) {
        Objects.requireNonNull(valueResolver, "valueResolver");

        String value = properties.getProperty(propertyName, defaultValue);
        return valueResolver.resolve(propertyName, value);
    }

    @Override
    public int readInt(String propertyName, int defaultValue) {
        String value = properties.getProperty(propertyName);
        return NumberUtils.parseInteger(value, defaultValue);
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
        return result;
    }

    @Override
    public long readLong(String propertyName, long defaultValue) {
        String value = properties.getProperty(propertyName);
        return NumberUtils.parseLong(value, defaultValue);
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
        return Boolean.parseBoolean(value);
    }

    @Override
    public Map<String, String> readPattern(String propertyNamePatternRegex) {
        final Pattern pattern = Pattern.compile(propertyNamePatternRegex);
        final Map<String, String> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                final String key = (String) entry.getKey();
                if (pattern.matcher(key).matches()) {
                    final String value = (String) entry.getValue();
                    result.put(key, value);
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "DefaultProfilerConfig{" +
                "properties=" + properties +
                ", pinpointDisable='" + pinpointDisable + '\'' +
                ", logDirMaxBackupSize=" + logDirMaxBackupSize +
                ", activeProfile='" + activeProfile + '\'' +
                ", staticResourceCleanup=" + staticResourceCleanup +
                ", jdbcSqlCacheSize=" + jdbcSqlCacheSize +
                ", traceSqlBindValue=" + traceSqlBindValue +
                ", maxSqlBindValueSize=" + maxSqlBindValueSize +
                ", maxSqlCacheLength=" + maxSqlCacheLength +
                ", maxSqlLength=" + maxSqlLength +
                ", grpcStatLoggingPeriod='" + grpcStatLoggingPeriod + '\'' +
                ", httpStatusCodeErrors=" + httpStatusCodeErrors +
                ", injectionModuleFactoryClazzName='" + injectionModuleFactoryClazzName + '\'' +
                ", applicationNamespace='" + applicationNamespace + '\'' +
                ", agentClassloaderAdditionalLibs='" + agentClassloaderAdditionalLibs + '\'' +
                '}';
    }
}
