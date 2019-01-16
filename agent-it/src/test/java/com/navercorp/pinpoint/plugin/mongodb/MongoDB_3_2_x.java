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
import com.mongodb.client.MongoCollection;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Roy Kim
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({
        "org.mongodb:mongodb-driver:[3.2.0,3.4.max]",
        "de.flapdoodle.embed:de.flapdoodle.embed.mongo:2.1.1"
})
public class MongoDB_3_2_x extends MongoDBBase {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

    }

    @AfterClass
    public static void tearDownAfterClass() {

    }

    @Test
    public void testConnection() throws Exception {

        startDB();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        com.mongodb.MongoClient mongoClient = new com.mongodb.MongoClient("localhost", 27018);

        database = mongoClient.getDatabase("myMongoDbFake").withReadPreference(ReadPreference.secondaryPreferred()).withWriteConcern(WriteConcern.MAJORITY);
        MongoCollection<Document> collection = database.getCollection("customers");
        MongoCollection<Document> collection2 = database.getCollection("customers2").withWriteConcern(WriteConcern.ACKNOWLEDGED);
        Class<?> mongoDatabaseImpl = Class.forName("com.mongodb.MongoCollectionImpl");

        insertComlexBsonValueData30(verifier, collection, mongoDatabaseImpl, "customers", "MAJORITY");
        insertData(verifier, collection, mongoDatabaseImpl, "customers", "MAJORITY");
        insertData(verifier, collection2, mongoDatabaseImpl, "customers2", "ACKNOWLEDGED");
        updateData(verifier, collection, mongoDatabaseImpl);
        readData(verifier, collection, mongoDatabaseImpl);
        filterData(verifier, collection, mongoDatabaseImpl);
        filterData2(verifier, collection, mongoDatabaseImpl);
        deleteData(verifier, collection, mongoDatabaseImpl);

        mongoClient.close();
        stopDB();
    }
}
