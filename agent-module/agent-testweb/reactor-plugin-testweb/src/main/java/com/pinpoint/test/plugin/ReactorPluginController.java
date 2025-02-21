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
import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
        Flux.range(1, 1)
                .parallel(2)
                .runOn(Schedulers.parallel())
                .map(i -> {
                    return call();
                })
                .subscribe(i -> System.out.println(Thread.currentThread().getName() + " -> " + i));

        return Mono.just("OK");
    }

    @GetMapping("/flux/publishOn")
    public Mono<String> fluxPublishOn() {
        Scheduler s = Schedulers.newParallel("parallel-scheduler", 4);

        final Flux<String> flux = Flux
                .range(1, 1)
                .map(i -> 10 + i)
                .publishOn(s)
                .map(i -> {
                    return call();
                });
        flux.subscribe(System.out::println);

        return Mono.just("OK");
    }

    @GetMapping("/flux/subscribeOn")
    public Mono<String> fluxSubscribeOn() {
        Scheduler s = Schedulers.newParallel("parallel-scheduler", 4);

        final Flux<String> flux = Flux
                .range(1, 1).map(i -> {
                    return call();
                })
                .subscribeOn(s)
                .map(i -> "value " + i);

        flux.subscribe(System.out::println);

        return Mono.just("OK");
    }

    @GetMapping("/mono/publishOn")
    public Mono<String> monoPublishOn() {
        Mono.just("test").publishOn(Schedulers.parallel()).map(i -> {
            return call();
        }).subscribe(System.out::println);

        return Mono.just("OK");
    }

    @GetMapping("/mono/subscribeOn")
    public Mono<String> monoSubscribeOn() {
        Mono.fromCallable(() -> Arrays.asList(1, 2))
                .subscribeOn(Schedulers.parallel()).map(i -> {
                    return call();
                }).subscribe(System.out::println);
        return Mono.just("OK");
    }

    @GetMapping("/mono/delay")
    public Mono<String> monoDelay() {
        System.out.println("MAIN thread=" + Thread.currentThread().getName());
        return Mono.delay(Duration.ofMillis(100L)).map(aLong -> {
            return call();
        });
    }

    @GetMapping("/mono/delayElement")
    public Mono<String> monoDelayElement() {
        System.out.println("MAIN thread=" + Thread.currentThread().getName());
        return Mono.just("Hello").delayElement(Duration.ofMillis(100L)).map(o -> {
            System.out.println("DELAY thread=" + Thread.currentThread().getName());
            return call();
        });
    }

    @GetMapping("/mono/delaySubscription")
    public Mono<String> monoDelaySubscription() {
        System.out.println("MAIN thread=" + Thread.currentThread().getName());
        return Mono.just("Hello").delaySubscription(Duration.ofMillis(100L)).map(o -> {
            return call();
        });
    }

    @GetMapping("/mono/take")
    public Mono<String> monoTake() {
        System.out.println("MAIN thread=" + Thread.currentThread().getName());
        return Mono.just("Hello").take(Duration.ofMillis(100L)).map(o -> {
            return call();
        });
    }

    @GetMapping("/flux/interval")
    public Flux<String> fluxInterval() {
        System.out.println("MAIN thread=" + Thread.currentThread().getName());
        return Flux.interval(Duration.ofMillis(100L)).take(3).map(o -> {
            return call();
        });
    }

    @GetMapping("/flux/buffer")
    public Flux<String> fluxBuffer() {
        System.out.println(Thread.currentThread().getName());
        return Flux.just(1, 2, 3).delayElements(Duration.ofMillis(100L)).take(3).map(o -> {
            return call();
        });
    }

    @GetMapping("/mono/subscribe/dispose")
    public Mono<String> monoSubscribeDispose() {
        System.out.println(Thread.currentThread().getName());

        WebClient client = WebClient.create("http://httpbin.org");
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();
        Mono<String> body = response.bodyToMono(String.class);
        body.subscribe();

        return Mono.just("ok");
    }

    @GetMapping("/mono/subscribe/return")
    public Mono<String> monoSubscribeReturn() {
        System.out.println(Thread.currentThread().getName());
        HttpClient httpClient = HttpClient.create();
        WebClient client = WebClient.builder().baseUrl("http://httpbin.org").clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        return client.method(HttpMethod.GET)
                .uri("").retrieve().bodyToMono(String.class);
    }

    @GetMapping("/flux/cancelOn")
    public Mono<String> fluxCancelOn() {
        System.out.println(Thread.currentThread().getName());
        WebClient client = WebClient.create("http://httpbin.org");
        Mono<String> callback = client.method(HttpMethod.GET)
                .uri("").retrieve().bodyToMono(String.class);

        WebClient client2 = WebClient.create("http://httpbin.org");
        return client2.method(HttpMethod.GET).uri("").retrieve().bodyToMono(String.class).cancelOn(Schedulers.parallel()).timeout(Duration.ofMillis(10), callback);
    }

    @GetMapping("/mono/retry")
    public Mono<String> clientRetry(ServerWebExchange exchange) {
        HttpClient httpClient = HttpClient.create();
        WebClient client = WebClient.builder().baseUrl("http://httpbin.org").clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();
        response.bodyToMono(String.class).retry(3).subscribe();

        return Mono.just("OK");
    }

    @GetMapping("/mono/retry/unknownHost")
    public Mono<String> clientRetryUnknownHost(ServerWebExchange exchange) {
        HttpClient httpClient = HttpClient.create();
        WebClient client = WebClient.builder().baseUrl("http://fjakjglkajlgkjal").clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();
        response.bodyToMono(String.class).retry(3).subscribe();

        return Mono.just("OK");
    }


    @GetMapping("/mono/retry/connectionTimeout")
    public Mono<String> clientRetryConnectionTimeout(ServerWebExchange exchange) {
        HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 100);
        WebClient client = WebClient.builder().baseUrl("http://httpbin.org").clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();
        response.bodyToMono(String.class).retry(3).subscribe();

        return Mono.just("OK");
    }

    @GetMapping("/mono/retryWhen/unknownHost")
    public Mono<String> clientRetryWhenUnknownHost(ServerWebExchange exchange) {
        HttpClient httpClient = HttpClient.create();
        WebClient client = WebClient.builder().baseUrl("http://fjakjglkajlgkjal").clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();
        response.bodyToMono(String.class).retryWhen(Retry.max(3)).subscribe();

        return Mono.just("OK");
    }

    @GetMapping("/mono/retryWhen/connectionTimeout")
    public Mono<String> clientRetryWhenConnectionTimeout(ServerWebExchange exchange) {
        HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 100);
        WebClient client = WebClient.builder().baseUrl("http://httpbin.org").clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();
        response.bodyToMono(String.class).retryWhen(Retry.max(3)).subscribe();

        return Mono.just("OK");
    }

    @GetMapping("/mono/timeout")
    public Mono<String> clientTimeout(ServerWebExchange exchange) {
        HttpClient httpClient = HttpClient.create();
        WebClient client = WebClient.builder().baseUrl("http://httpbin.org").clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();
        return response.bodyToMono(String.class).timeout(Duration.ofMillis(10));
    }

    @GetMapping("/mono/timeout/fallback")
    public Mono<String> clientTimeoutFallback(ServerWebExchange exchange) {
        HttpClient httpClient = HttpClient.create();
        WebClient client = WebClient.builder().baseUrl("http://httpbin.org").clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();
        return response.bodyToMono(String.class).timeout(Duration.ofMillis(10), Mono.just("TIMEOUT"));
    }

    @GetMapping("/mono/timeout/fallback2")
    public Mono<String> clientTimeoutFallback2(ServerWebExchange exchange) {
        HttpClient httpClient = HttpClient.create();
        WebClient client = WebClient.builder().baseUrl("http://httpbin.org").clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();
        return response.bodyToMono(String.class).timeout(Duration.ofMillis(10), fallback());
    }

    private Mono<String> fallback() {
        HttpClient httpClient = HttpClient.create();
        WebClient client = WebClient.builder().baseUrl("http://httpbin.org").clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();
        return response.bodyToMono(String.class);
    }

    private String call() {
        WebClient client = WebClient.create("http://httpbin.org");
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();
        Mono<String> body = response.bodyToMono(String.class);
        return body.block();
    }
}
