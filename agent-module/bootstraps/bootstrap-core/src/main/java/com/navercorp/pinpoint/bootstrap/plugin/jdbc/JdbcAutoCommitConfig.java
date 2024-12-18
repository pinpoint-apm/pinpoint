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
public class JdbcAutoCommitConfig extends JdbcConfig {
    protected final boolean profileSetAutoCommit;
    protected final boolean profileCommit;
    protected final boolean profileRollback;

    public static JdbcAutoCommitConfig of(String name, ProfilerConfig config) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(config, "config");
        return of(DEFAULT_PREFIX, name,  config);
    }

    public static JdbcAutoCommitConfig of(String prefix, String name, ProfilerConfig config) {
        Objects.requireNonNull(prefix, "prefix");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(config, "config");

        return new JdbcAutoCommitConfig(
                name,
                config.readBoolean(String.format("%s.%s", prefix, name), false),
                config.readBoolean(String.format("%s.%s.tracesqlbindvalue", prefix, name), config.getJdbcOption().isTraceSqlBindValue()),
                config.getJdbcOption().getMaxSqlBindValueSize(),
                config.readBoolean(String.format("%s.%s.setautocommit", prefix, name), false),
                config.readBoolean(String.format("%s.%s.commit", prefix, name), false),
                config.readBoolean(String.format("%s.%s.rollback", prefix, name), false)
        );
    }

    protected JdbcAutoCommitConfig(String name, boolean pluginEnable,
                                   boolean traceSqlBindValue,
                                   int maxSqlBindValue,
                                   boolean profileSetAutoCommit,
                                   boolean profileCommit,
                                   boolean profileRollback) {
        super(name, pluginEnable, traceSqlBindValue, maxSqlBindValue);
        this.profileSetAutoCommit = profileSetAutoCommit;
        this.profileCommit = profileCommit;
        this.profileRollback = profileRollback;
    }

    public boolean isProfileSetAutoCommit() {
        return profileSetAutoCommit;
    }

    public boolean isProfileCommit() {
        return profileCommit;
    }

    public boolean isProfileRollback() {
        return profileRollback;
    }

    @Override
    public String toString() {
        return name +
                "{pluginEnable=" + pluginEnable +
                ", traceSqlBindValue=" + traceSqlBindValue +
                ", maxSqlBindValueSize=" + maxSqlBindValueSize +
                ", profileSetAutoCommit=" + profileSetAutoCommit +
                ", profileCommit=" + profileCommit +
                ", profileRollback=" + profileRollback +
                '}';
    }

}
