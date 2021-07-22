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

import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class SpringWebfluxPluginController {
    @GetMapping("/server/welcome")
    public Mono<String> welcome() {
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
    public Mono<String> clientGet() {
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

//        WebClient client = WebClient.create("http://falfjlajdflajflajlf");
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();
        return response.bodyToMono(String.class);
    }
}
