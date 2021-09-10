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

import com.navercorp.pinpoint.bootstrap.config.util.ValueResolver;
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


    Properties getProperties();

    TransportModule getTransportModule();


    boolean isProfileEnable();

    int getJdbcSqlCacheSize();

    boolean isTraceSqlBindValue();

    int getMaxSqlBindValueSize();

    @InterfaceAudience.Private
    @VisibleForTesting
    boolean getStaticResourceCleanup();

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

    int getLogDirMaxBackupSize();

}
