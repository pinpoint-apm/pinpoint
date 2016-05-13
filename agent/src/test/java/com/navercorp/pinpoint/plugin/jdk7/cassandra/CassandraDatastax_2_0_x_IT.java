/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.plugin.jdk7.cassandra;

import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * @author HyunGil Jeong
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({
        "com.datastax.cassandra:cassandra-driver-core:[2.0.12,2.0.max]",
        "org.apache.cassandra:cassandra-all:2.0.17",
        "com.google.guava:guava:17.0",
        "org.codehaus.plexus:plexus-utils:3.0.22"})
public class CassandraDatastax_2_0_x_IT extends CassandraDatastaxITBase {

    private static final String CASSANDRA_VERSION = "2_0_x";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        initializeCluster(CASSANDRA_VERSION);
    }

    @AfterClass
    public static void tearDownAfterClass() {
        cleanUpCluster();
    }

}
