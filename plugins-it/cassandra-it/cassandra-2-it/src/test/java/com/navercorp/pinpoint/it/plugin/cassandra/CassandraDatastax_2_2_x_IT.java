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

package com.navercorp.pinpoint.it.plugin.cassandra;

import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import com.navercorp.pinpoint.test.plugin.shared.SharedDependency;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;

/**
 * @author HyunGil Jeong
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"com.datastax.cassandra:cassandra-driver-core:[2.2.min,2.2.max]",
        PluginITConstants.VERSION})
@SharedTestLifeCycleClass(CassandraServer2X.class)
@SharedDependency({"com.datastax.cassandra:cassandra-driver-core:2.1.7.1", CassandraITConstants.CASSANDRA_TESTCONTAINER})
public class CassandraDatastax_2_2_x_IT extends CassandraDatastaxITBase {

}
