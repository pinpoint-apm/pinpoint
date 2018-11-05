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

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.plugin.mongo.MongoConstants;
import com.navercorp.pinpoint.plugin.mongo.MongoUtil;
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
import org.junit.Assert;

import java.lang.reflect.Method;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author Roy Kim
 */
public abstract class MongoDBBase {

    protected static final String MONGO = "MONGO";
    protected static final String MONGO_EXECUTE_QUERY = "MONGO_EXECUTE_QUERY";
    protected static String MONGODB_ADDRESS = "localhost:" + 27018;

    MongoDatabase database;
    MongodProcess mongod;


    public void startDB() throws Exception {
        MongodStarter starter = MongodStarter.getDefaultInstance();

        String bindIp = "localhost";
        int port = 27018;

        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(bindIp, port, Network.localhostIsIPv6()))
                .build();

        MongodExecutable mongodExecutable = null;

        mongodExecutable = starter.prepare(mongodConfig);
        mongod = mongodExecutable.start();
    }

    public void stopDB() throws Exception {
        //give time for the test to finish"
        Thread.sleep(500L);
        mongod.stop();
    }

    public void insertData(PluginTestVerifier verifier, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl, String collectionInfo, String collectionOption) {
        //insert Data
        Document doc = new Document("name", "Roy").append("company", "Naver");
        collection.insertOne(doc);

        Object[] objects = new Object[1];
        objects[0] = doc;

        StringStringValue parsedBson = MongoUtil.parseBson(objects, true);

        Method insertOne;
        try {
            insertOne = mongoDatabaseImpl.getDeclaredMethod("insertOne", Object.class);
        } catch (NoSuchMethodException e) {
            insertOne = null;
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, insertOne, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), collectionInfo)
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), collectionOption)
                , new ExpectedAnnotation(MongoConstants.MONGO_JSON.getName(), parsedBson)));
    }

    public void updateData(PluginTestVerifier verifier, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl) {

        //update Data
        Document doc = new Document("name", "Roy").append("company", "Naver");
        Document doc2 = new Document("$set", new Document("name", "Roy3"));
        collection.updateOne(doc, doc2);

        Object[] objects = new Object[2];
        objects[0] = doc;
        objects[1] = doc2;

        StringStringValue parsedBson = MongoUtil.parseBson(objects, true);

        Method updateOne;
        try {
            updateOne = mongoDatabaseImpl.getDeclaredMethod("updateOne", Bson.class, Bson.class);
        } catch (NoSuchMethodException e) {
            updateOne = null;
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, updateOne, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), "customers")
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), "MAJORITY")
                , new ExpectedAnnotation(MongoConstants.MONGO_JSON.getName(), parsedBson)));
    }


    public void readData(PluginTestVerifier verifier, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl) {
        //read data
        MongoCursor<Document> cursor = collection.find().iterator();

        Method find;
        try {
            find = mongoDatabaseImpl.getDeclaredMethod("find");
        } catch (NoSuchMethodException e) {
            find = null;
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, find, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), "customers")
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), "secondaryPreferred")));

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
    }

    public void deleteData(PluginTestVerifier verifier, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl) {
        //delete data
        Document doc = new Document("name", "Roy3");
        Object[] objects = new Object[1];
        objects[0] = doc;

        StringStringValue parsedBson = MongoUtil.parseBson(objects, true);

        DeleteResult deleteResult = collection.deleteMany(doc);

        Method deleteMany;
        try {
            deleteMany = mongoDatabaseImpl.getDeclaredMethod("deleteMany", Bson.class);
        } catch (NoSuchMethodException e) {
            deleteMany = null;
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, deleteMany, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), "customers")
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), "MAJORITY")
                , new ExpectedAnnotation(MongoConstants.MONGO_JSON.getName(), parsedBson)));

        Assert.assertEquals(1, deleteResult.getDeletedCount());
    }
}
