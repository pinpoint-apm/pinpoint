/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.it.plugin.lettuce;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.it.plugin.utils.TestcontainersOption;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import com.navercorp.pinpoint.test.plugin.shared.SharedDependency;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestBeforeAllResult;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import io.lettuce.core.AbstractRedisAsyncCommands;
import io.lettuce.core.AbstractRedisReactiveCommands;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.reactive.RedisStringReactiveCommands;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.pinpoint.plugin.lettuce.Echo;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runtime assertions for {@code profiler.redis.lettuce.wrap.publisher=true} (the seam wrapper
 * variant of the lettuce command interceptor).
 *
 * <p>The config also sets {@code profiler.reactor.enable=false}: without the reactor plugin there
 * are no per-operator relays and no accessor fields on reactor publishers, so a reactive
 * async link can only be produced by the wrapped publisher — the same wrapper-only proof
 * {@code R2dbcPostgresqlSeamWrap_IT} uses. The callback assertion is the async-link relation
 * ({@link Expectations#async(ExpectedTrace, ExpectedTrace...)}); see {@code ReactorPropagation_IT}
 * for why nothing weaker discriminates.
 *
 * <p><b>Discriminating probe (manual)</b>: flip {@code wrap.publisher=false} in
 * {@code pinpoint-lettuce-seam-wrap.config} — {@code reactive_wrapperAloneLinksAcrossThread}
 * must fail — then flip back.
 *
 * <p>The async/sync command surface shares the same interceptor: with wrap on, a non-reactor
 * result must keep the original injection fallback (hybrid), which the async case pins.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-lettuce-seam-wrap.config")
@Dependency({"io.lettuce:lettuce-core:[6.2.0.RELEASE],[6.5.0.RELEASE]",
        "org.latencyutils:LatencyUtils:[2.0.3]",
        PluginITConstants.VERSION})
@SharedDependency({PluginITConstants.VERSION, TestcontainersOption.TEST_CONTAINER})
@SharedTestLifeCycleClass(RedisServer.class)
public class RedisClientSeamWrap_IT {
    private static final String REDIS_LETTUCE = "REDIS_LETTUCE";
    private static final String INTERNAL_METHOD = "INTERNAL_METHOD";

    private static final long AWAIT_UNIT_MILLIS = 20L;
    private static final long AWAIT_MAX_MILLIS = 5000L;

    private static String host;
    private static int port;
    @AutoClose("shutdown")
    private static RedisClient redisClient;

    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
    }

    @BeforeAll
    public static void beforeClass() {
        port = Integer.parseInt(System.getProperty("PORT"));
        host = System.getProperty("HOST");
        redisClient = RedisClient.create(String.format("redis://%s:%s", host, port));
    }

    private static Method echoGet() throws NoSuchMethodException {
        return Echo.class.getDeclaredMethod("get", String.class);
    }

    @Test
    public void reactive_wrapperAloneLinksAcrossThread() throws Exception {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        try {
            RedisStringReactiveCommands<String, String> commands = connection.reactive();

            final CountDownLatch latch = new CountDownLatch(1);
            final List<String> callbackThreads = new CopyOnWriteArrayList<>();
            final String testThread = Thread.currentThread().getName();

            // the seam fires inside the @PluginTest root trace; the value is delivered on the
            // lettuce netty event loop - a real thread hop.
            commands.set("foo", "bar")
                    .map(v -> {
                        callbackThreads.add(Thread.currentThread().getName());
                        try {
                            return new Echo().get("Hello" + v);
                        } finally {
                            latch.countDown();
                        }
                    })
                    .subscribe();

            assertTrue(latch.await(AWAIT_MAX_MILLIS, TimeUnit.MILLISECONDS), "callback was not invoked");
            assertEquals(1, callbackThreads.size(), "callback ran an unexpected number of times");
            assertNotEquals(testThread, callbackThreads.get(0),
                    "callback ran on the test thread - no hop, the assertion would be vacuous");

            // io.lettuce.core.AbstractRedisReactiveCommands.set(java.lang.Object, java.lang.Object)
            Method reactiveSet = AbstractRedisReactiveCommands.class.getDeclaredMethod("set", Object.class, Object.class);
            final ExpectedTrace callback = Expectations.event(INTERNAL_METHOD, echoGet());
            final ExpectedTrace asyncLink = Expectations.async(
                    Expectations.event(REDIS_LETTUCE, reactiveSet), callback);

            PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
            verifier.awaitTrace(callback, AWAIT_UNIT_MILLIS, AWAIT_MAX_MILLIS);
            verifier.printCache();
            verifier.verifyDiscreteTrace(asyncLink);
        } finally {
            connection.close();
        }
    }

    @Test
    public void async_hybridFallbackKeepsCommandEvents() throws Exception {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        try {
            RedisAsyncCommands<String, String> commands = connection.async();

            RedisFuture<String> future = commands.set("foo", "bar");
            future.get(1000, TimeUnit.MILLISECONDS);
            future = commands.get("foo");
            future.get(1000, TimeUnit.MILLISECONDS);

            PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
            Method setMethod = AbstractRedisAsyncCommands.class.getDeclaredMethod("set", Object.class, Object.class);
            Method getMethod = AbstractRedisAsyncCommands.class.getDeclaredMethod("get", Object.class);
            verifier.printCache();
            verifier.verifyTrace(Expectations.event(REDIS_LETTUCE, setMethod));
            verifier.verifyTrace(Expectations.event(REDIS_LETTUCE, getMethod));
        } finally {
            connection.close();
        }
    }
}
