/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.it.plugin.jdbc.r2dbc;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.it.plugin.utils.TestcontainersOption;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.DriverProperties;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCTestConstants;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import com.navercorp.pinpoint.test.plugin.shared.SharedDependency;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.api.PostgresqlConnection;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import test.pinpoint.plugin.r2dbc.Echo;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Seam-wrapper PoC (PINPOINT_REQ-1739): proves the async link at the postgresql
 * {@code Statement.execute()} seam holds through the wrapped publisher ALONE.
 * <p>
 * The config turns the wrapper on ({@code profiler.spring.data.r2dbc.wrap.publisher=true} — no
 * field injection at this seam) and turns reactor per-operator instrumentation OFF
 * ({@code profiler.reactor.enable=false}), so nothing else can restore the trace on the driver's
 * delivery thread. The {@code Echo.get} entrypoint invoked in {@code map()} directly downstream of
 * the wrapped publisher must therefore be recorded inside the async trace created at the seam;
 * if the wrapper fails, Echo lands in a standalone trace and the async assertion fails.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-r2dbc-seam-wrap.config")
@Dependency({"org.postgresql:r2dbc-postgresql:[0.8.12.RELEASE],[0.9.1.RELEASE]",
        "org.postgresql:postgresql:9.4.1207",
        "org.springframework.data:spring-data-r2dbc:1.5.1",
        PluginITConstants.VERSION, JDBCTestConstants.VERSION})
@SharedDependency({"org.postgresql:postgresql:42.3.2", PluginITConstants.VERSION, JDBCTestConstants.VERSION, TestcontainersOption.TEST_CONTAINER, TestcontainersOption.POSTGRESQL})
@SharedTestLifeCycleClass(PostgreSqlServer.class)
public class R2dbcPostgresqlSeamWrap_IT extends SqlBase {

    private static final String SELECT_QUERY = "SELECT first_name, last_name, age FROM persons";
    private static final long AWAIT_UNIT_MILLIS = 100;
    private static final long AWAIT_MAX_MILLIS = 5000;

    @Test
    public void wrappedExecute_deliversDownstreamSignalInsideSeamAsyncTrace() throws Exception {
        final DriverProperties driverProperties = getDriverProperties();
        final String host = driverProperties.getProperty(DriverProperties.HOST);
        final int port = Integer.parseInt(driverProperties.getProperty(DriverProperties.PORT));
        final String database = driverProperties.getProperty(DriverProperties.DATABASE);

        final PostgresqlConnectionFactory connectionFactory = new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .host(host)
                        .port(port)
                        .username(driverProperties.getUser())
                        .password(driverProperties.getPassword())
                        .database(database)
                        .build());

        final CountDownLatch latch = new CountDownLatch(1);
        final List<String> callbackThreads = new CopyOnWriteArrayList<>();
        final String mainThread = Thread.currentThread().getName();

        final PostgresqlConnection connection = Mono.from(connectionFactory.create()).block();
        try {
            // map() sits directly downstream of the wrapped execute() publisher: its lambda runs
            // inside TracedSubscriber's onNext window on the driver's delivery thread.
            Flux.from(connection.createStatement(SELECT_QUERY).execute())
                    .map(result -> {
                        callbackThreads.add(Thread.currentThread().getName());
                        try {
                            return new Echo().get(result);
                        } finally {
                            latch.countDown();
                        }
                    })
                    .flatMap(result -> result.map((row, metadata) -> String.valueOf(row.get(0))))
                    .blockLast();
        } finally {
            Mono.from(connection.close()).block();
        }

        assertTrue(latch.await(AWAIT_MAX_MILLIS, TimeUnit.MILLISECONDS), "callback was not invoked");
        assertNotEquals(mainThread, callbackThreads.get(0),
                "callback ran on the subscribing thread - the async assertion would be vacuous");

        final Method echoGet = Echo.class.getDeclaredMethod("get", Object.class);
        final ExpectedTrace callback = Expectations.event("INTERNAL_METHOD", echoGet);

        final Method executeMethod = Class.forName("io.r2dbc.postgresql.PostgresqlStatement").getDeclaredMethod("execute");
        final String databaseAddress = host + ":" + port;
        final ExpectedTrace asyncLink = Expectations.async(
                Expectations.event("R2DBC_POSTGRESQL_EXECUTE_QUERY", executeMethod, null, databaseAddress, database,
                        Expectations.sql(SELECT_QUERY, null)),
                callback);

