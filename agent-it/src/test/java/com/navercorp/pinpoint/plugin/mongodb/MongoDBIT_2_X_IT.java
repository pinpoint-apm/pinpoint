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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.mongodb.Mongo;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.navercorp.pinpoint.plugin.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

/**
 * @author Community
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@JvmVersion(8)
@Dependency({
        "org.mongodb:mongo-java-driver:[2.14.3]",
        "de.flapdoodle.embed:de.flapdoodle.embed.mongo:1.47.3"
})
public class MongoDBIT_2_X_IT extends MongoDBITBase_2_X {

    private static Mongo mongoClient;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        version = 2.9;
    }

    @AfterClass
    public static void tearDownAfterClass() {
    }

    @Override
    public void setClient() {
        mongoClient = new Mongo("localhost", 27018);
        database = mongoClient.getDB("myMongoDbFake");
        database.setReadPreference(ReadPreference.secondaryPreferred());
        database.setWriteConcern(WriteConcern.MAJORITY);
    }

    @Override
    public void closeClient() {
        mongoClient.close();
    }
}
