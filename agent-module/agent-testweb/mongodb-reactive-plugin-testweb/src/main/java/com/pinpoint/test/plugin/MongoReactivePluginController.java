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
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.pinpoint.test.common.view.ApiLinkPage;
import com.pinpoint.test.common.view.HrefTag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import static java.util.concurrent.TimeUnit.SECONDS;

@RestController
public class MongoReactivePluginController {

    @Autowired
    MongoTemplate mongo;
    @Autowired
    ReactiveMongoTemplate reactiveMongo;

    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public MongoReactivePluginController(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @GetMapping("/")
    String welcome() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = this.handlerMapping.getHandlerMethods();
        List<HrefTag> list = new ArrayList<>();
        for (RequestMappingInfo info : handlerMethods.keySet()) {
            for (String path : info.getDirectPaths()) {
                list.add(HrefTag.of(path));
            }
        }
        list.sort(Comparator.comparing(HrefTag::getPath));
        return new ApiLinkPage("mongodb-reactive-plugin-testweb")
                .addHrefTag(list)
                .build();
    }

    @GetMapping(value = "/insertOne")
    public String insertOn() throws Throwable {
        MongoDatabase db = (MongoDatabase) mongo.getDb();

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

    @GetMapping("/save")
    Demo save() {
        Demo demo = new Demo();
        demo.setName("foo");
        return mongo.save(demo);
    }

    @GetMapping("/find")
    Demo find() {
        return mongo.findById(1L, Demo.class);
    }

    @GetMapping("/findOne")
    Demo findOne() {
        return mongo.findOne(Query.query(Criteria.where("name").is("foo")), Demo.class);
    }

    @GetMapping("/findAll")
    List<Demo> findAll() {
        return mongo.findAll(Demo.class);
    }

    @GetMapping("/aggregate")
    List<Demo> aggregate() {
        return mongo.aggregate(Aggregation.newAggregation(Aggregation.project("_id")), Demo.class, Demo.class)
                .getMappedResults();
    }

    @GetMapping("/reactiveFindAll")
    Mono<List<Demo>> reactiveFindAll() {
        return reactiveMongo.findAll(Demo.class).collectList();
    }

    @GetMapping("/reactiveAggregate")
    Mono<List<Demo>> reactiveAggregate() {
        return reactiveMongo.aggregate(Aggregation.newAggregation(Aggregation.project("_id")), Demo.class, Demo.class)
                .collectList();
    }

    @GetMapping("/reactiveFind")
    Flux<Demo> reactiveFind() {
        return reactiveMongo.find(Query.query(Criteria.where("name").is("foo")), Demo.class);
    }

    private static class ObservableSubscriber<T> implements Subscriber<T> {
        private final Logger logger = LogManager.getLogger(this.getClass());

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
        public void onSubscribe(final Subscription s) {
            subscription = s;
            subscription.request(Integer.MAX_VALUE);
        }

        @Override
        public void onNext(final T t) {
            results.add(t);
            logger.info(t);

            final int i = COUNT_UPDATER.incrementAndGet(this);
            if (i >= minimumNumberOfResults) {
                latch.countDown();
            }
        }

        @Override
        public void onError(final Throwable t) {
            error = t;
            logger.info(t.getMessage());
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
