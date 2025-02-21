/*
 * Copyright 2025 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import test.pinpoint.plugin.reactor.Echo;

import java.time.Duration;
import java.util.Arrays;

@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint.config")
@Dependency({"io.projectreactor:reactor-core:[3.6.9]"})

public class Reactor_IT {

    @Test
    public void fluxPublishOn() throws Exception {
        System.out.println("## Main thread=" + Thread.currentThread().getName());
        Scheduler s = Schedulers.newParallel("parallel-scheduler", 3);
        Flux.range(1, 1).publishOn(s).map(v -> {
            System.out.println("## Work thread=" + Thread.currentThread().getName());
            Echo echoRepository = new Echo();
            return echoRepository.get("Hello" + v);
        }).subscribe();

        // for parallel-scheduler
        Thread.sleep(100);

        // reactor.core.publisher.FluxPublishOn
        // reactor.core.publisher.FluxPublishOn$PublishOnConditionalSubscriber
        // reactor.core.publisher.FluxPublishOn$PublishOnSubscriber
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }

    @Test
    public void fluxSubscribeOn() throws Exception {
        System.out.println("## Main thread=" + Thread.currentThread().getName());
        Scheduler s = Schedulers.newParallel("parallel-scheduler", 4);
        Flux.range(1, 1).map(i -> 10 + i).subscribeOn(s).map(v -> {
            System.out.println("## Work thread=" + Thread.currentThread().getName());
            Echo echoRepository = new Echo();
            return echoRepository.get("Hello" + v);
        }).subscribe();

        // for parallel-scheduler
        Thread.sleep(100);

        // reactor.core.publisher.FluxSubscribeOnValue
        // reactor.core.publisher.FluxSubscribeOnValue$ScheduledEmpty
        // reactor.core.publisher.FluxSubscribeOnValue$ScheduledScalar
        // reactor.core.publisher.FluxSubscribeOnCallable
        // reactor.core.publisher.FluxSubscribeOnCallable$CallableSubscribeOnSubscription
        // reactor.core.publisher.FluxSubscribeOn -- TODO: need to check
        // reactor.core.publisher.FluxSubscribeOn$SubscribeOnSubscriber
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }

    @Test
    public void fluxInterval() throws Exception {
        System.out.println("MAIN thread=" + Thread.currentThread().getName());
        Flux.interval(Duration.ofMillis(100L)).take(3).map(v -> {
            System.out.println("## Work thread=" + Thread.currentThread().getName());
            Echo echoRepository = new Echo();
            return echoRepository.get("Hello" + v);
        }).subscribe();

        // for parallel-scheduler
        Thread.sleep(100);

        // reactor.core.publisher.FluxInterval
        // reactor.core.publisher.FluxInterval$IntervalRunnable -- ignored
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }


    @Test
    public void monoPublishOn() throws Exception {
        System.out.println("MAIN thread=" + Thread.currentThread().getName());
        Mono.just("test").publishOn(Schedulers.parallel()).map(v -> {
            System.out.println("## Work thread=" + Thread.currentThread().getName());
            Echo echoRepository = new Echo();
            return echoRepository.get("Hello" + v);
        }).subscribe();

        // for parallel-scheduler
        Thread.sleep(100);

        // reactor.core.publisher.MonoPublishOn
        // reactor.core.publisher.MonoPublishOn$PublishOnSubscriber
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }

    @Test
    public void monoSubscribeOn() throws Exception {
        System.out.println("MAIN thread=" + Thread.currentThread().getName());
        Mono.fromCallable(() -> Arrays.asList(1)).subscribeOn(Schedulers.parallel()).map(v -> {
            System.out.println("## Work thread=" + Thread.currentThread().getName());
            Echo echoRepository = new Echo();
            return echoRepository.get("Hello" + v);
        }).subscribe();

        // for parallel-scheduler
        Thread.sleep(100);

        // reactor.core.publisher.MonoSubscribeOnValue
        // reactor.core.publisher.MonoSubscribeOnCallable
        // reactor.core.publisher.MonoSubscribeOn
        // reactor.core.publisher.MonoSubscribeOn$SubscribeOnSubscriber
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }

    @Test
    public void monoDelay() throws Exception {
        System.out.println("MAIN thread=" + Thread.currentThread().getName());
        Mono.delay(Duration.ofMillis(100L)).map(v -> {
            System.out.println("## Work thread=" + Thread.currentThread().getName());
            Echo echoRepository = new Echo();
            return echoRepository.get("Hello" + v);
        }).subscribe();

        // for parallel-scheduler
        Thread.sleep(100);

        // reactor.core.publisher.MonoDelay
        // reactor.core.publisher.MonoDelay$MonoDelayRunnable
        // reactor.core.publisher.MonoDelaySubscription
        // reactor.core.publisher.MonoDelayElement
        // reactor.core.publisher.MonoDelayElement$DelayElementSubscriber
        // reactor.core.publisher.MonoDelayElement$DelayElementSubscriber
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }

    @Test
    public void monoDelayElement() throws Exception {
        System.out.println("MAIN thread=" + Thread.currentThread().getName());
        Mono.just("Hello").delayElement(Duration.ofMillis(100L)).map(v -> {
            System.out.println("## Work thread=" + Thread.currentThread().getName());
            Echo echoRepository = new Echo();
            return echoRepository.get("Hello" + v);
        }).subscribe();

        // for parallel-scheduler
        Thread.sleep(100);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }

    @Test
    public void monoDelaySubscription() throws Exception {
        System.out.println("MAIN thread=" + Thread.currentThread().getName());
        Mono.just("Hello").delaySubscription(Duration.ofMillis(100L)).map(v -> {
            System.out.println("## Work thread=" + Thread.currentThread().getName());
            Echo echoRepository = new Echo();
            return echoRepository.get("Hello" + v);
        }).subscribe();

        // for parallel-scheduler
        Thread.sleep(100);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }

    @Test
    public void fluxTimeout() throws Exception {
        Flux.range(1, 1).timeout(Duration.ofMillis(100L)).map(v -> {
            Echo echoRepository = new Echo();
            return echoRepository.get("Hello" + v);
        }).subscribe();

        // for parallel-scheduler
        Thread.sleep(100);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }

    @Test
    public void fluxRetry() throws Exception {
        Flux.range(1, 1).retry(1).map(v -> {
            Echo echoRepository = new Echo();
            return echoRepository.get("Hello" + v);
        }).subscribe();

        // for parallel-scheduler
        Thread.sleep(100);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }

    @Test
    public void parallelFluxRunOn() throws Exception {
        System.out.println("MAIN thread=" + Thread.currentThread().getName());
        Flux.range(1, 1).parallel(2).runOn(Schedulers.parallel()).map(v -> {
            System.out.println("## Work thread=" + Thread.currentThread().getName());
            Echo echoRepository = new Echo();
            return echoRepository.get("Hello" + v);
        }).subscribe();

        // for parallel-scheduler
        Thread.sleep(100);

        // runOn -- TODO need to check
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }

    @Test
    public void baseSubscriber() {
        Flux.range(1, 1).map(v -> {
            Echo echoRepository = new Echo();
            return echoRepository.get("Hello" + v);
        }).subscribe();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }

    @Test
    public void fluxMap() {
        Flux.range(1, 2).map(v -> {
            Echo echoRepository = new Echo();
            return echoRepository.get("Hello" + v);
        }).subscribe();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }

    @Test
    public void fluxMapFilter() {
        Flux.range(1, 2).map(v -> {
            Echo echoRepository = new Echo();
            return echoRepository.get("Hello" + v);
        }).filter(v -> v.equals("Hello1")).subscribe();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }


    @Test
    public void blockingOptional() {
        Mono.just("foo").delayElement(Duration.ofMillis(10)).map(v -> {
            Echo echoRepository = new Echo();
            return echoRepository.get("Hello" + v);
        }).blockOptional();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }

    @Test
    public void connectableFluxHide() throws Exception {
        Flux.range(1, 4).replay().hide().map(v -> {
            Echo echoRepository = new Echo();
            return echoRepository.get("Hello" + v);
        }).subscribe();

        // for parallel-scheduler
        Thread.sleep(100);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }

    @Test
    public void fluxArray() {
        Flux.just(1, 2).map(v -> {
            Echo echoRepository = new Echo();
            return echoRepository.get("Hello" + v);
        }).subscribe();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }

    @Test
    public void fluxCallable() {
        Mono.fromCallable(() -> 1).flux().map(v -> {
            Echo echoRepository = new Echo();
            return echoRepository.get("Hello" + v);
        }).subscribe();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }


    @Test
    public void monoTake() {
        Mono.just("Hello").take(Duration.ofMillis(100L)).map(v -> {
            Echo echoRepository = new Echo();
            return echoRepository.get("Hello" + v);
        }).subscribe();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }
}
