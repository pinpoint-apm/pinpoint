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

import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.plugin.mongo.MongoConstants;
import com.navercorp.pinpoint.plugin.mongo.MongoUtil;
import com.navercorp.pinpoint.plugin.mongo.NormalizedBson;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.bson.BsonArray;
import org.bson.BsonBinary;
import org.bson.BsonBinarySubType;
import org.bson.BsonBoolean;
import org.bson.BsonDateTime;
import org.bson.BsonDbPointer;
import org.bson.BsonDecimal128;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonJavaScript;
import org.bson.BsonJavaScriptWithScope;
import org.bson.BsonNull;
import org.bson.BsonObjectId;
import org.bson.BsonRegularExpression;
import org.bson.BsonString;
import org.bson.BsonSymbol;
import org.bson.BsonTimestamp;
import org.bson.BsonUndefined;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;

import static com.mongodb.client.model.Filters.*;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author Roy Kim
 */
public abstract class MongoDBITBase {

    protected static final String MONGO_EXECUTE_QUERY = "MONGO_EXECUTE_QUERY";
    public static MongoDatabase database;
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

        MongoCollection<Document> collection = database.getCollection("customers");
        MongoCollection<Document> collection2 = database.getCollection("customers2").withWriteConcern(WriteConcern.ACKNOWLEDGED);
        Class<?> mongoDatabaseImpl;

        if (version >= 3.7) {
            mongoDatabaseImpl = Class.forName("com.mongodb.client.internal.MongoCollectionImpl");
        } else {
            mongoDatabaseImpl = Class.forName("com.mongodb.MongoCollectionImpl");
        }

        if (version >= 3.4) {
            insertComlexBsonValueData34(verifier, collection, mongoDatabaseImpl, "customers", "MAJORITY");
        } else {
            insertComlexBsonValueData30(verifier, collection, mongoDatabaseImpl, "customers", "MAJORITY");
        }
        insertData(verifier, collection, mongoDatabaseImpl, "customers", "MAJORITY");
        insertData(verifier, collection2, mongoDatabaseImpl, "customers2", secondCollectionDefaultOption);
        updateData(verifier, collection, mongoDatabaseImpl);
        readData(verifier, collection, mongoDatabaseImpl);
        filterData(verifier, collection, mongoDatabaseImpl);
        filterData2(verifier, collection, mongoDatabaseImpl);
        deleteData(verifier, collection, mongoDatabaseImpl);

