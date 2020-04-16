/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.cassandra;

import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.PluginITConstants;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * Integration tests require {@link com.datastax.driver.core.StatementWrapper StatementWrapper}, so we
 * limit datastax driver version to 2.1.6+.
 *
 * @author HyunGil Jeong
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-cassandra-driver-plugin", "com.navercorp.pinpoint:pinpoint-httpclient4-plugin"})
@Dependency({
        "com.datastax.cassandra:cassandra-driver-core:[2.1.6,2.1.max]",
        PluginITConstants.VERSION, CassandraITConstants.COMMONS_PROFILER, CassandraITConstants.CASSANDRA_TESTCONTAINER})
public class CassandraDatastax_2_1_x_IT extends CassandraDatastaxITBase {
    @BeforeClass
    public static void beforeClass() {
        startCassandra(CassandraITConstants.CASSANDRA_2_X_IMAGE);
    }
}
