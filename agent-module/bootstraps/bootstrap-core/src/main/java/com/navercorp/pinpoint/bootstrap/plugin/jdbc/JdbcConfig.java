/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.jdbc;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class JdbcConfig {
    protected final String name;
    protected final boolean pluginEnable;
    protected final boolean traceSqlBindValue;
    protected final int maxSqlBindValueSize;

    public static JdbcConfig of(String name, ProfilerConfig config) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(config, "config");
        return of("profiler.jdbc", name, config);
    }

    public static JdbcConfig of(String prefix, String name, ProfilerConfig config) {
        Objects.requireNonNull(prefix, "prefix");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(config, "config");
        return new JdbcConfig(name,
                config.readBoolean(String.format("%s.%s", prefix, name), false),
                config.readBoolean(String.format("%s.%s.tracesqlbindvalue", prefix, name), config.isTraceSqlBindValue()),
                config.getMaxSqlBindValueSize()
        );
    }

    protected JdbcConfig(String name, boolean pluginEnable, boolean traceSqlBindValue, int maxSqlBindValue) {
        this.name = Objects.requireNonNull(name, "name");
        this.pluginEnable = pluginEnable;
        this.traceSqlBindValue = traceSqlBindValue;
        this.maxSqlBindValueSize = maxSqlBindValue;
    }

    public boolean isPluginEnable() {
        return pluginEnable;
    }

    public boolean isTraceSqlBindValue() {
        return traceSqlBindValue;
    }

    public int getMaxSqlBindValueSize() {
        return maxSqlBindValueSize;
    }

    @Override
    public String toString() {
        return name +
                "{pluginEnable=" + pluginEnable +
                ", traceSqlBindValue=" + traceSqlBindValue +
                ", maxSqlBindValueSize=" + maxSqlBindValueSize +
                '}';
    }
}
