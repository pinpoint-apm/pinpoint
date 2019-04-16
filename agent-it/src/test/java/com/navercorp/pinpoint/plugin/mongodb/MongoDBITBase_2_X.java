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

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Filters.nin;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

import java.lang.reflect.Method;
import java.util.Iterator;

import org.bson.conversions.Bson;
import org.junit.Assert;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.plugin.mongo.MongoConstants;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * @author Community
 */
public abstract class MongoDBITBase_2_X {

    protected static final String MONGO_EXECUTE_QUERY = "MONGO_EXECUTE_QUERY";
    public static DB database;
    public static String secondCollectionDefaultOption = "ACKNOWLEDGED";
    public static double version = 0;
    protected static String MONGODB_ADDRESS = "localhost:" + 27018;
    MongodProcess mongod;

    public abstract void setClient();

    public abstract void closeClient();

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

        //give time for previous DB close to finish and port to be released"
        Thread.sleep(200L);
        mongod = mongodExecutable.start();
        setClient();
    }

    public void stopDB() throws Exception {
        //give time for the test to finish"
        Thread.sleep(100L);

        closeClient();
        mongod.stop();
    }

    @Test
    public void testConnection() throws Exception {
        startDB();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Mongo mongoClient = new Mongo("localhost", 27018);
        DBCollection collection = database.getCollection("customers");
        DBCollection collection2 = database.getCollection("customers2");
        collection2.setWriteConcern(WriteConcern.ACKNOWLEDGED);
        Class<?> mongoDatabaseImpl = Class.forName("com.mongodb.DBCollection");

        //insertComplexBsonValueData(verifier, collection, mongoDatabaseImpl, "customers", "MAJORITY");
        insertData(verifier, collection, mongoDatabaseImpl, "customers", "MAJORITY");
        insertData(verifier, collection2, mongoDatabaseImpl, "customers2", secondCollectionDefaultOption);
        updateData(verifier, collection, mongoDatabaseImpl);
        readData(verifier, collection, mongoDatabaseImpl);
        filterData(verifier, collection, mongoDatabaseImpl);
        filterData2(verifier, collection, mongoDatabaseImpl);
        deleteData(verifier, collection, mongoDatabaseImpl);

        stopDB();
    }/*

    public void insertComplexBsonValueData(PluginTestVerifier verifier, DBCollection collection, Class<?> mongoDatabaseImpl, String collectionInfo, String collectionOption) {
        //insert Data
        BsonValue a = new BsonString("stest");
        BsonValue b = new BsonDouble(111);
        BsonValue c = new BsonBoolean(true);

        Document document = new Document()
                .append("int32", new BsonInt32(12))
                .append("int64", new BsonInt64(77L))
                .append("bo\"olean", new BsonBoolean(true))
                .append("date", new BsonDateTime(new Date().getTime()))
                .append("double", new BsonDouble(12.3))
                .append("string", new BsonString("pinpoint"))
                .append("objectId", new BsonObjectId(new ObjectId()))
                .append("code", new BsonJavaScript("int i = 10;"))
                .append("codeWithScope", new BsonJavaScriptWithScope("int x = y", new BsonDocument("y", new BsonInt32(1))))
                .append("regex", new BsonRegularExpression("^test.*regex.*xyz$", "big"))
                .append("symbol", new BsonSymbol("wow"))
                .append("timestamp", new BsonTimestamp(0x12345678, 5))
                .append("undefined", new BsonUndefined())
                .append("binary1", new BsonBinary(new byte[]{(byte) 0xe0, 0x4f, (byte) 0xd0, 0x20}))
                .append("oldBinary", new BsonBinary(BsonBinarySubType.OLD_BINARY, new byte[]{1, 1, 1, 1, 1}))
                .append("arrayInt", new BsonArray(Arrays.asList(a, b, c, new BsonInt32(7))))
                .append("document", new BsonDocument("a", new BsonInt32(77)))
                .append("dbPointer", new BsonDbPointer("db.coll", new ObjectId()))
                .append("null", new BsonNull())
                .append("decimal128", new BsonDecimal128(new Decimal128(55)));

        //collection.insert(document);

        Object[] objects = new Object[1];
        objects[0] = document;

        NormalizedBson parsedBson = MongoUtil.parseBson(objects, true);

        Method insertOne;
        try {
            insertOne = mongoDatabaseImpl.getDeclaredMethod("insert", Object.class);
        } catch (NoSuchMethodException e) {
            insertOne = null;
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, insertOne, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), collectionInfo)
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), collectionOption)
                , new ExpectedAnnotation(MongoConstants.MONGO_JSON_DATA.getName(), new StringStringValue(parsedBson.getNormalizedBson(), parsedBson.getParameter()))));
    }*/

    public void insertData(PluginTestVerifier verifier, DBCollection collection, Class<?> mongoDatabaseImpl, String collectionInfo, String collectionOption) {
        //insert Data
        //Document doc = new Document("name", "Roy").append("company", "Naver");
        DBObject doc = new BasicDBObject("name", "Roy").append("company", "Naver");
        collection.insert(doc);

        Object[] objects = new Object[1];
        objects[0] = doc;

        //NormalizedBson parsedBson = MongoUtil.parseBson(objects, true);

        Method insertOne;
        try {
            insertOne = mongoDatabaseImpl.getDeclaredMethod("insert", DBObject[].class);
        } catch (NoSuchMethodException e) {
            insertOne = null;
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, insertOne, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), collectionInfo)
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), collectionOption)));
    }

    public void updateData(PluginTestVerifier verifier, DBCollection collection, Class<?> mongoDatabaseImpl) {
        //update Data
        DBObject doc = new BasicDBObject("name", "Roy").append("company", "Naver");
        DBObject doc2 = new BasicDBObject("$set", new BasicDBObject("name", "Roy3"));
        collection.update(doc, doc2);

        Object[] objects = new Object[2];
        objects[0] = doc;
        objects[1] = doc2;

        //NormalizedBson parsedBson = MongoUtil.parseBson(objects, true);

        Method updateOne;
        try {
            updateOne = mongoDatabaseImpl.getDeclaredMethod("update", DBObject.class, DBObject.class);
        } catch (NoSuchMethodException e) {
            updateOne = null;
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, updateOne, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), "customers")
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), "MAJORITY")));
    }


    public void readData(PluginTestVerifier verifier, DBCollection collection, Class<?> mongoDatabaseImpl) {
        //read data
        Iterator<DBObject> cursor = collection.find().iterator();

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

        while (cursor.hasNext()) {
            resultCount++;
            cursor.next();
        }

        Assert.assertEquals(2, resultCount);
    }

    public void deleteData(PluginTestVerifier verifier, DBCollection collection, Class<?> mongoDatabaseImpl) {
        //delete data
        DBObject doc = new BasicDBObject("name", "Roy3");
        Object[] objects = new Object[1];
        objects[0] = doc;

        //NormalizedBson parsedBson = MongoUtil.parseBson(objects, true);

        WriteResult result = collection.remove(doc);

        Method deleteMany;
        try {
            deleteMany = mongoDatabaseImpl.getDeclaredMethod("deleteMany", Bson.class);
        } catch (NoSuchMethodException e) {
            deleteMany = null;
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, deleteMany, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), "customers")
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), "MAJORITY")));

        Assert.assertEquals(1, result.getN());
    }

    public void filterData(PluginTestVerifier verifier, DBCollection collection, Class<?> mongoDatabaseImpl) {

        DBObject doc = new BasicDBObject("name", "Roy3");
        Object[] objects = new Object[1];
        objects[0] = doc;

        //NormalizedBson parsedBson = MongoUtil.parseBson(objects, true);

        Iterator<DBObject> iterator = collection.find(doc).iterator();

        Method find;
        try {
            find = mongoDatabaseImpl.getDeclaredMethod("find", Bson.class);
        } catch (NoSuchMethodException e) {
            find = null;
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, find, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), "customers")
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), "secondaryPreferred")));

        int resultCount = 0;

        while (iterator.hasNext()) {
            resultCount++;
            iterator.next();
        }

        Assert.assertEquals(1, resultCount);
    }

    public void filterData2(PluginTestVerifier verifier, DBCollection collection, Class<?> mongoDatabaseImpl) {

        DBObject doc = new BasicDBObject("name", "Roy3");

        Bson bson = and(exists("name"), nin("name", 5, 15));
        Object[] objects = new Object[1];
        objects[0] = bson;

        //NormalizedBson parsedBson = MongoUtil.parseBson(objects, true);

        Method find;
        try {
            find = mongoDatabaseImpl.getDeclaredMethod("find", Bson.class);
        } catch (NoSuchMethodException e) {
            find = null;
        }

        Iterator<DBObject> cursor = collection.find(doc).iterator();

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, find, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), "customers")
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), "secondaryPreferred")));

        int resultCount = 0;

        while (cursor.hasNext()) {
            resultCount++;
            cursor.next();
        }

        Assert.assertEquals(1, resultCount);
    }
}
