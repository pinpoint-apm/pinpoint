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
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class Resilience4jPluginController {

    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public Resilience4jPluginController(RequestMappingHandlerMapping handlerMapping) {
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
        return new ApiLinkPage("resilience4j-plugin-testweb")
                .addHrefTag(list)
                .build();
    }

    @GetMapping("/client/circuitBreaker")
    public Mono<String> clientCircuitBreaker() {
        for (int i = 0; i < 20; i++) {
            invoke();
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
            }
        }

        return Mono.just("OK");
    }

    private void invoke() {
        HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 100);
        WebClient client = WebClient.builder().baseUrl("http://httpbin.org").clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        WebClient.ResponseSpec responseSpec = client.method(HttpMethod.GET)
                .uri("")
                .retrieve();

        Mono<String> response = getResponse(responseSpec);
        response.transformDeferred(stringMono -> postOperation(stringMono)).subscribe();
    }

    private Mono<String> getResponse(WebClient.ResponseSpec responseSpec) {
        WebClient.ResponseSpec response = responseSpec.onStatus(HttpStatus::isError,
                clientResponse -> Mono.error(new RuntimeException("ERROR")));
        return response.bodyToMono(String.class).onErrorResume(NullPointerException.class, e -> Mono.empty());
    }

    static final CircuitBreaker circuitBreaker = CircuitBreaker.of("circuitBreaker", CircuitBreakerConfig.custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(3)
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofMillis(10000))
            .recordExceptions(WebClientException.class, IOException.class)
            .build());

    public <R> Mono<R> postOperation(Mono<R> monoResponse) {
        long startTime = System.currentTimeMillis();
        monoResponse = monoResponse.transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
        return monoResponse.map(response -> logResult(response, startTime, false))
                .doOnError(throwable -> logResult(throwable.getMessage(), startTime, true))
                .switchIfEmpty(Mono.fromRunnable(() -> logResult(null, startTime, false)));
    }

    private <R> R logResult(R response, long startTime, boolean isError) {
        System.out.println("##Result " + response);
        return response;
    }
}
