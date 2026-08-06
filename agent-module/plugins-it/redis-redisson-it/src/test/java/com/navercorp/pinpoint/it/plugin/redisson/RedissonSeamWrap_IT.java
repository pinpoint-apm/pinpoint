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

package com.navercorp.pinpoint.it.plugin.redisson;

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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RBucketReactive;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.config.Config;
import reactor.core.publisher.Mono;
import test.pinpoint.plugin.redisson.Echo;

import java.lang.reflect.Member;
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
 * Runtime assertions for {@code profiler.redis.redisson.wrap.publisher=true} (the seam wrapper
 * variant of the redisson reactive method interceptor, woven into
 * {@code org.redisson.reactive.ReactiveProxyBuilder$1.execute}).
 *
 * <p>The config also sets {@code profiler.reactor.enable=false}: without the reactor plugin there
 * are no per-operator relays and no accessor fields on reactor publishers, so a reactive async
 * link can only be produced by the wrapped publisher — the same wrapper-only proof
 * {@code R2dbcPostgresqlSeamWrap_IT} and {@code RedisClientSeamWrap_IT} use.
 *
 * <p><b>Discriminating probe (manual)</b>: flip {@code wrap.publisher=false} in
 * {@code pinpoint-redisson-seam-wrap.config} — the async-link assertion must fail — then flip back.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-redisson-seam-wrap.config")
@Dependency({"org.redisson:redisson:[3.17.7],[3.27.2]",
        PluginITConstants.VERSION})
@SharedDependency({PluginITConstants.VERSION, TestcontainersOption.TEST_CONTAINER})
@SharedTestLifeCycleClass(RedisServer.class)
public class RedissonSeamWrap_IT {
    private static final String REDISSON_REACTIVE = "REDIS_REDISSON_REACTIVE";
    private static final String INTERNAL_METHOD = "INTERNAL_METHOD";

    private static final long AWAIT_UNIT_MILLIS = 20L;
    private static final long AWAIT_MAX_MILLIS = 5000L;

    private static RedissonClient redisson;

    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
    }

    @BeforeAll
    public static void beforeClass() {
        final String host = System.getProperty("HOST");
        final int port = Integer.parseInt(System.getProperty("PORT"));
        final Config config = new Config();
        config.useSingleServer().setAddress(String.format("redis://%s:%s", host, port));
        redisson = Redisson.create(config);
    }

    @AfterAll
    public static void afterClass() {
        if (redisson != null) {
            redisson.shutdown();
        }
    }

    private static Method echoGet() throws NoSuchMethodException {
        return Echo.class.getDeclaredMethod("get", String.class);
    }

    /**
     * The reactive proxy funnels every command through the woven {@code execute} — resolve it by
     * reflection because the owner is an anonymous class ({@code ReactiveProxyBuilder$1}) and the
     * signature changed across redisson versions (3.17+: {@code execute(Callable, Method)}).
     */
    private static Member reactiveExecuteMethod() throws Exception {
        final Class<?> owner = Class.forName("org.redisson.reactive.ReactiveProxyBuilder$1");
        try {
            // redisson 3.19+
            return owner.getDeclaredMethod("execute", java.util.concurrent.Callable.class, Method.class);
        } catch (NoSuchMethodException e1) {
            try {
                // redisson ~3.17.x
                return owner.getDeclaredMethod("execute", Method.class, Object.class, Method.class, Object[].class);
            } catch (NoSuchMethodException e2) {
                // older
                return owner.getDeclaredMethod("execute", Method.class, Object.class, Object[].class);
            }
        }
    }

    @Test
    public void reactive_wrapperAloneLinksAcrossThread() throws Exception {
        // seed OUTSIDE the reactive seam (sync api): the reactive set would record a second
        // initiator with the same api descriptor but no chunk (Mono<Void> has no user callback),
        // and the discrete matcher binds to the first candidate without backtracking.
        redisson.getBucket("foo").set("bar");

        final RedissonReactiveClient reactive = redisson.reactive();
        final RBucketReactive<String> bucket = reactive.getBucket("foo");

        final CountDownLatch latch = new CountDownLatch(1);
        final List<String> callbackThreads = new CopyOnWriteArrayList<>();
        final String testThread = Thread.currentThread().getName();

        final Mono<String> get = bucket.get();
        get.map(v -> {
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

        final ExpectedTrace callback = Expectations.event(INTERNAL_METHOD, echoGet());
        final ExpectedTrace asyncLink = Expectations.async(
                Expectations.event(REDISSON_REACTIVE, reactiveExecuteMethod()), callback);

        final PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTrace(callback, AWAIT_UNIT_MILLIS, AWAIT_MAX_MILLIS);
        verifier.printCache();
        verifier.verifyDiscreteTrace(asyncLink);
    }
}
