/*
 * Copyright 2023 NAVER Corp.
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.ExchangeFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class SpringBoot3WebfluxPluginController {

    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public SpringBoot3WebfluxPluginController(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @GetMapping("/welcome")
    public String welcome() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = this.handlerMapping.getHandlerMethods();
        List<HrefTag> list = new ArrayList<>();
        for (RequestMappingInfo info : handlerMethods.keySet()) {
            for (String path : info.getDirectPaths()) {
                list.add(HrefTag.of(path));
            }
        }
        list.sort(Comparator.comparing(HrefTag::getPath));
        return new ApiLinkPage("spring-webflux-plugin-testweb")
                .addHrefTag(list)
                .build();
    }

    @GetMapping("/server/welcome/**")
    public Mono<String> serverWelcome(ServerWebExchange exchange) {
        exchange.getAttributes().put("pinpoint.metric.uri-template", "/test");
        return Mono.just("Welcome Home");
    }

    @GetMapping("/server/wait/3s")
    public Mono<String> wait3(ServerWebExchange exchange) {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
        }
        return Mono.just("Welcome Home");
    }

    @PostMapping("/server/post")
    public Mono<String> welcome(@RequestBody String body) {
        return Mono.just("Post=" + body);
    }

    @GetMapping("/client/post")
    public Mono<String> clientPost() {
        WebClient client = WebClient.create("http://localhost:18080");
        WebClient.ResponseSpec response = client.method(HttpMethod.POST)
                .uri("/server/post")
                .body(BodyInserters.fromPublisher(Mono.just("data"), String.class)).retrieve();
        return response.bodyToMono(String.class);
    }

    @GetMapping("/client/get")
    public Mono<String> clientGet(ServerWebExchange exchange) {
        exchange.getAttributes().put("pinpoint.metric.uri-template", "/test");
        WebClient client = WebClient.create("http://www.google.com");
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();
        return response.bodyToMono(String.class);
    }

    @GetMapping("/client/local")
    public Mono<String> clientLocal() {
        WebClient client = WebClient.create("http://localhost:18080");
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("/server/welcome").retrieve();
        return response.bodyToMono(String.class);
    }

    @GetMapping("/client/unknown")
    public Mono<String> clientUnknown() {
        WebClient client = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector())
                .baseUrl("http://falfjlajdflajflajlf")
                .build();

        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();
        return response.bodyToMono(String.class);
    }

    @GetMapping("/client/get/param")
    public Mono<String> clientGetParam(@RequestParam String id, @RequestParam(name = "password") String pwd) {
        final String param = "id=" + id + "&password=" + pwd;
        return Mono.just(param);
    }

    @PostMapping("/client/post/param")
    public Mono<String> clientPostParam(@RequestParam String id, @RequestParam(name = "password") String pwd) {
        final String param = "id=" + id + "&password=" + pwd;
        return Mono.just(param);
    }

    @PostMapping("/client/post/body")
    @ResponseBody
    public String clientPostParam(@RequestBody String body) {
        return "OK";
    }

    @PostMapping("/client/post/testExchange")
    @ResponseBody
    public Mono<String> testExchange(@RequestBody String body) {
        final ExchangeFunction exchangeFunction = ExchangeFunctions.create(new ReactorClientHttpConnector());
        exchangeFunction.exchange(ClientRequest.create(
                HttpMethod.GET, URI.create("http://localhost:18080")
        ).build());
        WebClient client = WebClient.builder().exchangeFunction(exchangeFunction).baseUrl("http://localhost:18080").build();

        return client.get()
                .uri("/")
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping(value = "/client/post/useStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<String> useStream() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> "Stream Data " + sequence)
                .take(10);
    }
}
