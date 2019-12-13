/*
 * Copyright 2019 Naver Corp.
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

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
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

/**
 * @author Community
 */
public abstract class MongoDBITBase_2_X {

    private static final PLogger LOGGER = PLoggerFactory.getLogger(MongoDBITBase_2_X.class);

    protected static final String MONGO_EXECUTE_QUERY = "MONGO_EXECUTE_QUERY";
    public static DB database;
    public static String secondCollectionDefaultWriteConcern = WriteConcern.MAJORITY.getWString();
    private final String defaultReadPreference = ReadPreference.secondaryPreferred().getName();
    public static double version = 0;
    protected static String MONGODB_ADDRESS = "localhost:" + 27018;
    MongodProcess mongod;

    public abstract void setClient();

    public abstract void closeClient();

    public void startDB() throws Exception {
        MongodStarter starter = MongodStarter.getDefaultInstance();

        String bindIp = "localhost";
        int port = 27018;

        IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                .net(new Net(bindIp, port, Network.localhostIsIPv6())).build();

        MongodExecutable mongodExecutable = null;

        mongodExecutable = starter.prepare(mongodConfig);

        // give time for previous DB close to finish and port to be released"
        Thread.sleep(200L);
        mongod = mongodExecutable.start();
        setClient();
    }

    public void stopDB() throws Exception {
        // give time for the test to finish"
        Thread.sleep(100L);

        closeClient();
        mongod.stop();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testConnection() throws Exception {
        startDB();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        DBCollection collection = database.getCollection("customers");
        DBCollection collection2 = database.getCollection("customers2");
        String firstCollectionWriteConcern = WriteConcern.SAFE.getWString();
        String secondCollectionWriteConcern = firstCollectionWriteConcern;
        try {
            // Class available from 2.7.0
            Class<?> dBCollectionClass = Class.forName("com.mongodb.WriteConcern");
            // Field available from 2.10.0
            Field declaredField = dBCollectionClass.getDeclaredField("ACKNOWLEDGED");
            if (declaredField != null) {
                collection.setWriteConcern(WriteConcern.ACKNOWLEDGED);
                firstCollectionWriteConcern = WriteConcern.ACKNOWLEDGED.getWString();
            }
            collection2.setWriteConcern(WriteConcern.MAJORITY);
            secondCollectionWriteConcern = secondCollectionDefaultWriteConcern;
        } catch (ClassNotFoundException e) {
            LOGGER.error("WriteConcern is not supported by the driver.");
        } catch (NoSuchFieldException e) {
            LOGGER.error(
                    "WriteConcern of type ACKNOWLEDGED is not supported by the driver. Falling back to the type SAFE");
            collection.setWriteConcern(WriteConcern.SAFE);
            collection2.setWriteConcern(WriteConcern.SAFE);
        } catch (SecurityException e) {
            LOGGER.error("Unable to inspect WriteConcern; Cause - " + e.getMessage());
        }
        Class<?> mongoDatabaseImpl = Class.forName("com.mongodb.DBCollection");

        insertData(verifier, collection, mongoDatabaseImpl, "customers", firstCollectionWriteConcern);
        insertData(verifier, collection2, mongoDatabaseImpl, "customers2", secondCollectionWriteConcern);
        updateData(verifier, collection, mongoDatabaseImpl, "customers", firstCollectionWriteConcern);
        readData(verifier, collection, mongoDatabaseImpl, "customers", defaultReadPreference);
        filterData(verifier, collection, mongoDatabaseImpl, "customers", defaultReadPreference);
        deleteData(verifier, collection, mongoDatabaseImpl, "customers", firstCollectionWriteConcern);

        stopDB();
    }

    public void insertData(PluginTestVerifier verifier, DBCollection collection, Class<?> collectionImpl,
            String collectionInfo, String collectionOption) {
        // insert Data
        // Document doc = new Document("name", "Roy").append("company", "Naver");
        DBObject doc = new BasicDBObject("name", "Roy").append("company", "Naver");
        collection.insert(doc);

        Object[] objects = new Object[1];
        objects[0] = doc;

        NormalizedBson parsedBson = MongoUtil.parseJson(objects, true);

        Method insert;
        try {
            insert = collectionImpl.getDeclaredMethod("insert", DBObject[].class);
        } catch (NoSuchMethodException e) {
            insert = null;
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, insert, null, MONGODB_ADDRESS, null,
                new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), collectionInfo),
                new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), collectionOption),
                new ExpectedAnnotation(MongoConstants.MONGO_JSON_DATA.getName(),
                        new StringStringValue(parsedBson.getNormalizedBson(), parsedBson.getParameter()))));
    }

    public void updateData(PluginTestVerifier verifier, DBCollection collection, Class<?> collectionImpl,
            String collectionInfo, String collectionOption) {
        // update Data
        DBObject doc = new BasicDBObject("name", "Roy").append("company", "Naver");
        DBObject doc2 = new BasicDBObject("$set", new BasicDBObject("name", "Roy3"));
        collection.update(doc, doc2);

        Object[] objects = new Object[2];
        objects[0] = doc;
        objects[1] = doc2;

        NormalizedBson parsedBson = MongoUtil.parseJson(objects, true);

        Method updateOne;
        try {
            updateOne = collectionImpl.getDeclaredMethod("update", DBObject.class, DBObject.class);
        } catch (NoSuchMethodException e) {
            updateOne = null;
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, updateOne, null, MONGODB_ADDRESS, null,
                new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), collectionInfo),
                new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), collectionOption),
                new ExpectedAnnotation(MongoConstants.MONGO_JSON_DATA.getName(),
                        new StringStringValue(parsedBson.getNormalizedBson(), parsedBson.getParameter()))));
    }

    public void readData(PluginTestVerifier verifier, DBCollection collection, Class<?> collectionImpl,
            String collectionInfo, String collectionOption) {
        // read data
        Iterator<DBObject> cursor = collection.find().iterator();

        List<DBObject> objects = new ArrayList<DBObject>();
        while (cursor.hasNext()) {
            objects.add(cursor.next());
        }

        Method find;
        try {
            find = collectionImpl.getDeclaredMethod("find");
        } catch (NoSuchMethodException e) {
            System.err.println("Unable to see find method on DBCollection.");
            find = null;
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, find, null, MONGODB_ADDRESS, null,
                new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), collectionInfo),
                new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), collectionOption)));

        Assert.assertEquals(1, objects.size());
    }

    public void filterData(PluginTestVerifier verifier, DBCollection collection, Class<?> collectionImpl,
            String collectionInfo, String collectionOption) {
        DBObject doc = new BasicDBObject("name", "Roy3");
        Object[] objects = new Object[1];
        objects[0] = doc;

        NormalizedBson parsedBson = MongoUtil.parseJson(objects, true);

        Iterator<DBObject> iterator = collection.find(doc).iterator();

        Method find;
        try {
            find = collectionImpl.getDeclaredMethod("find", DBObject.class);
        } catch (NoSuchMethodException e) {
            System.err.println("Unable to see find method with DBObject arg on DBCollection.");
            find = null;
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, find, null, MONGODB_ADDRESS, null,
                new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), collectionInfo),
                new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), collectionOption),
                new ExpectedAnnotation(MongoConstants.MONGO_JSON_DATA.getName(),
                        new StringStringValue(parsedBson.getNormalizedBson(), parsedBson.getParameter()))));

        int resultCount = 0;

        while (iterator.hasNext()) {
            resultCount++;
            iterator.next();
        }

        Assert.assertEquals(1, resultCount);
    }

    public void deleteData(PluginTestVerifier verifier, DBCollection collection, Class<?> collectionImpl,
            String collectionInfo, String collectionOption) {
        // delete data
        DBObject doc = new BasicDBObject("name", "Roy3");
        Object[] objects = new Object[1];
        objects[0] = doc;

        NormalizedBson parsedBson = MongoUtil.parseJson(objects, true);

        WriteResult result = collection.remove(doc);

        Method deleteMany;
        try {
            deleteMany = collectionImpl.getDeclaredMethod("remove", DBObject.class);
        } catch (NoSuchMethodException e) {
            deleteMany = null;
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, deleteMany, null, MONGODB_ADDRESS, null,
                new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_INFO.getName(), collectionInfo),
                new ExpectedAnnotation(MongoConstants.MONGO_COLLECTION_OPTION.getName(), collectionOption),
                new ExpectedAnnotation(MongoConstants.MONGO_JSON_DATA.getName(),
                        new StringStringValue(parsedBson.getNormalizedBson(), parsedBson.getParameter()))));

        Assert.assertEquals(1, result.getN());
    }
}
