/*
 * Copyright 2018 Naver Corp.
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

package com.navercorp.pinpoint.plugin.mongodb;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoDriverInformation;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author Roy Kim
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({
        "org.mongodb:mongodb-driver:[3.7.0,3.7.max]",
        "org.mongodb:bson:3.7.0",
        "de.flapdoodle.embed:de.flapdoodle.embed.mongo:1.50.5"
        })
public class MongoDB_3_7_x extends MongoDBBase {

    private static final String MONGODB_VERSION = "3_7_x";

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

        //create DB
        com.mongodb.client.MongoClient mongoClient = MongoClients.create( "mongodb://localhost:27018" );

        verifier.printCache();
        Class<?> CreateDBClass = Class.forName("com.mongodb.client.MongoClients");

        Method create = CreateDBClass.getDeclaredMethod("create", MongoClientSettings.class, MongoDriverInformation.class);
        verifier.verifyTrace(event(MONGODB, create, null, MONGODB_ADDRESS, null));

        //get DB, get Collection is instrumented but has no trace, only passing variable
        database = mongoClient.getDatabase("myMongoDbFake");
        MongoCollection<Document> collection = database.getCollection("customers");

        //insert Data
        Document doc = new Document("name", "Roy").append("company", "Naver");
        collection.insertOne(doc);
        verifier.printCache();
        Class<?> mongoDatabaseImpl = Class.forName("com.mongodb.client.internal.MongoCollectionImpl");

        Method insertOne = mongoDatabaseImpl.getDeclaredMethod("insertOne", Object.class);
        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, insertOne, null, MONGODB_ADDRESS, null));

        // Data
        Document doc2 = new Document("name", "Roy2").append("company", "Naver");
        collection.insertOne(doc2);
        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, insertOne, null, MONGODB_ADDRESS, null));


        //update Data
        collection.updateOne(doc, new Document("$set", new Document("name", "Roy3")));

        Method updateOne = mongoDatabaseImpl.getDeclaredMethod("updateOne", Bson.class , Bson.class);
        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, updateOne, null, MONGODB_ADDRESS, null));


        //read data
        MongoCursor<Document> cursor = collection.find().iterator();
        Method find = mongoDatabaseImpl.getDeclaredMethod("find");
        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, find, null, MONGODB_ADDRESS, null));

        int resultCount=0;
        try {
            while (cursor.hasNext()) {
                resultCount++;
                //System.out.println(cursor.next().toJson());
                cursor.next();
            }
        } finally {
            cursor.close();
        }
        Assert.assertEquals(2, resultCount);

        DeleteResult deleteResult = collection.deleteMany(new Document("name", "Roy3"));
        Method deleteMany = mongoDatabaseImpl.getDeclaredMethod("deleteMany", Bson.class);
        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, deleteMany, null, MONGODB_ADDRESS, null));

        Assert.assertEquals(1, deleteResult.getDeletedCount());

        //close connection
        mongoClient.close();
        stopDB();
    }
}
