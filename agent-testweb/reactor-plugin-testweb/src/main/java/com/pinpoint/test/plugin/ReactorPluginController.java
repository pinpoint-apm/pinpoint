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

import com.pinpoint.test.common.view.ApiLinkPage;
import com.pinpoint.test.common.view.HrefTag;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class ReactorPluginController {

    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public ReactorPluginController(RequestMappingHandlerMapping handlerMapping) {
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
        return new ApiLinkPage("reactor-plugin-testweb")
                .addHrefTag(list)
                .build();
    }


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

    @GetMapping("/mono/delay")
    public Mono<String> monoDelay() {
        System.out.println(Thread.currentThread().getName());
        return Mono.delay(Duration.ofMillis(100L)).map(aLong -> {
            System.out.println(Thread.currentThread().getName());
            WebClient client = WebClient.create("http://naver.com");
            WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                    .uri("").retrieve();
            Mono<String> body = response.bodyToMono(String.class);
            return body.block();
        });
    }

    @GetMapping("/mono/delayElement")
    public Mono<String> monoDelayElement() {
        System.out.println(Thread.currentThread().getName());
        return Mono.just("Hello").delayElement(Duration.ofMillis(100L)).map(o -> {
            System.out.println(Thread.currentThread().getName());
            WebClient client = WebClient.create("http://naver.com");
            WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                    .uri("").retrieve();
            Mono<String> body = response.bodyToMono(String.class);
            return body.block();
        });
    }

    @GetMapping("/mono/delaySubscription")
    public Mono<String> monoDelaySubscription() {
        System.out.println(Thread.currentThread().getName());
        return Mono.just("Hello").delaySubscription(Duration.ofMillis(100L)).map(o -> {
            System.out.println(Thread.currentThread().getName());
            WebClient client = WebClient.create("http://naver.com");
            WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                    .uri("").retrieve();
            Mono<String> body = response.bodyToMono(String.class);
            return body.block();
        });
    }

    @GetMapping("/mono/take")
    public Mono<String> monoTake() {
        System.out.println(Thread.currentThread().getName());
        return Mono.just("Hello").take(Duration.ofMillis(100L)).map(o -> {
            System.out.println(Thread.currentThread().getName());
            WebClient client = WebClient.create("http://naver.com");
            WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                    .uri("").retrieve();
            Mono<String> body = response.bodyToMono(String.class);
            return body.block();
        });
    }

    @GetMapping("/flux/interval")
    public Flux<String> fluxInterval() {
        System.out.println(Thread.currentThread().getName());
        return Flux.interval(Duration.ofMillis(100L)).take(3).map(o -> {
            System.out.println(Thread.currentThread().getName());
            WebClient client = WebClient.create("http://naver.com");
            WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                    .uri("").retrieve();
            Mono<String> body = response.bodyToMono(String.class);
            return body.block();
        });
    }

    @GetMapping("/flux/buffer")
    public Flux<String> fluxBuffer() {
        System.out.println(Thread.currentThread().getName());
        return Flux.just(1, 2, 3).delayElements(Duration.ofMillis(100L)).take(3).map(o -> {
            System.out.println(Thread.currentThread().getName());
            WebClient client = WebClient.create("http://naver.com");
            WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                    .uri("").retrieve();
            Mono<String> body = response.bodyToMono(String.class);
            return body.block();
        });
    }

    @GetMapping("/mono/subscribe1")
    public Mono<String> monoSubscribe1() {
        System.out.println(Thread.currentThread().getName());

        WebClient client = WebClient.create("http://httpbin.org");
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();
        Mono<String> body = response.bodyToMono(String.class);
        body.subscribe();

        return Mono.just("ok");
    }

    @GetMapping("/mono/subscribe2")
    public Mono<String> monoSubscribe2() {
        System.out.println(Thread.currentThread().getName());
        HttpClient httpClient = HttpClient.create().doOnConnected(connection -> {
            connection.addHandlerFirst(new WriteTimeoutHandler(1, TimeUnit.MILLISECONDS));
        });
        WebClient client = WebClient.builder().baseUrl("http://httpbin.org").clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        client.method(HttpMethod.GET)
                .uri("").retrieve().bodyToMono(String.class).subscribe();

        return Mono.just("OK");
    }

    @GetMapping("/flux/cancelOn")
    public Mono<String> fluxCancelOn() {
        System.out.println(Thread.currentThread().getName());
        WebClient client = WebClient.create("http://naver.com");
        Mono<String> callback = client.method(HttpMethod.GET)
                .uri("").retrieve().bodyToMono(String.class);

        WebClient client2 = WebClient.create("http://httpbin.org");
        return client2.method(HttpMethod.GET).uri("").retrieve().bodyToMono(String.class).cancelOn(Schedulers.parallel()).timeout(Duration.ofMillis(10), callback);
    }
}
