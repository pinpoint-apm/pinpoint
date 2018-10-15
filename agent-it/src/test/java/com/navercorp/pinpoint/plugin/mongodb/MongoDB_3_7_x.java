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

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoDriverInformation;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.plugin.mongo.MongoUtil;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.mongoJson;

/**
 * @author Roy Kim
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({
        "org.mongodb:mongodb-driver:[3.7.0,3.8.max]",
        "org.mongodb:bson:3.7.0",
        "de.flapdoodle.embed:de.flapdoodle.embed.mongo:1.50.5"
})
public class MongoDB_3_7_x extends MongoDBBase {

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
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27018");

        verifier.printCache();
        Class<?> CreateDBClass = Class.forName("com.mongodb.client.MongoClients");

        Method create = CreateDBClass.getDeclaredMethod("create", MongoClientSettings.class, MongoDriverInformation.class);
        verifier.verifyTrace(event(MONGODB, create, null, MONGODB_ADDRESS, null));

        database = mongoClient.getDatabase("myMongoDbFake").withReadPreference(ReadPreference.secondaryPreferred()).withWriteConcern(WriteConcern.MAJORITY);
        MongoCollection<Document> collection = database.getCollection("customers");

        //insert Data
        Document doc = new Document("name", "Roy").append("company", "Naver");
        collection.insertOne(doc);

        Class<?> mongoDatabaseImpl = Class.forName("com.mongodb.client.internal.MongoCollectionImpl");
        Method insertOne = mongoDatabaseImpl.getDeclaredMethod("insertOne", Object.class);

        StringStringValue parsedBson = MongoUtil.parseBson(doc, true);

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, insertOne, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(AnnotationKey.MONGO_COLLECTIONINFO.getName(), "customers,MAJORITY")
                , mongoJson(parsedBson.getStringValue1(), parsedBson.getStringValue2())));


        //insert Data
        MongoCollection<Document> collection2 = database.getCollection("customers2").withWriteConcern(WriteConcern.ACKNOWLEDGED);

        Document doc2 = new Document("name", "Roy2").append("company", "Naver2");
        collection2.insertOne(doc2);

        StringStringValue parsedBson2 = MongoUtil.parseBson(doc2, true);

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, insertOne, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(AnnotationKey.MONGO_COLLECTIONINFO.getName(), "customers2,ACKNOWLEDGED")
                , mongoJson(parsedBson2.getStringValue1(), parsedBson2.getStringValue2())));


        //update Data
        Document doc3 = new Document("$set", new Document("name", "Roy3"));
        collection.updateOne(doc, doc3);

        StringStringValue parsedBson3 = MongoUtil.parseBson(doc3, true);

        parsedBson.appendStringStringValue(parsedBson3);

        Method updateOne = mongoDatabaseImpl.getDeclaredMethod("updateOne", Bson.class, Bson.class);
        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, updateOne, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(AnnotationKey.MONGO_COLLECTIONINFO.getName(), "customers,MAJORITY")
                , mongoJson(parsedBson.getStringValue1(), parsedBson.getStringValue2())));

        //read data
        MongoCursor<Document> cursor = collection.find().iterator();
        Method find = mongoDatabaseImpl.getDeclaredMethod("find");

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, find, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(AnnotationKey.MONGO_COLLECTIONINFO.getName(), "customers,SECONDARYPREFERRED")
        ));

        int resultCount = 0;
        try {
            while (cursor.hasNext()) {
                resultCount++;
                cursor.next();
            }
        } finally {
            cursor.close();
        }
        Assert.assertEquals(1, resultCount);

        //delete data
        Document doc4 = new Document("name", "Roy3");
        StringStringValue parsedBson4 = MongoUtil.parseBson(doc4, true);

        DeleteResult deleteResult = collection.deleteMany(doc4);
        Method deleteMany = mongoDatabaseImpl.getDeclaredMethod("deleteMany", Bson.class);
        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, deleteMany, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(AnnotationKey.MONGO_COLLECTIONINFO.getName(), "customers,MAJORITY")
                , mongoJson(parsedBson4.getStringValue1(), parsedBson4.getStringValue2())));

        Assert.assertEquals(1, deleteResult.getDeletedCount());

        //close connection
        mongoClient.close();
        stopDB();
    }
}
