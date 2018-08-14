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
import com.navercorp.pinpoint.common.util.DefaultJsonParser;
import com.navercorp.pinpoint.common.util.JsonParser;
import com.navercorp.pinpoint.common.util.NormalizedJson;
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
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.json;

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

    private static final String MONGODB_VERSION = "3_7_x";

    // for normalized json
    protected static final JsonParser JSON_PARSER = new DefaultJsonParser();
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
        MongoClient mongoClient = MongoClients.create( "mongodb://localhost:27018" );

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
        String nosql = doc.toJson();
        NormalizedJson normalizedJson = JSON_PARSER.normalizeJson(nosql);

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, insertOne, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(AnnotationKey.MONGO_COLLECTIONINFO.getName(), "customers with MAJORITY")
                , json(normalizedJson.getNormalizedJson(), normalizedJson.getParseParameter())));


        //insert Data
        MongoCollection<Document> collection2 = database.getCollection("customers2").withWriteConcern(WriteConcern.ACKNOWLEDGED);

        Document doc2 = new Document("name", "Roy2").append("company", "Naver2");
        collection2.insertOne(doc2);

        String nosql2 = doc2.toJson();
        NormalizedJson normalizedJson2 = JSON_PARSER.normalizeJson(nosql2);

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, insertOne, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(AnnotationKey.MONGO_COLLECTIONINFO.getName(), "customers2 with ACKNOWLEDGED")
                , json(normalizedJson2.getNormalizedJson(), normalizedJson2.getParseParameter())));


        //update Data
        Document doc3 = new Document("$set", new Document("name", "Roy3"));
        collection.updateOne(doc, doc3);

        String nosql3 = nosql + ", " + doc3.toJson();
        NormalizedJson normalizedJson3 = JSON_PARSER.normalizeJson(nosql3);

        Method updateOne = mongoDatabaseImpl.getDeclaredMethod("updateOne", Bson.class , Bson.class);
        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, updateOne, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(AnnotationKey.MONGO_COLLECTIONINFO.getName(), "customers with MAJORITY")
                , json(normalizedJson3.getNormalizedJson(), normalizedJson3.getParseParameter())));

        //read data
        MongoCursor<Document> cursor = collection.find().iterator();
        Method find = mongoDatabaseImpl.getDeclaredMethod("find");

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, find, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(AnnotationKey.MONGO_COLLECTIONINFO.getName(), "customers with SECONDARYPREFERRED")
                ));

        int resultCount=0;
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
        NormalizedJson normalizedJson4 = JSON_PARSER.normalizeJson(doc4.toJson());

        DeleteResult deleteResult = collection.deleteMany(doc4);
        Method deleteMany = mongoDatabaseImpl.getDeclaredMethod("deleteMany", Bson.class);
        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, deleteMany, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(AnnotationKey.MONGO_COLLECTIONINFO.getName(), "customers with MAJORITY")
                , json(normalizedJson4.getNormalizedJson(), normalizedJson4.getParseParameter())));

        Assert.assertEquals(1, deleteResult.getDeletedCount());

        //close connection
        mongoClient.close();
        stopDB();
    }
}
