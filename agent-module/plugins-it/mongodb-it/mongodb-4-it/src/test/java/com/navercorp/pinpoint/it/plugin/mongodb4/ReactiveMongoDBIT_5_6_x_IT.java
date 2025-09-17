/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.it.plugin.mongodb4;

import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.it.plugin.utils.TestcontainersOption;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.DriverProperties;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCTestConstants;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import com.navercorp.pinpoint.test.plugin.shared.SharedDependency;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;

@PluginTest
@PinpointAgent(AgentPath.PATH)
@JvmVersion(8)
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-mongodb-driver-plugin"})
@Dependency({
        "org.mongodb:mongodb-driver-reactivestreams:[5.6.0,]",
        PluginITConstants.VERSION, JDBCTestConstants.VERSION
})
@SharedDependency({PluginITConstants.VERSION, JDBCTestConstants.VERSION, TestcontainersOption.TEST_CONTAINER, TestcontainersOption.MONGODB})
@SharedTestLifeCycleClass(MongodbServer.class)
public class ReactiveMongoDBIT_5_6_x_IT extends MongoDBITBase {
    @AutoClose
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static URI uri;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        DriverProperties driverProperties = getDriverProperties();
        uri = new URI(driverProperties.getUrl());
        mongoClient = MongoClients.create("mongodb://" + uri.getHost() + ":" + uri.getPort());
        database = mongoClient.getDatabase("myMongoDbFake").withReadPreference(ReadPreference.secondaryPreferred()).withWriteConcern(WriteConcern.MAJORITY);
    }

    @Override
    Class<?> getMongoDatabaseClazz() throws ClassNotFoundException {
        return Class.forName("com.mongodb.reactivestreams.client.internal.MongoCollectionImpl");
    }

    @Test
    public void testStatements() throws Exception {
        final ReactiveMongoDBITHelper helper = new ReactiveMongoDBITHelper();
        final String address = uri.getHost() + ":" + uri.getPort();
        helper.testConnection(address, database, getMongoDatabaseClazz(), "ACKNOWLEDGED", ReactiveMongoDBITHelper.SIMPLIFY_OPERATIONS);
    }
}
