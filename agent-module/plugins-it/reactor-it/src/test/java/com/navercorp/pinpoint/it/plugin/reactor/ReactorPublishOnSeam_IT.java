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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import test.pinpoint.plugin.reactor.Echo;
import test.pinpoint.plugin.reactor.ReactorFlow;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Correctness probe for the publishOn-only seam. Assembly happens under a root trace and
 * subscription happens after that root has left the thread. Generic subscriber instrumentation
 * and direct subscribe tracing are both disabled by the config, so the callback can be linked to
 * the publishOn event only when the returned Publisher was actually replaced by the seam wrapper.
 */
@Disabled("publishOn seam parked after the G2 verdict (2026-08-10): the wrapper's per-signal window "
        + "cost and fusion suppression cost ~2-3% RPS on the very chain it targets, cancelling the "
        + "expected relay recovery. The known failure here is a double async link - the forced-on "
        + "scheduler task carrier mints a sibling link inside the seam window and the leaf attaches "
        + "to it, leaving the wrapper's link dangling. Both are documented in "
        + "pinpoint-req-1739-review docs 35 (5-1a) and 41. Re-enable together with the carrier "
        + "hop-ownership fix if the seam design is revived.")
@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-publishon-seam.config")
@Dependency({"io.projectreactor:reactor-core:[3.6.9][3.7.19]"})
public class ReactorPublishOnSeam_IT {
    private static final String REACTOR = "REACTOR";
    private static final String INTERNAL_METHOD = "INTERNAL_METHOD";
    private static final String FLUX_PUBLISH_ON =
            "reactor.core.publisher.Flux.publishOn(reactor.core.scheduler.Scheduler, boolean, int, int)";
    private static final String MONO_PUBLISH_ON =
            "reactor.core.publisher.Mono.publishOn(reactor.core.scheduler.Scheduler)";

    private static final long AWAIT_UNIT_MILLIS = 20L;
    private static final long AWAIT_MAX_MILLIS = 5000L;

    @Test
    public void fluxOnNextPropagatesWhenSubscribedWithoutAmbientTrace() throws Exception {
        Scheduler scheduler = Schedulers.newParallel("it-seam-flux", 2);
        try {
            CountDownLatch callback = new CountDownLatch(1);
            Flux<?>[] assembled = new Flux<?>[1];

            new ReactorFlow().execute(() -> assembled[0] = Flux.range(1, 1)
                    .publishOn(scheduler)
                    .map(value -> echo(callback, "flux-" + value)));

            assembled[0].subscribe();

            awaitAndVerify(callback, FLUX_PUBLISH_ON);
        } finally {
            scheduler.dispose();
        }
    }

    @Test
    public void monoCompletePropagatesWhenSubscribedWithoutAmbientTrace() throws Exception {
        Scheduler scheduler = Schedulers.newParallel("it-seam-mono-complete", 2);
        try {
            CountDownLatch callback = new CountDownLatch(1);
            Mono<?>[] assembled = new Mono<?>[1];

            new ReactorFlow().execute(() -> assembled[0] = Mono.empty()
                    .publishOn(scheduler)
                    .doOnSuccess(value -> echo(callback, "complete")));

            assembled[0].subscribe();

            awaitAndVerify(callback, MONO_PUBLISH_ON);
        } finally {
            scheduler.dispose();
        }
    }

    @Test
    public void monoErrorPropagatesWhenSubscribedWithoutAmbientTrace() throws Exception {
        Scheduler scheduler = Schedulers.newParallel("it-seam-mono-error", 2);
        try {
            CountDownLatch callback = new CountDownLatch(1);
            Mono<?>[] assembled = new Mono<?>[1];

            new ReactorFlow().execute(() -> assembled[0] = Mono.error(new IllegalStateException("test"))
                    .publishOn(scheduler)
                    .doOnError(error -> echo(callback, "error")));

            assembled[0].subscribe(value -> {
            }, error -> {
            });

            awaitAndVerify(callback, MONO_PUBLISH_ON);
        } finally {
            scheduler.dispose();
        }
    }

    private String echo(CountDownLatch callback, String value) {
        try {
            return new Echo().get(value);
        } finally {
            callback.countDown();
        }
    }

    private void awaitAndVerify(CountDownLatch callback, String publishOnApi) throws Exception {
        assertTrue(callback.await(AWAIT_MAX_MILLIS, TimeUnit.MILLISECONDS), "callback was not invoked");

        Method echoGet = Echo.class.getDeclaredMethod("get", String.class);
        ExpectedTrace leaf = Expectations.event(INTERNAL_METHOD, echoGet);
        ExpectedTrace asyncLink = Expectations.async(Expectations.event(REACTOR, publishOnApi), leaf);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTrace(leaf, AWAIT_UNIT_MILLIS, AWAIT_MAX_MILLIS);
        verifier.printCache();
        verifier.verifyDiscreteTrace(asyncLink);
    }
}
