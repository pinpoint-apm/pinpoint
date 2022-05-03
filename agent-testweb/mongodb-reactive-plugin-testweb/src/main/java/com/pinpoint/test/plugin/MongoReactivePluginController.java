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

package com.pinpoint.test.plugin;

import com.mongodb.MongoTimeoutException;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.bson.Document;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import static java.util.concurrent.TimeUnit.SECONDS;

@RestController
public class MongoReactivePluginController {
    private static final int PORT = 27017;
    private MongodExecutable mongodExecutable;
    private MongodProcess mongod;

    @PostConstruct
    private void start() throws Exception {
        MongodStarter starter = MongodStarter.getDefaultInstance();

        MongodConfig mongodConfig = MongodConfig.builder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(PORT, Network.localhostIsIPv6()))
                .build();

        this.mongodExecutable = starter.prepare(mongodConfig);
        this.mongod = mongodExecutable.start();
    }

    @PreDestroy
    private void shutdown() {
        if (mongod != null) {
            mongod.stop();
        }
        if (mongodExecutable != null) {
            mongodExecutable.stop();
        }
    }

    @GetMapping(value = "/mongodb/insertOne")
    public String insertOn() throws Throwable {
        MongoDatabase db = getDatabase();

        MongoCollection<Document> collection = db.getCollection("test");
        Document canvas = new Document("item", "canvas")
                .append("qty", 100)
                .append("tags", Collections.singletonList("cotton"));

        Document size = new Document("h", 28)
                .append("w", 35.5)
                .append("uom", "cm");
        canvas.put("size", size);

        ObservableSubscriber<InsertOneResult> sub = new ObservableSubscriber<>();
        collection.insertOne(canvas).subscribe(sub);
        sub.waitForThenCancel(1);

        return "Insert=" + sub.getResults();
    }

    private MongoDatabase getDatabase() {
        String uri = "mongodb://localhost:" + PORT;
        MongoClient mongo = MongoClients.create(uri);
        MongoDatabase db = mongo.getDatabase("test");

        return db;
    }

    private static <T> void subscribeAndAwait(final Publisher<T> publisher) throws Throwable {
        ObservableSubscriber<T> subscriber = new ObservableSubscriber<>(false);
        publisher.subscribe(subscriber);
        subscriber.await();
    }

    private static class ObservableSubscriber<T> implements Subscriber<T> {
        private static final AtomicIntegerFieldUpdater<ObservableSubscriber> COUNT_UPDATER
                = AtomicIntegerFieldUpdater.newUpdater(ObservableSubscriber.class, "counter");
        private final CountDownLatch latch;
        private final List<T> results = new ArrayList<T>();
        private final boolean printResults;

        private volatile int minimumNumberOfResults;
        private volatile int counter;
        private volatile Subscription subscription;
        private volatile Throwable error;

        public ObservableSubscriber() {
            this(true);
        }

        public ObservableSubscriber(final boolean printResults) {
            this.printResults = printResults;
            this.latch = new CountDownLatch(1);
        }

        @Override
        public void onSubscribe(final Subscription s) {
            subscription = s;
            subscription.request(Integer.MAX_VALUE);
        }

        @Override
        public void onNext(final T t) {
            results.add(t);
            if (printResults) {
                System.out.println(t);
            }
            final int i = COUNT_UPDATER.incrementAndGet(this);
            if (i >= minimumNumberOfResults) {
                latch.countDown();
            }
        }

        @Override
        public void onError(final Throwable t) {
            error = t;
            System.out.println(t.getMessage());
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
