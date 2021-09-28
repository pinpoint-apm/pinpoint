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

import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.plugin.mongo.MongoConstants;
import com.navercorp.pinpoint.plugin.mongo.MongoUtil;
import com.navercorp.pinpoint.plugin.mongo.NormalizedBson;

import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
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

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Filters.nin;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author Roy Kim
 */
public abstract class MongoDBITBase {

    protected static final String MONGO_EXECUTE_QUERY = "MONGO_EXECUTE_QUERY";
    public static MongoDatabase database;
    public static String secondCollectionDefaultOption = "ACKNOWLEDGED";
    MongodProcess mongod;

    public abstract void setClient();

    public abstract void closeClient();

    abstract Class<?> getMongoDatabaseClazz() throws ClassNotFoundException;

    abstract void insertComplex(PluginTestVerifier verifier, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl, String collectionInfo, String collectionOption);

    public void startDB() throws Exception {
        MongodStarter starter = MongodStarter.getDefaultInstance();
        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(MongoDBITConstants.BIND_ADDRESS, MongoDBITConstants.PORT, Network.localhostIsIPv6()))
                .build();

        MongodExecutable mongodExecutable = starter.prepare(mongodConfig);

//        replaced via awaitCompleted()
//        //give time for previous DB close to finish and port to be released"
//        Thread.sleep(200L);

