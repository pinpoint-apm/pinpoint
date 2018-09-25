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

import com.mongodb.MongoClientOptions;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.connection.Cluster;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.DefaultJsonParser;
import com.navercorp.pinpoint.common.util.JsonParser;
import com.navercorp.pinpoint.common.util.NormalizedJson;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.json;

/**
 * @author Roy Kim
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({
        "org.mongodb:mongodb-driver:[3.0.0,3.6.max]",
        "org.mongodb:bson:3.6.4",
        "de.flapdoodle.embed:de.flapdoodle.embed.mongo:1.50.5"
        })
public class MongoDB_3_0_x extends MongoDBBase {

    private static final String MONGODB_VERSION = "3_0_x";

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

        com.mongodb.MongoClient mongoClient = new com.mongodb.MongoClient( "localhost" , 27018);
        verifier.printCache();
        Class<?> clusterClass = Class.forName("com.mongodb.Mongo");

        Constructor create = clusterClass.getDeclaredConstructor(Cluster.class, MongoClientOptions.class, List.class);
        verifier.verifyTrace(event(MONGODB, create, null, MONGODB_ADDRESS, null));

        database = mongoClient.getDatabase("myMongoDbFake").withReadPreference(ReadPreference.secondaryPreferred()).withWriteConcern(WriteConcern.MAJORITY);
        MongoCollection<Document> collection = database.getCollection("customers");

        //insert Data
        Document doc = new Document("name", "Roy").append("company", "Naver");
        collection.insertOne(doc);

        Class<?> mongoDatabaseImpl = Class.forName("com.mongodb.MongoCollectionImpl");
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

        //delete
        Document doc4 = new Document("name", "Roy3");
        NormalizedJson normalizedJson4 = JSON_PARSER.normalizeJson(doc4.toJson());

        DeleteResult deleteResult = collection.deleteMany(doc4);
        Method deleteMany = mongoDatabaseImpl.getDeclaredMethod("deleteMany", Bson.class);
        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, deleteMany, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(AnnotationKey.MONGO_COLLECTIONINFO.getName(), "customers with MAJORITY")
                , json(normalizedJson4.getNormalizedJson(), normalizedJson4.getParseParameter())));

        Assert.assertEquals(1, deleteResult.getDeletedCount());


        mongoClient.close();
        stopDB();
    }
}