        stopDB();
    }

    public void insertComlexBsonValueData30(PluginTestVerifier verifier, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl, String collectionInfo, String collectionOption) {
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
                .append("null", new BsonNull());

        collection.insertOne(document);

        Object[] objects = new Object[1];
        objects[0] = document;

        NormalizedBson parsedBson = MongoUtil.parseBson(objects, true);

        Method insertOne;
        try {
            insertOne = mongoDatabaseImpl.getDeclaredMethod("insertOne", Object.class);
        } catch (NoSuchMethodException e) {
            insertOne = null;
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, insertOne, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), collectionInfo)
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), collectionOption)
                , new ExpectedAnnotation(MongoConstants.MONGO_JSON_DATA.getName(), new StringStringValue(parsedBson.getNormalizedBson(), parsedBson.getParameter()))));
    }

    public void insertComlexBsonValueData34(PluginTestVerifier verifier, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl, String collectionInfo, String collectionOption) {
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

        collection.insertOne(document);

        Object[] objects = new Object[1];
        objects[0] = document;

        NormalizedBson parsedBson = MongoUtil.parseBson(objects, true);

        Method insertOne;
        try {
            insertOne = mongoDatabaseImpl.getDeclaredMethod("insertOne", Object.class);
        } catch (NoSuchMethodException e) {
            insertOne = null;
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, insertOne, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), collectionInfo)
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), collectionOption)
                , new ExpectedAnnotation(MongoConstants.MONGO_JSON_DATA.getName(), new StringStringValue(parsedBson.getNormalizedBson(), parsedBson.getParameter()))));
    }

    public void insertData(PluginTestVerifier verifier, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl, String collectionInfo, String collectionOption) {
        //insert Data
        Document doc = new Document("name", "Roy").append("company", "Naver");
        collection.insertOne(doc);

        Object[] objects = new Object[1];
        objects[0] = doc;

        NormalizedBson parsedBson = MongoUtil.parseBson(objects, true);

        Method insertOne;
        try {
            insertOne = mongoDatabaseImpl.getDeclaredMethod("insertOne", Object.class);
        } catch (NoSuchMethodException e) {
            insertOne = null;
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, insertOne, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), collectionInfo)
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), collectionOption)
                , new ExpectedAnnotation(MongoConstants.MONGO_JSON_DATA.getName(), new StringStringValue(parsedBson.getNormalizedBson(), parsedBson.getParameter()))));
    }

    public void updateData(PluginTestVerifier verifier, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl) {

        //update Data
        Document doc = new Document("name", "Roy").append("company", "Naver");
        Document doc2 = new Document("$set", new Document("name", "Roy3"));
        collection.updateOne(doc, doc2);

        Object[] objects = new Object[2];
        objects[0] = doc;
        objects[1] = doc2;

        NormalizedBson parsedBson = MongoUtil.parseBson(objects, true);

        Method updateOne;
        try {
            updateOne = mongoDatabaseImpl.getDeclaredMethod("updateOne", Bson.class, Bson.class);
        } catch (NoSuchMethodException e) {
            updateOne = null;
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, updateOne, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), "customers")
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), "MAJORITY")
                , new ExpectedAnnotation(MongoConstants.MONGO_JSON_DATA.getName(), new StringStringValue(parsedBson.getNormalizedBson(), parsedBson.getParameter()))));
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
        Assert.assertEquals(2, resultCount);
    }

    public void deleteData(PluginTestVerifier verifier, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl) {
        //delete data
        Document doc = new Document("name", "Roy3");
        Object[] objects = new Object[1];
        objects[0] = doc;

        NormalizedBson parsedBson = MongoUtil.parseBson(objects, true);

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
                , new ExpectedAnnotation(MongoConstants.MONGO_JSON_DATA.getName(), new StringStringValue(parsedBson.getNormalizedBson(), parsedBson.getParameter()))));

        Assert.assertEquals(1, deleteResult.getDeletedCount());
    }

    public void filterData(PluginTestVerifier verifier, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl) {

        Bson bson = eq("name", "Roy3");
        Object[] objects = new Object[1];
        objects[0] = bson;

        NormalizedBson parsedBson = MongoUtil.parseBson(objects, true);

        MongoCursor<Document> cursor = collection.find(bson).iterator();

        Method find;
        try {
            find = mongoDatabaseImpl.getDeclaredMethod("find", Bson.class);
        } catch (NoSuchMethodException e) {
            find = null;
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, find, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), "customers")
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), "secondaryPreferred")
                , new ExpectedAnnotation(MongoConstants.MONGO_JSON_DATA.getName(), new StringStringValue(parsedBson.getNormalizedBson(), parsedBson.getParameter()))));

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

    public void filterData2(PluginTestVerifier verifier, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl) {

        Document doc = new Document("name", "Roy3");

        Bson bson = and(exists("name"), nin("name", 5, 15));
        Object[] objects = new Object[1];
        objects[0] = bson;

        NormalizedBson parsedBson = MongoUtil.parseBson(objects, true);

        MongoCursor<Document> cursor = collection.find(bson).iterator();

        Method find;
        try {
            find = mongoDatabaseImpl.getDeclaredMethod("find", Bson.class);
        } catch (NoSuchMethodException e) {
            find = null;
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, find, null, MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), "customers")
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), "secondaryPreferred")
                , new ExpectedAnnotation(MongoConstants.MONGO_JSON_DATA.getName(), new StringStringValue(parsedBson.getNormalizedBson(), parsedBson.getParameter()))));

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
}
