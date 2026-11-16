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
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * org.asynchttpclient 3.x by default; build with -Dtestweb.async-http-client.version=2.16.1 (or another 2.x) to try the
 * 2.x line, which runs on the same Java 11.
 */
@RestController
public class NingAsyncHttpClientPluginController implements DisposableBean {

    private final RequestMappingHandlerMapping handlerMapping;
    private final AsyncHttpClient asyncHttpClient;
    private final ExecutorService callbackExecutor = Executors.newSingleThreadExecutor();

    public NingAsyncHttpClientPluginController(RequestMappingHandlerMapping handlerMapping, AsyncHttpClient asyncHttpClient) {
        this.handlerMapping = Objects.requireNonNull(handlerMapping, "handlerMapping");
        this.asyncHttpClient = Objects.requireNonNull(asyncHttpClient, "asyncHttpClient");
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
        return new ApiLinkPage("ning-asynchttpclient-plugin-testweb (async-http-client " + libraryVersion() + ")")
                .addHrefTag(list)
                .build();
    }

    @GetMapping("/client/version")
    public Mono<String> clientVersion() {
        return Mono.just(libraryVersion());
    }

    @GetMapping("/client/get")
    public Mono<String> clientGet() throws Exception {
        Response response = asyncHttpClient.prepareGet("http://naver.com").execute().get();
        return Mono.just(response.getResponseBody());
    }

    @GetMapping("/client/post")
    public Mono<String> clientPost() throws Exception {
        Response response = asyncHttpClient.preparePost("http://naver.com").execute().get();
        return Mono.just(response.getResponseBody());
    }

    @GetMapping("/client/local")
    public Mono<String> clientLocal(ServerWebExchange exchange) throws Exception {
        Response response = asyncHttpClient.prepareGet(localUrl(exchange)).execute().get();
        return Mono.just("status=" + response.getStatusCode());
    }

    @GetMapping("/client/unknown")
    public Mono<String> clientUnknown() throws Exception {
        try {
            Response response = asyncHttpClient.prepareGet("http://falfjlajdflajflajlf").execute().get();
            return Mono.just("status=" + response.getStatusCode());
        } catch (ExecutionException e) {
            // onThrowable of the handler records the failure
            return Mono.just("failed=" + e.getCause());
        }
    }

    @GetMapping("/client/handler")
    public Mono<String> clientHandler(ServerWebExchange exchange) throws Exception {
        // an application handler: the traced callbacks are the ones AsyncCompletionHandler declares
        String result = asyncHttpClient.prepareGet(localUrl(exchange)).execute(new AsyncCompletionHandler<String>() {
            @Override
            public String onCompleted(Response response) {
                return "status=" + response.getStatusCode();
            }
        }).get();
        return Mono.just(result);
    }

    @GetMapping("/client/callback")
    public Mono<String> clientCallback(ServerWebExchange exchange) {
        ListenableFuture<Response> listenableFuture = asyncHttpClient.prepareGet(localUrl(exchange)).execute();
        listenableFuture.addListener(() -> {
            try {
                Response response = listenableFuture.get();
                System.out.println("Callback response status=" + response.getStatusCode());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, callbackExecutor);
        return Mono.just("Callback added");
    }

    private static String localUrl(ServerWebExchange exchange) {
        return "http://localhost:" + exchange.getRequest().getLocalAddress().getPort() + "/";
    }

    private static String libraryVersion() {
        try (InputStream in = AsyncHttpClient.class.getResourceAsStream("/META-INF/maven/org.asynchttpclient/async-http-client/pom.properties")) {
            if (in == null) {
                return "unknown";
            }
            Properties properties = new Properties();
            properties.load(in);
            return properties.getProperty("version", "unknown");
        } catch (IOException e) {
            return "unknown";
        }
    }

    @Override
    public void destroy() {
        callbackExecutor.shutdown();
    }
}