        mongod = mongodExecutable.start();
        setClient();
    }

    public void stopDB(MongoCollection<Document> collection) throws Exception {
        try {
            collection.drop();
        } catch (Exception e) {
        }

//        replaced via awaitCompleted()
//        //give time for the test to finish"
//        Thread.sleep(100L);

        closeClient();
        mongod.stop();

        awaitCompleted();
    }

    private void awaitCompleted() throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            if (!mongod.isProcessRunning()) {
                break;
            }
            Thread.sleep(10);
        }
    }

    @Test
    public void testConnection() throws Exception {
        startDB();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        MongoCollection<Document> collection = database.getCollection("customers");
        MongoCollection<Document> collection2 = database.getCollection("customers2").withWriteConcern(WriteConcern.ACKNOWLEDGED);

        Class<?> mongoDatabaseImpl = getMongoDatabaseClazz();

        insertComplex(verifier, collection, mongoDatabaseImpl, "customers", "MAJORITY");
        insertData(verifier, collection, mongoDatabaseImpl, "customers", "MAJORITY");
        insertData(verifier, collection2, mongoDatabaseImpl, "customers2", secondCollectionDefaultOption);
        updateData(verifier, collection, mongoDatabaseImpl);
        readData(verifier, collection, mongoDatabaseImpl);
        filterData(verifier, collection, mongoDatabaseImpl);
        filterData2(verifier, collection, mongoDatabaseImpl);
        deleteData(verifier, collection, mongoDatabaseImpl);

        stopDB(collection);
    }

    public void insertComlexBsonValueData30(PluginTestVerifier verifier, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl, String collectionInfo, String collectionOption) {
        //insert Data
        Document document = createComplexDocument();

        collection.insertOne(document);

        Method insertOneMethod = getMethod(mongoDatabaseImpl, "insertOne", Object.class);
        NormalizedBson parsedBson = parseBson(document);

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, insertOneMethod, null, MongoDBITConstants.MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), collectionInfo)
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), collectionOption)
                , new ExpectedAnnotation(MongoConstants.MONGO_JSON_DATA.getName(), new StringStringValue(parsedBson.getNormalizedBson(), parsedBson.getParameter()))));
    }

    public NormalizedBson parseBson(Object... documents) {
        Object[] objects = new Object[documents.length];
        for (int i = 0; i < documents.length; i++) {
            objects[i] = documents[i];
        }
        return MongoUtil.parseBson(objects, true);
    }

    private Document createComplexDocument() {
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
        return document;
    }

    private Method getMethod(Class<?> mongoDatabaseImpl, String name, Class<?>... parameterTypes) {
        Method method = null;
        try {
            method = mongoDatabaseImpl.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
        }
        return method;
    }

    public void insertComlexBsonValueData34(PluginTestVerifier verifier, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl, String collectionInfo, String collectionOption) {
        //insert Data
        Document document = createComplexDocument();
        document.append("decimal128", new BsonDecimal128(new Decimal128(55)));

        collection.insertOne(document);

        Method insertOneMethod = getMethod(mongoDatabaseImpl, "insertOne", Object.class);
        NormalizedBson parsedBson = parseBson(document);

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, insertOneMethod, null, MongoDBITConstants.MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), collectionInfo)
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), collectionOption)
                , new ExpectedAnnotation(MongoConstants.MONGO_JSON_DATA.getName(), new StringStringValue(parsedBson.getNormalizedBson(), parsedBson.getParameter()))));
    }

    public void insertData(PluginTestVerifier verifier, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl, String collectionInfo, String collectionOption) {
        //insert Data
        Document doc = new Document("name", "Roy").append("company", "Naver");
        collection.insertOne(doc);

        Method insertOneMethod = getMethod(mongoDatabaseImpl, "insertOne", Object.class);
        NormalizedBson parsedBson = parseBson(doc);

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, insertOneMethod, null, MongoDBITConstants.MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), collectionInfo)
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), collectionOption)
                , new ExpectedAnnotation(MongoConstants.MONGO_JSON_DATA.getName(), new StringStringValue(parsedBson.getNormalizedBson(), parsedBson.getParameter()))));
    }

    public void updateData(PluginTestVerifier verifier, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl) {
        //update Data
        Document doc = new Document("name", "Roy").append("company", "Naver");
        Document doc2 = new Document("$set", new Document("name", "Roy3"));
        collection.updateOne(doc, doc2);

        Method updateOne = getMethod(mongoDatabaseImpl, "updateOne", Bson.class, Bson.class);
        NormalizedBson parsedBson = parseBson(doc, doc2);

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, updateOne, null, MongoDBITConstants.MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), "customers")
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), "MAJORITY")
                , new ExpectedAnnotation(MongoConstants.MONGO_JSON_DATA.getName(), new StringStringValue(parsedBson.getNormalizedBson(), parsedBson.getParameter()))));
    }


    public void readData(PluginTestVerifier verifier, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl) {
        //read data
        MongoCursor<Document> cursor = collection.find().iterator();

        Method find = getMethod(mongoDatabaseImpl, "find");

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, find, null, MongoDBITConstants.MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), "customers")
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), "secondaryPreferred")));

        assertResultSize(2, cursor);
    }

    private void assertResultSize(int expected, MongoCursor<Document> cursor) {
        int resultCount = 0;
        try {
            while (cursor.hasNext()) {
                resultCount++;
                cursor.next();
            }
        } finally {
            cursor.close();
        }
        Assert.assertEquals(expected, resultCount);
    }

    public void deleteData(PluginTestVerifier verifier, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl) {
        //delete data
        Document doc = new Document("name", "Roy3");
        DeleteResult deleteResult = collection.deleteMany(doc);

        Method deleteMany = getMethod(mongoDatabaseImpl, "deleteMany", Bson.class);
        NormalizedBson parsedBson = parseBson(doc);

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, deleteMany, null, MongoDBITConstants.MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), "customers")
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), "MAJORITY")
                , new ExpectedAnnotation(MongoConstants.MONGO_JSON_DATA.getName(), new StringStringValue(parsedBson.getNormalizedBson(), parsedBson.getParameter()))));

        Assert.assertEquals(1, deleteResult.getDeletedCount());
    }

    public void filterData(PluginTestVerifier verifier, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl) {
        Method find = getMethod(mongoDatabaseImpl, "find", Bson.class);
        Bson bson = eq("name", "Roy3");
        NormalizedBson parsedBson = parseBson(bson);

        MongoCursor<Document> cursor = collection.find(bson).iterator();

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, find, null, MongoDBITConstants.MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), "customers")
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), "secondaryPreferred")
                , new ExpectedAnnotation(MongoConstants.MONGO_JSON_DATA.getName(), new StringStringValue(parsedBson.getNormalizedBson(), parsedBson.getParameter()))));

        assertResultSize(1, cursor);
    }

    public void filterData2(PluginTestVerifier verifier, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl) {
        Method find = getMethod(mongoDatabaseImpl, "find", Bson.class);
        Bson bson = and(exists("name"), nin("name", 5, 15));
        NormalizedBson parsedBson = parseBson(bson);

        MongoCursor<Document> cursor = collection.find(bson).iterator();

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, find, null, MongoDBITConstants.MONGODB_ADDRESS, null
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), "customers")
                , new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), "secondaryPreferred")
                , new ExpectedAnnotation(MongoConstants.MONGO_JSON_DATA.getName(), new StringStringValue(parsedBson.getNormalizedBson(), parsedBson.getParameter()))));

        assertResultSize(1, cursor);
    }
}
