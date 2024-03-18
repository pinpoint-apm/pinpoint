/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.it.plugin.mongodb4;

import com.mongodb.MongoTimeoutException;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.internal.operation.AsyncReadOperation;
import com.mongodb.internal.operation.AsyncWriteOperation;
import com.mongodb.reactivestreams.client.ClientSession;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.reactivestreams.client.internal.OperationExecutorImpl;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedNotNull;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
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
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Filters.nin;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ReactiveMongoDBITHelper {
    protected static final String MONGO_REACTIVE = "MONGO_REACTIVE";
    protected static final String MONGO_EXECUTE_QUERY = "MONGO_EXECUTE_QUERY";

    public void testConnection(String address, final MongoDatabase database, Class<?> mongoDatabaseImplClass, String secondCollectionDefaultOption) throws Exception {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        MongoCollection<Document> collection = database.getCollection("customers");
        MongoCollection<Document> collection2 = database.getCollection("customers2").withWriteConcern(WriteConcern.ACKNOWLEDGED);

        insertComlexBsonValueData34(verifier, address, collection, mongoDatabaseImplClass, "customers", "MAJORITY");
        insertData(verifier, address, collection, mongoDatabaseImplClass, "customers", "MAJORITY");
        insertData(verifier, address, collection2, mongoDatabaseImplClass, "customers2", secondCollectionDefaultOption);
        updateData(verifier, address, collection, mongoDatabaseImplClass);
        readData(verifier, address, collection, mongoDatabaseImplClass);
        filterData(verifier, address, collection, mongoDatabaseImplClass);
        filterData2(verifier, address, collection, mongoDatabaseImplClass);
        deleteData(verifier, address, collection, mongoDatabaseImplClass);

        stopDB(collection);
    }

    public void stopDB(MongoCollection<Document> collection) {
        try {
            ObservableSubscriber<Void> sub = new ObservableSubscriber<>();
            collection.drop().subscribe(sub);
            sub.waitForThenCancel(1);
        } catch (Throwable t) {
            throw new RuntimeException("drop() failure", t);
        }
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
        try {
            return mongoDatabaseImpl.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void insertComlexBsonValueData34(PluginTestVerifier verifier, String address, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl, String collectionInfo, String collectionOption) throws Exception {
        //insert Data
        Document document = createComplexDocument();
        document.append("decimal128", new BsonDecimal128(new Decimal128(55)));

        ObservableSubscriber<InsertOneResult> sub = new ObservableSubscriber<>();
        collection.insertOne(document).subscribe(sub);
        try {
            sub.waitForThenCancel(1);
        } catch (Throwable ignored) {
        }

        Method insertOneMethod = getMethod(mongoDatabaseImpl, "insertOne", Object.class);

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, insertOneMethod, null, address, null
                , new ExpectedAnnotation("Collection-Info", collectionInfo)
                , new ExpectedAnnotation("Collection-Option", collectionOption)
                , new ExpectedNotNull("MONGO-JSON-Data")));

        Method executeMethod = OperationExecutorImpl.class.getDeclaredMethod("execute", AsyncWriteOperation.class, ReadConcern.class, ClientSession.class);
        // execute
        verifier.verifyTrace(event(MONGO_REACTIVE, executeMethod));
    }

    public void insertData(PluginTestVerifier verifier, String address, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl, String collectionInfo, String collectionOption) throws Exception {
        //insert Data
        Document doc = new Document("name", "Roy").append("company", "Naver");
        ObservableSubscriber<InsertOneResult> sub = new ObservableSubscriber<>();
        collection.insertOne(doc).subscribe(sub);
        try {
            sub.waitForThenCancel(1);
        } catch (Throwable ignored) {
        }

        Method insertOneMethod = getMethod(mongoDatabaseImpl, "insertOne", Object.class);

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, insertOneMethod, null, address, null
                , new ExpectedAnnotation("Collection-Info", collectionInfo)
                , new ExpectedAnnotation("Collection-Option", collectionOption)
                , new ExpectedNotNull("MONGO-JSON-Data")));

        Method executeMethod = OperationExecutorImpl.class.getDeclaredMethod("execute", AsyncWriteOperation.class, ReadConcern.class, ClientSession.class);
        verifier.verifyTrace(event(MONGO_REACTIVE, executeMethod));
    }

    public void updateData(PluginTestVerifier verifier, String address, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl) throws Exception {
        //update Data
        Document doc = new Document("name", "Roy").append("company", "Naver");
        Document doc2 = new Document("$set", new Document("name", "Roy3"));
        ObservableSubscriber<UpdateResult> sub = new ObservableSubscriber<>();
        collection.updateOne(doc, doc2).subscribe(sub);
        try {
            sub.waitForThenCancel(1);
        } catch (Throwable ignored) {
        }

        Method updateOne = getMethod(mongoDatabaseImpl, "updateOne", Bson.class, Bson.class);

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, updateOne, null, address, null
                , new ExpectedAnnotation("Collection-Info", "customers")
                , new ExpectedAnnotation("Collection-Option", "MAJORITY")
                , new ExpectedNotNull("MONGO-JSON-Data")));

        Method executeMethod = OperationExecutorImpl.class.getDeclaredMethod("execute", AsyncWriteOperation.class, ReadConcern.class, ClientSession.class);
        verifier.verifyTrace(event(MONGO_REACTIVE, executeMethod));
    }

    public void readData(PluginTestVerifier verifier, String address, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl) throws Exception {
        //read data
        ObservableSubscriber<Document> sub = new ObservableSubscriber<>();
        collection.find().subscribe(sub);
        try {
            sub.waitForThenCancel(2);
        } catch (Throwable ignored) {
        }

        Method find = getMethod(mongoDatabaseImpl, "find");

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, find, null, address, null
                , new ExpectedAnnotation("Collection-Info", "customers")
                , new ExpectedAnnotation("Collection-Option", "secondaryPreferred")));

        Method executeMethod = OperationExecutorImpl.class.getDeclaredMethod("execute", AsyncReadOperation.class, ReadPreference.class, ReadConcern.class, ClientSession.class);
        verifier.verifyTrace(event(MONGO_REACTIVE, executeMethod));
    }


    public void deleteData(PluginTestVerifier verifier, String address, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl) throws Exception {
        //delete data
        Document doc = new Document("name", "Roy3");
        ObservableSubscriber<DeleteResult> sub = new ObservableSubscriber<>();
        collection.deleteMany(doc).subscribe(sub);
        try {
            sub.waitForThenCancel(1);
        } catch (Throwable ignored) {
        }

        Method deleteMany = getMethod(mongoDatabaseImpl, "deleteMany", Bson.class);

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, deleteMany, null, address, null
                , new ExpectedAnnotation("Collection-Info", "customers")
                , new ExpectedAnnotation("Collection-Option", "MAJORITY")
                , new ExpectedNotNull("MONGO-JSON-Data")));

        Method executeMethod = OperationExecutorImpl.class.getDeclaredMethod("execute", AsyncWriteOperation.class, ReadConcern.class, ClientSession.class);
        verifier.verifyTrace(event(MONGO_REACTIVE, executeMethod));
    }

    public void filterData(PluginTestVerifier verifier, String address, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl) throws Exception {
        Method find = getMethod(mongoDatabaseImpl, "find", Bson.class);
        Bson bson = eq("name", "Roy3");

        ObservableSubscriber<Document> sub = new ObservableSubscriber<>();
        collection.find(bson).subscribe(sub);
        try {
            sub.waitForThenCancel(1);
        } catch (Throwable ignored) {
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, find, null, address, null
                , new ExpectedAnnotation("Collection-Info", "customers")
                , new ExpectedAnnotation("Collection-Option", "secondaryPreferred")
                , new ExpectedNotNull("MONGO-JSON-Data")));

        Method executeMethod = OperationExecutorImpl.class.getDeclaredMethod("execute", AsyncReadOperation.class, ReadPreference.class, ReadConcern.class, ClientSession.class);
        verifier.verifyTrace(event(MONGO_REACTIVE, executeMethod));
    }

    public void filterData2(PluginTestVerifier verifier, String address, MongoCollection<Document> collection, Class<?> mongoDatabaseImpl) throws Exception {
        Method find = getMethod(mongoDatabaseImpl, "find", Bson.class);
        Bson bson = and(exists("name"), nin("name", 5, 15));

        ObservableSubscriber<Document> sub = new ObservableSubscriber<>();
        collection.find(bson).subscribe(sub);
        try {
            sub.waitForThenCancel(1);
        } catch (Throwable ignored) {
        }

        verifier.verifyTrace(event(MONGO_EXECUTE_QUERY, find, null, address, null
                , new ExpectedAnnotation("Collection-Info", "customers")
                , new ExpectedAnnotation("Collection-Option", "secondaryPreferred")
                , new ExpectedNotNull("MONGO-JSON-Data")));

        Method executeMethod = OperationExecutorImpl.class.getDeclaredMethod("execute", AsyncReadOperation.class, ReadPreference.class, ReadConcern.class, ClientSession.class);
        verifier.verifyTrace(event(MONGO_REACTIVE, executeMethod));
    }

    private static class ObservableSubscriber<T> implements Subscriber<T> {
        private static final AtomicIntegerFieldUpdater<ObservableSubscriber> COUNT_UPDATER
                = AtomicIntegerFieldUpdater.newUpdater(ObservableSubscriber.class, "counter");

        private final CountDownLatch latch;
        private final List<T> results = new ArrayList<T>();

        private volatile int minimumNumberOfResults;
        private volatile int counter;
        private volatile Subscription subscription;
        private volatile Throwable error;

        public ObservableSubscriber() {
            this.latch = new CountDownLatch(1);
        }

        @Override
        public void onSubscribe(Subscription s) {
            subscription = s;
            subscription.request(Integer.MAX_VALUE);
        }

        @Override
        public void onNext(T t) {
            results.add(t);

            final int i = COUNT_UPDATER.incrementAndGet(this);
            if (i >= minimumNumberOfResults) {
                latch.countDown();
            }
        }

        @Override
        public void onError(Throwable throwable) {
            error = throwable;
            onComplete();
        }

        @Override
        public void onComplete() {
            latch.countDown();
        }

        public List<T> getResults() {
            return results;
        }

        public void await() throws Throwable {
            if (!latch.await(10, SECONDS)) {
                throw new MongoTimeoutException("Publisher timed out");
            }
            if (error != null) {
                throw error;
            }
        }

        public void waitForThenCancel(final int minimumNumberOfResults) throws Throwable {
            this.minimumNumberOfResults = minimumNumberOfResults;
            if (minimumNumberOfResults > COUNT_UPDATER.get(this)) {
                await();
            }
            subscription.cancel();
        }
    }
}
