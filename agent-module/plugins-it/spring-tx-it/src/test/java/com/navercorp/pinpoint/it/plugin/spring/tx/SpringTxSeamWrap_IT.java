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

package com.navercorp.pinpoint.it.plugin.spring.tx;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.ReactiveTransaction;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import reactor.core.publisher.Mono;
import test.pinpoint.plugin.tx.Echo;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runtime assertions for {@code profiler.spring.tx.wrap.publisher=true} (the seam wrapper variant
 * woven into {@code TransactionAspectSupport$ReactiveTransactionSupport.invokeWithinTransaction}).
 *
 * <p>No Spring context or database: a {@link ProxyFactory} + {@link TransactionInterceptor} with a
 * no-op {@link ReactiveTransactionManager} drives the exact woven method — the reactive branch is
 * selected because the service returns a {@link Mono} and the manager is reactive.
 *
 * <p>The config also sets {@code profiler.reactor.enable=false}: without the reactor plugin there
 * are no per-operator relays and no accessor fields on reactor publishers, so the async link can
 * only come from the wrapped publisher — the same wrapper-only proof the r2dbc/lettuce/redisson
 * seam ITs use. The service hops threads via {@code Mono.delay} (reactor parallel timer), making
 * the async-link assertion non-vacuous.
 *
 * <p><b>Discriminating probe (manual)</b>: flip {@code wrap.publisher=false} in
 * {@code pinpoint-tx-seam-wrap.config} — the async-link assertion must fail — then flip back.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-tx-seam-wrap.config")
@Dependency({"org.springframework:spring-tx:[5.3.39]",
        "org.springframework:spring-aop:5.3.39",
        "io.projectreactor:reactor-core:3.4.30",
        PluginITConstants.VERSION})
public class SpringTxSeamWrap_IT {
    private static final String SPRING_TX = "SPRING_TX";
    private static final String INTERNAL_METHOD = "INTERNAL_METHOD";

    private static final long AWAIT_UNIT_MILLIS = 20L;
    private static final long AWAIT_MAX_MILLIS = 5000L;

    private static Method echoGet() throws NoSuchMethodException {
        return Echo.class.getDeclaredMethod("get", String.class);
    }

    private static Member invokeWithinTransactionMethod() throws Exception {
        final Class<?> owner = Class.forName(
                "org.springframework.transaction.interceptor.TransactionAspectSupport$ReactiveTransactionSupport");
        final Class<?> invocationCallback = Class.forName(
                "org.springframework.transaction.interceptor.TransactionAspectSupport$InvocationCallback");
        final Class<?> transactionAttribute = Class.forName(
                "org.springframework.transaction.interceptor.TransactionAttribute");
        return owner.getDeclaredMethod("invokeWithinTransaction",
                Method.class, Class.class, invocationCallback, transactionAttribute, ReactiveTransactionManager.class);
    }

    @Test
    public void reactiveTransaction_wrapperAloneLinksAcrossThread() throws Exception {
        final TransactionInterceptor transactionInterceptor = new TransactionInterceptor();
        transactionInterceptor.setTransactionManager(new NoopReactiveTransactionManager());
        transactionInterceptor.setTransactionAttributeSource(new MatchAlwaysTransactionAttributeSource());

        final ProxyFactory proxyFactory = new ProxyFactory(new GreetServiceImpl());
        proxyFactory.addInterface(GreetService.class);
        proxyFactory.addAdvice(transactionInterceptor);
        final GreetService service = (GreetService) proxyFactory.getProxy();

        final CountDownLatch latch = new CountDownLatch(1);
        final List<String> callbackThreads = new CopyOnWriteArrayList<>();
        final String testThread = Thread.currentThread().getName();

        // the proxy call runs inside the @PluginTest root trace and returns the wrapped publisher;
        // Mono.delay delivers the value on the reactor parallel timer thread.
        service.greet()
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

        final ExpectedTrace callback = Expectations.event(INTERNAL_METHOD, echoGet());
        final ExpectedTrace asyncLink = Expectations.async(
                Expectations.event(SPRING_TX, invokeWithinTransactionMethod()), callback);

        final PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTrace(callback, AWAIT_UNIT_MILLIS, AWAIT_MAX_MILLIS);
        verifier.printCache();
        verifier.verifyDiscreteTrace(asyncLink);
    }

    public interface GreetService {
        Mono<String> greet();
    }

    public static class GreetServiceImpl implements GreetService {
        @Override
        public Mono<String> greet() {
            return Mono.delay(Duration.ofMillis(50L)).map(t -> "hello");
        }
    }

    /**
     * The transaction lifecycle itself is not under test - only the woven seam is. Begin/commit
     * are inert Monos.
     */
    static class NoopReactiveTransactionManager implements ReactiveTransactionManager {
        @Override
        public Mono<ReactiveTransaction> getReactiveTransaction(TransactionDefinition definition) {
            return Mono.just(new NoopReactiveTransaction());
        }

        @Override
        public Mono<Void> commit(ReactiveTransaction transaction) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> rollback(ReactiveTransaction transaction) {
            return Mono.empty();
        }
    }

    static class NoopReactiveTransaction implements ReactiveTransaction {
        private boolean rollbackOnly;

        @Override
        public boolean isNewTransaction() {
            return true;
        }

        @Override
        public void setRollbackOnly() {
            this.rollbackOnly = true;
        }

        @Override
        public boolean isRollbackOnly() {
            return rollbackOnly;
        }

        @Override
        public boolean isCompleted() {
            return false;
        }
    }
}