        final PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        // read-only wait until the callback event is recorded; awaitTrace does nothing on timeout,
        // the assertion below is what actually decides the result.
        verifier.awaitTrace(callback, AWAIT_UNIT_MILLIS, AWAIT_MAX_MILLIS);
        verifier.printCache();
        verifier.verifyDiscreteTrace(asyncLink);
    }

    /**
     * Deterministic pin of the NESTED seam semantics (W3). The DatabaseClient path wraps several
     * publishers under the one config gate, and they are NOT one doubly-wrapped chain — each seam
     * wraps a different level:
     * <ul>
     * <li>the execute seam wraps the {@code Flux<Result>} (one Result object, no row signals),</li>
     * <li>the fetch-spec seam is a PURE RELAY: it wraps the row-level {@code Flux} with the
     * context already parked on the fetch spec — which the {@code DefaultDatabaseClient.sql()}
     * seam recorded.</li>
     * </ul>
     * Rows are therefore delivered inside the <b>sql() seam's</b> window and the callback chunk
     * links to the SPRING_DATA_R2DBC {@code sql()} event. The execute/create links carry no row
     * callback in this flow (their windows see no user signal) and stay dangling — accepted
     * policy P4, unit-pinned in seam-support's NestedSeamWindowTest. If this ownership ever
     * shifts (e.g. the relay stops using the sql() context), this assertion fails.
     */
    @Test
    public void nestedSeams_sourceSideExecuteSeamCarriesTheCallback() throws Exception {
        final DriverProperties driverProperties = getDriverProperties();
        final String host = driverProperties.getProperty(DriverProperties.HOST);
        final int port = Integer.parseInt(driverProperties.getProperty(DriverProperties.PORT));
        final String database = driverProperties.getProperty(DriverProperties.DATABASE);
        final PostgresqlConnectionFactory connectionFactory = new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .host(host)
                        .port(port)
                        .username(driverProperties.getUser())
                        .password(driverProperties.getPassword())
                        .database(database)
                        .build());

        final CountDownLatch latch = new CountDownLatch(1);
        final List<String> callbackThreads = new CopyOnWriteArrayList<>();
        final String mainThread = Thread.currentThread().getName();

        // all() emits per ROW (the sibling test maps per-Result and works on an empty table):
        // seed one row via the RAW driver api. Seeding through DatabaseClient would record a
        // second, indistinguishable sql() initiator and the discrete matcher binds to the first
        // candidate without backtracking (same pitfall as the redisson seam IT).
        final PostgresqlConnection seedConnection = Mono.from(connectionFactory.create()).block();
        try {
            Flux.from(seedConnection.createStatement("INSERT INTO persons (first_name, last_name, age) VALUES ('foo', 'bar', 30)").execute())
                    .flatMap(io.r2dbc.spi.Result::getRowsUpdated)
                    .blockLast();
        } finally {
            Mono.from(seedConnection.close()).block();
        }

        // fetch().all() is the fetch-spec seam (a pure relay of the sql() seam's context); the
        // statement execute() it subscribes to inside is the execute seam.
        org.springframework.r2dbc.core.DatabaseClient.create(connectionFactory)
                .sql(SELECT_QUERY)
                .fetch()
                .all()
                .map(row -> {
                    callbackThreads.add(Thread.currentThread().getName());
                    try {
                        return new Echo().get(row);
                    } finally {
                        latch.countDown();
                    }
                })
                .blockLast();

        assertTrue(latch.await(AWAIT_MAX_MILLIS, TimeUnit.MILLISECONDS), "callback was not invoked");
        assertNotEquals(mainThread, callbackThreads.get(0),
                "callback ran on the subscribing thread - the async assertion would be vacuous");

        final Method echoGet = Echo.class.getDeclaredMethod("get", Object.class);
        final ExpectedTrace callback = Expectations.event("INTERNAL_METHOD", echoGet);

        final Method sqlMethod = Class.forName("org.springframework.r2dbc.core.DefaultDatabaseClient")
                .getDeclaredMethod("sql", java.util.function.Supplier.class);
        final ExpectedTrace asyncLink = Expectations.async(
                Expectations.event("SPRING_DATA_R2DBC", sqlMethod),
                callback);

        final PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTrace(callback, AWAIT_UNIT_MILLIS, AWAIT_MAX_MILLIS);
        verifier.printCache();
        verifier.verifyDiscreteTrace(asyncLink);
    }
}
