/*
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
package com.navercorp.pinpoint.plugin.jdbc.mariadb;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcConfig;

/**
 * @author dawidmalina
 */
public class MariaDBConfig extends JdbcConfig {
    private final boolean profileSetAutoCommit;
    private final boolean profileCommit;
    private final boolean profileRollback;

    public MariaDBConfig(ProfilerConfig config) {
        super(config.readBoolean("profiler.jdbc.mariadb", false),
                config.readBoolean("profiler.jdbc.mariadb.tracesqlbindvalue", config.isTraceSqlBindValue()),
                config.getMaxSqlBindValueSize());
        this.profileSetAutoCommit = config.readBoolean("profiler.jdbc.mariadb.setautocommit", false);
        this.profileCommit = config.readBoolean("profiler.jdbc.mariadb.commit", false);
        this.profileRollback = config.readBoolean("profiler.jdbc.mariadb.rollback", false);
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
        return "MariaDBConfig [profileSetAutoCommit=" + profileSetAutoCommit + ", profileCommit=" + profileCommit
                + ", profileRollback=" + profileRollback + "]";
    }

}
