/*
 * Copyright 2018 Naver Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance,the License.
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

package com.navercorp.pinpoint.plugin.mongodb;

import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;

import com.mongodb.client.MongoDatabase;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.TestcontainersOption;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

import com.navercorp.pinpoint.test.plugin.shared.BeforeSharedClass;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * @author Roy Kim
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@JvmVersion(8)
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-mongodb-driver-plugin"})
@Dependency({
        "org.mongodb:mongodb-driver:[3.0.0,3.1.max]",
        TestcontainersOption.TEST_CONTAINER, TestcontainersOption.MONGODB})

public class MongoDBIT_3_0_x_IT extends MongoDBITBase {

    private static com.mongodb.MongoClient mongoClient;
    public static MongoDatabase database;

    @BeforeSharedClass
    public static void sharedSetup() throws Exception {
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());
        container = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));

        container.start();

        setHost(container.getHost());
        setPort(container.getFirstMappedPort());
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        String host = getHost();
        int port = getPort();
        mongoClient = new com.mongodb.MongoClient(host, port);
        database = mongoClient.getDatabase("myMongoDbFake").withReadPreference(ReadPreference.secondaryPreferred()).withWriteConcern(WriteConcern.MAJORITY);
    }

    @AfterClass
    public static void cleanAfterClass() throws Exception {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    @Override
    Class<?> getMongoDatabaseClazz() throws ClassNotFoundException {
        return Class.forName("com.mongodb.MongoCollectionImpl");
    }

    @Test
    public void testStatements() throws Exception {
        final MongoDBITHelper helper = new MongoDBITHelper();
        final String address = getHost() + ":" + getPort();
        helper.testConnection30(this, address, database, getMongoDatabaseClazz(), "SAFE");
    }
}
