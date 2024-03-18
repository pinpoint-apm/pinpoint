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
@Dependency({"com.datastax.oss:java-driver-core:[4.1.0,4.9.0),[4.11.0,4.max)"})
@SharedDependency({"com.datastax.oss:java-driver-core:[4.15.0]", CassandraITConstants.CASSANDRA_TESTCONTAINER})
@SharedTestLifeCycleClass(CassandraServer3X.class)
public class CassandraDatastax_4_0_x_IT extends CassandraDatastaxITBase {
}
