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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.JettyClientHttpRequestFactory;
import org.springframework.http.client.ReactorNettyClientRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
public class SpringRestClientPluginController {

    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public SpringRestClientPluginController(RequestMappingHandlerMapping handlerMapping) {
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
        return new ApiLinkPage("spring-webflux-plugin-testweb")
                .addHrefTag(list)
                .build();
    }

    @GetMapping("/server/welcome/**")
    public Mono<String> serverWelcome(ServerWebExchange exchange) {
        exchange.getAttributes().put("pinpoint.metric.uri-template", "/test");
        return Mono.just("Welcome Home");
    }

    @GetMapping("/jdk/httpclient")
    public String jdkHttpClient(ServerWebExchange exchange) {
        RestClient restClient = RestClient.builder().requestFactory(new SimpleClientHttpRequestFactory()).build();
        String result = restClient.get().uri("http://httpbin.org").retrieve().body(String.class);

        return result;
    }

    @GetMapping("/java11/httpclient")
    public String javaHttpClient(ServerWebExchange exchange) {
        RestClient restClient = RestClient.builder().requestFactory(new JdkClientHttpRequestFactory()).build();
        String result = restClient.get().uri("http://httpbin.org").retrieve().body(String.class);

        return result;
    }

    @GetMapping("/apache/httpclient")
    public String apacheHttpClient(ServerWebExchange exchange) {
        RestClient restClient = RestClient.builder().requestFactory(new HttpComponentsClientHttpRequestFactory()).build();
        String result = restClient.get().uri("http://httpbin.org").retrieve().body(String.class);

        return result;
    }

    @GetMapping("/jetty/httpclient")
    public String jettyHttpClient(ServerWebExchange exchange) {
        RestClient restClient = RestClient.builder().requestFactory(new JettyClientHttpRequestFactory()).build();
        String result = restClient.get().uri("http://httpbin.org").retrieve().body(String.class);

        return result;
    }

    @GetMapping("/reactor/httpclient")
    public String reactorHttpClient(ServerWebExchange exchange) {
        RestClient restClient = RestClient.builder().requestFactory(new ReactorNettyClientRequestFactory()).build();
        String result = restClient.get().uri("http://httpbin.org").retrieve().body(String.class);


        return result;
    }
}