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

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;

@RestController
public class ReactorPluginController {

    @GetMapping("/parallelFlux/runOn")
    public Mono<String> parallelFluxRunOn() {
        Flux.range(1, 10)
                .parallel(2)
                .runOn(Schedulers.parallel())
                .subscribe(i -> System.out.println(Thread.currentThread().getName() + " -> " + i));

        return Mono.just("OK");
    }

    @GetMapping("/flux/publishOn")
    public Mono<String> fluxPublishOn() {
        Scheduler s = Schedulers.newParallel("parallel-scheduler", 4);

        final Flux<String> flux = Flux
                .range(1, 2)
                .map(i -> 10 + i)
                .publishOn(s)
                .map(i -> "value " + i);

        flux.subscribe(System.out::println);

        return Mono.just("OK");
    }

    @GetMapping("/flux/subscribeOn")
    public Mono<String> fluxSubscribeOn() {
        Scheduler s = Schedulers.newParallel("parallel-scheduler", 4);

        final Flux<String> flux = Flux
                .range(1, 2)
                .map(i -> 10 + i)
                .subscribeOn(s)
                .map(i -> "value " + i);

        flux.subscribe(System.out::println);

        return Mono.just("OK");
    }

    @GetMapping("/mono/publishOn")
    public Mono<String> monoPublishOn() {
        Flux<Integer> test = Flux
                .just(0, 1)
                .hide()
                .flatMap(f -> Mono.just(f).publishOn(Schedulers.parallel()).map(i -> 1 / i));
        test.subscribe(System.out::println);

        return Mono.just("OK");
    }

    @GetMapping("/mono/subscribeOn")
    public Mono<String> monoSubscribeOn() {
        Flux<Integer> test = Flux.fromIterable(Arrays.asList("A"))
                .flatMap(w -> Mono.fromCallable(() -> Arrays.asList(1, 2))
                        .subscribeOn(Schedulers.parallel())
                        .flatMapMany(Flux::fromIterable));
        test.subscribe(System.out::println);
        return Mono.just("OK");
    }
}
