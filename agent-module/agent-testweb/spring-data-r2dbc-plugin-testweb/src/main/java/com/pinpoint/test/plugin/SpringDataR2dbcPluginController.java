/*
 * Copyright 2020 NAVER Corp.
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

import io.r2dbc.spi.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RestController
public class SpringDataR2dbcPluginController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    R2dbcDatabase r2dbcDatabase;

    public SpringDataR2dbcPluginController(@Qualifier("mysql") R2dbcDatabase r2dbcDatabase) {
        this.r2dbcDatabase = r2dbcDatabase;
    }

    @GetMapping("/template/insert")
    public Mono<Map<String, Object>> insert() throws SQLException {
        DatabaseClient databaseClient = new R2dbcEntityTemplate(r2dbcDatabase.getConnectionFactory()).getDatabaseClient();
        final String firstName = Long.toHexString(System.currentTimeMillis());
        final String lastName = "foo";

        return databaseClient
                .sql("INSERT INTO  persons (first_name, last_name, age) VALUES (:first_name, :last_name, :age)")
                .bind("first_name", firstName)
                .bind("last_name", lastName)
                .bind("age", 25)
                .fetch()
                .first()
                .publishOn(Schedulers.single());
    }

    @GetMapping("/template/select")
    public Flux<Map<String, Object>> select() throws SQLException {
        DatabaseClient databaseClient = new R2dbcEntityTemplate(r2dbcDatabase.getConnectionFactory()).getDatabaseClient();
        return databaseClient
                .sql("SELECT * FROM persons")
                .fetch()
                .all()
                .subscribeOn(Schedulers.parallel());
    }

    @GetMapping("/repository/insert")
    public Mono<String> repositoryInsert() throws Throwable {
        PersonRepository repository = new PersonRepository(new R2dbcEntityTemplate(r2dbcDatabase.getConnectionFactory()));
        Person person = new Person("foo", "bar", 30);
        return repository.save(person);
    }

    @GetMapping("/connection/select")
    public List<String> connectionSelect() throws Throwable {
        Publisher<? extends Connection> conn = r2dbcDatabase.getConnectionFactory().create();
        final ObservableSubscriber<String> subscriber = new ObservableSubscriber();
        Mono.from(conn)
                .flatMapMany(
                        c -> Flux.from(c.createStatement("SELECT * FROM persons")
                                .execute())
                ).flatMap(result -> result.map(((row, rowMetadata) -> row.get("first_name", String.class)))
        ).subscribe(subscriber);
        subscriber.await();
        return subscriber.getReceived();
    }

    public static class ObservableSubscriber<T> implements Subscriber<T> {
        private final List<T> received;
        private final List<Throwable> errors;
        private final CountDownLatch latch;
        private volatile Subscription subscription;
        private volatile boolean completed;

        ObservableSubscriber() {
            this.received = new ArrayList<T>();
            this.errors = new ArrayList<Throwable>();
            this.latch = new CountDownLatch(1);
        }

        @Override
        public void onSubscribe(final Subscription s) {
            subscription = s;
        }

        @Override
        public void onNext(final T t) {
            received.add(t);
        }

        @Override
        public void onError(final Throwable t) {
            errors.add(t);
            onComplete();
        }

        @Override
        public void onComplete() {
            completed = true;
            latch.countDown();
        }

        public Subscription getSubscription() {
            return subscription;
        }

        public List<T> getReceived() {
            return received;
        }

        public Throwable getError() {
            if (errors.size() > 0) {
                return errors.get(0);
            }
            return null;
        }

        public boolean isCompleted() {
            return completed;
        }

        public List<T> get(final long timeout, final TimeUnit unit) throws Throwable {
            return await(timeout, unit).getReceived();
        }

        public ObservableSubscriber<T> await() throws Throwable {
            return await(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        }

        public ObservableSubscriber<T> await(final long timeout, final TimeUnit unit) throws Throwable {
            subscription.request(Integer.MAX_VALUE);
            if (!latch.await(timeout, unit)) {
                throw new RuntimeException("Publisher onComplete timed out");
            }
            if (!errors.isEmpty()) {
                throw errors.get(0);
            }
            return this;
        }
    }
}
