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
package com.navercorp.pinpoint.plugin.cassandra;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcConfig;

/**
 * @author dawidmalina
 */
public class CassandraConfig extends JdbcConfig {

    public CassandraConfig(ProfilerConfig config) {
        super(config.readBoolean("profiler.cassandra", false),
                config.readBoolean("profiler.cassandra.tracecqlbindvalue", config.isTraceSqlBindValue()),
                config.getMaxSqlBindValueSize());
    }

    @Override
    public String toString() {
        return "CassandraConfig [cassandra=" + isPluginEnable() + "]";
    }

}
