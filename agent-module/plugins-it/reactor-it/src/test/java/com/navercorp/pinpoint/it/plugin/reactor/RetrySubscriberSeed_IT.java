/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.it.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import test.pinpoint.plugin.reactor.Echo;
import test.pinpoint.plugin.reactor.ReactorFlow;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins S2-2 with the generic CoreSubscriber transform disabled. Only the exact retry subscriber
 * owns the seed, and only its {@code resubscribe} method restores that seed. A plain executor
 * delivers each failure from a thread that has no ambient trace, so a retry attempt can remain an
 * INTERNAL_METHOD event linked to the constructor seed only if both halves are present.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-retry-subscriber-seed.config")
@Dependency({"io.projectreactor:reactor-core:[3.6.9][3.7.19]"})
public class RetrySubscriberSeed_IT {
    private static final String REACTOR = "REACTOR";
    private static final String INTERNAL_METHOD = "INTERNAL_METHOD";

    private static final long AWAIT_UNIT_MILLIS = 20L;
    private static final long AWAIT_MAX_MILLIS = 5000L;

    @Test
    public void retryReusesExactSubscriberSeedAcrossForeignThreadResubscribe() throws Exception {
        runAttempts(false);
        verifyAsyncRetryLink("reactor.core.publisher.FluxRetry$RetrySubscriber");
    }

    @Test
    public void retryWhenReusesExactMainSubscriberSeedAcrossForeignThreadResubscribe() throws Exception {
        runAttempts(true);
        verifyAsyncRetryLink("reactor.core.publisher.FluxRetryWhen$RetryWhenMainSubscriber");
    }

    @Test
    public void retrySupportsSynchronousNestedResubscribe() throws Exception {
        runSynchronousAttempts(false);
        verifySynchronousRetry("reactor.core.publisher.FluxRetry$RetrySubscriber");
    }

    @Test
    public void retryWhenSupportsSynchronousNestedResubscribe() throws Exception {
        runSynchronousAttempts(true);
        verifySynchronousRetry("reactor.core.publisher.FluxRetryWhen$RetryWhenMainSubscriber");
    }

    private void verifyAsyncRetryLink(String subscriberClassName) throws Exception {
        final Constructor<?> seedConstructor = Class.forName(subscriberClassName).getDeclaredConstructors()[0];
        final Method echoGet = Echo.class.getDeclaredMethod("get", String.class);
        final ExpectedTrace retryAttempt = Expectations.event(INTERNAL_METHOD, echoGet);
        final ExpectedTrace seed = Expectations.event(REACTOR, seedConstructor);
        // Both retry attempts must use the same constructor seed. Matching just one async child
        // would allow a later attempt to detach silently.
        final ExpectedTrace asyncLink = Expectations.async(seed, retryAttempt, retryAttempt);

        final PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTrace(retryAttempt, AWAIT_UNIT_MILLIS, AWAIT_MAX_MILLIS);
        verifier.printCache();
        verifier.verifyDiscreteTrace(asyncLink);
    }

    private void verifySynchronousRetry(String subscriberClassName) throws Exception {
        final Constructor<?> seedConstructor = Class.forName(subscriberClassName).getDeclaredConstructors()[0];
        final Method echoGet = Echo.class.getDeclaredMethod("get", String.class);

        final PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTrace(Expectations.event(INTERNAL_METHOD, echoGet), AWAIT_UNIT_MILLIS, AWAIT_MAX_MILLIS);
        verifier.printCache();
        verifier.verifyDiscreteTrace(
                Expectations.event(REACTOR, seedConstructor),
                Expectations.event(INTERNAL_METHOD, echoGet),
                Expectations.event(INTERNAL_METHOD, echoGet),
                Expectations.event(INTERNAL_METHOD, echoGet));
    }

    private void runAttempts(boolean retryWhen) throws Exception {
        final ExecutorService failureExecutor = Executors.newSingleThreadExecutor();
        try {
            final AtomicInteger attempts = new AtomicInteger();
            final List<String> attemptThreads = new CopyOnWriteArrayList<>();
            final String subscribingThread = Thread.currentThread().getName();
            final CountDownLatch done = new CountDownLatch(1);

            new ReactorFlow().execute(() -> {
                Mono<String> source = Mono.create(sink -> {
                    attemptThreads.add(Thread.currentThread().getName());
                    new Echo().get("attempt" + attempts.incrementAndGet());
                    failureExecutor.execute(() -> sink.error(new RuntimeException("boom")));
                });
                source = retryWhen ? source.retryWhen(Retry.max(2)) : source.retry(2);
                source.doFinally(signal -> done.countDown()).subscribe(value -> {
                }, error -> {
                });
            });

            assertTrue(done.await(AWAIT_MAX_MILLIS, TimeUnit.MILLISECONDS), "retry flow did not finish");
            assertEquals(3, attempts.get(), "expected the initial attempt and two retries");
            assertEquals(3, attemptThreads.size());
            assertNotEquals(subscribingThread, attemptThreads.get(1), "first retry did not cross threads");
            assertNotEquals(subscribingThread, attemptThreads.get(2), "second retry did not cross threads");
        } finally {
            failureExecutor.shutdownNow();
        }
    }

    private void runSynchronousAttempts(boolean retryWhen) {
        final AtomicInteger attempts = new AtomicInteger();

        new ReactorFlow().execute(() -> {
            Mono<String> source = Mono.defer(() -> {
                new Echo().get("sync-attempt" + attempts.incrementAndGet());
                return Mono.error(new RuntimeException("sync boom"));
            });
            source = retryWhen ? source.retryWhen(Retry.max(2)) : source.retry(2);
            source.subscribe(value -> {
            }, error -> {
            });
        });

        assertEquals(3, attempts.get(), "expected the initial attempt and two synchronous retries");
    }
}
