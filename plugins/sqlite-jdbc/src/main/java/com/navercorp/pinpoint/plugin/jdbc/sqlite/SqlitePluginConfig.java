/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jdbc.sqlite;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcConfig;

/**
 * @author barney
 *
 */
public class SqlitePluginConfig extends JdbcConfig {

    private final boolean profileSetAutoCommit;
    private final boolean profileCommit;
    private final boolean profileRollback;

    public SqlitePluginConfig(ProfilerConfig src) {
        super(src.readBoolean("profiler.jdbc.sqlite", false),
                src.readBoolean("profiler.jdbc.sqlite.tracesqlbindvalue", src.isTraceSqlBindValue()),
                src.getMaxSqlBindValueSize());
        this.profileSetAutoCommit = src.readBoolean("profiler.jdbc.sqlite.setautocommit", false);
        this.profileCommit = src.readBoolean("profiler.jdbc.sqlite.commit", false);
        this.profileRollback = src.readBoolean("profiler.jdbc.sqlite.rollback", false);
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
        StringBuilder builder = new StringBuilder();
        builder.append("SqlitePluginConfig [profileSetAutoCommit=");
        builder.append(profileSetAutoCommit);
        builder.append(", profileCommit=");
        builder.append(profileCommit);
        builder.append(", profileRollback=");
        builder.append(profileRollback);
        builder.append(", toString()=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }
}
