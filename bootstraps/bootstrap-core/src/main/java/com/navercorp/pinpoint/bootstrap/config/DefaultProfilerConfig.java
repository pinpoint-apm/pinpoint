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

import com.navercorp.pinpoint.bootstrap.config.util.BypassResolver;
import com.navercorp.pinpoint.bootstrap.config.util.ValueResolver;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.common.util.logger.CommonLogger;
import com.navercorp.pinpoint.common.util.logger.StdoutCommonLoggerFactory;

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

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE.getLogger(DefaultProfilerConfig.class.getName());

    // TestAgent only
    public static final String IMPORT_PLUGIN = "profiler.plugin.import-plugin";

    private final Properties properties;

    private static final TransportModule DEFAULT_TRANSPORT_MODULE = TransportModule.THRIFT;

    @Value("${profiler.enable:true}")
    private boolean profileEnable = false;

    @Value("${profiler.logdir.maxbackupsize}")
    private int logDirMaxBackupSize = 5;

    @Value("${" + Profiles.ACTIVE_PROFILE_KEY + " }")
    private String activeProfile = Profiles.DEFAULT_ACTIVE_PROFILE;

    @VisibleForTesting
    private boolean staticResourceCleanup = false;

    private TransportModule transportModule = DEFAULT_TRANSPORT_MODULE;

    @Value("${profiler.jdbc.sqlcachesize}")
    private int jdbcSqlCacheSize = 1024;
    @Value("${profiler.jdbc.tracesqlbindvalue}")
    private boolean traceSqlBindValue = false;
    @Value("${profiler.jdbc.maxsqlbindvaluesize}")
    private int maxSqlBindValueSize = 1024;


    private HttpStatusCodeErrors httpStatusCodeErrors = new HttpStatusCodeErrors();

    @Value("${profiler.guice.module.factory}")
    private String injectionModuleFactoryClazzName = null;
    @Value("${profiler.application.namespace}")
    private String applicationNamespace = "";


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
    public TransportModule getTransportModule() {
        return transportModule;
    }

    @Value("${profiler.transport.module}")
    public void setTransportModule(String transportModule) {
        this.transportModule = TransportModule.parse(transportModule, DEFAULT_TRANSPORT_MODULE);
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
    public String readString(String propertyName, String defaultValue) {
        return readString(propertyName, defaultValue, BypassResolver.RESOLVER);
    }

    public String readString(String propertyName, String defaultValue, ValueResolver valueResolver) {
        Objects.requireNonNull(valueResolver, "valueResolver");

        String value = properties.getProperty(propertyName, defaultValue);
        value = valueResolver.resolve(propertyName, value);
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

        if (logger.isDebugEnabled()) {
            logger.debug(propertyNamePatternRegex + "=" + result);
        }

        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultProfilerConfig{");
        sb.append("profileEnable='").append(profileEnable).append('\'');
        sb.append(", activeProfile=").append(activeProfile);
        sb.append(", logDirMaxBackupSize=").append(logDirMaxBackupSize);
        sb.append(", staticResourceCleanup=").append(staticResourceCleanup);
        sb.append(", jdbcSqlCacheSize=").append(jdbcSqlCacheSize);
        sb.append(", traceSqlBindValue=").append(traceSqlBindValue);
        sb.append(", maxSqlBindValueSize=").append(maxSqlBindValueSize);
        sb.append(", httpStatusCodeErrors=").append(httpStatusCodeErrors);
        sb.append(", injectionModuleFactoryClazzName='").append(injectionModuleFactoryClazzName).append('\'');
        sb.append(", applicationNamespace='").append(applicationNamespace).append('\'');
        sb.append('}');
        return sb.toString();
    }
}