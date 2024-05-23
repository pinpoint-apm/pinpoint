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
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class SpringWebfluxPluginController {

    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public SpringWebfluxPluginController(RequestMappingHandlerMapping handlerMapping) {
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
        WebClient client = WebClient.create("http://naver.com");

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

    @GetMapping("/client/error")
    public Mono<String> clientError(ServerWebExchange exchange) {
        WebClient client = WebClient.create("http://fjaljglkajg.gjalfjlajfl");
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();

        Mono<String> result = response.bodyToMono(String.class).onErrorMap(throwable -> {
            String message = throwable.getMessage();
            System.out.println("Error " + message);
            return new RuntimeException("ERROR");
        });
        result.subscribe();
        return Mono.just("OK");
    }

    @GetMapping("/client/responseTimeout")
    public Mono<String> clientResponseTimeout(ServerWebExchange exchange) {
        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofMillis(100));
        WebClient client = WebClient.builder().baseUrl("http://httpbin.org").clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();
        response.bodyToMono(String.class).onErrorComplete(throwable -> {
            System.out.println("ERROR=" + throwable.getMessage());
            return throwable != null;
        }).subscribe();

        return Mono.just("OK");
    }

    @GetMapping("/client/connectionTimeout")
    public Mono<String> clientConnectionTimeout(ServerWebExchange exchange) {
        HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 100);
        WebClient client = WebClient.builder().baseUrl("http://httpbin.org").clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();
        response.bodyToMono(String.class).onErrorComplete(throwable -> {
            System.out.println("ERROR=" + throwable.getMessage());
            return throwable != null;
        }).subscribe();

        return Mono.just("OK");
    }

    @GetMapping("/client/readTimeout")
    public Mono<String> clientReadTimeout(ServerWebExchange exchange) {
        HttpClient httpClient = HttpClient.create().doOnConnected(connection -> {
            connection.addHandlerFirst(new ReadTimeoutHandler(10, TimeUnit.MILLISECONDS));
        });
        WebClient client = WebClient.builder().baseUrl("http://httpbin.org").clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();
        response.bodyToMono(String.class).onErrorComplete(throwable -> {
            System.out.println("ERROR=" + throwable.getMessage());
            return throwable != null;
        }).subscribe();

        return Mono.just("OK");
    }

    @GetMapping("/client/writeTimeout")
    public Mono<String> clientWriteTimeout(ServerWebExchange exchange) {
        HttpClient httpClient = HttpClient.create().doOnConnected(connection -> {
            connection.addHandlerFirst(new WriteTimeoutHandler(1, TimeUnit.MILLISECONDS));
        });
        WebClient client = WebClient.builder().baseUrl("http://httpbin.org").clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();
        response.bodyToMono(String.class).onErrorComplete(throwable -> {
            System.out.println("ERROR=" + throwable.getMessage());
            return throwable != null;
        }).subscribe();

        return Mono.just("OK");
    }

    @GetMapping("/client/nativeResponseTimeout")
    public Mono<String> clientNativeResponseTimeout(ServerWebExchange exchange) {
        HttpClient httpClient = HttpClient.create();
        WebClient client = WebClient.builder().baseUrl("http://httpbin.org").clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").httpRequest(clientHttpRequest -> {
                    HttpClientRequest reactorRequest = clientHttpRequest.getNativeRequest();
                    reactorRequest.responseTimeout(Duration.ofMillis(10));
                }).retrieve();
        response.bodyToMono(String.class).onErrorComplete(throwable -> {
            System.out.println("ERROR=" + throwable.getMessage());
            return throwable != null;
        }).subscribe();

        return Mono.just("OK");
    }

    @GetMapping("/client/reactorTimeout")
    public Mono<String> clientReactorTimeout(ServerWebExchange exchange) {
        HttpClient httpClient = HttpClient.create();
        WebClient client = WebClient.builder().baseUrl("http://httpbin.org").clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("").retrieve();
        response.bodyToMono(String.class).timeout(Duration.ofMillis(10)).onErrorComplete(throwable -> {
            System.out.println("ERROR=" + throwable.getMessage());
            return throwable != null;
        }).subscribe();

        return Mono.just("OK");
    }

    @GetMapping("/client/resttemplate")
    public Mono<String> clientRestTemplate(ServerWebExchange exchange) {
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = "http://httpbin.org";
        ResponseEntity<String> response
                = restTemplate.getForEntity(fooResourceUrl + "/", String.class);
        System.out.println(response.getStatusCode());
        System.out.println(response.getBody());

        return Mono.just("OK");
    }

    @GetMapping("/client/resttemplate/https")
    public Mono<String> clientRestTemplateHttps(ServerWebExchange exchange) {
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl
                = "https://naver.com";
        ResponseEntity<String> response
                = restTemplate.getForEntity(fooResourceUrl + "/", String.class);
        System.out.println(response.getStatusCode());
        System.out.println(response.getBody());

        return Mono.just("OK");
    }

    @RequestMapping(value = "/jdk/connect")
    public String get() {

        final URL url = newURL("http://httpbin.org");
        try {
            URLConnection connection = url.openConnection();
            connection.connect();
        } catch (IOException e) {
            return "fail";
        }

        return "OK";
    }
    @RequestMapping(value = "/jdk/connect/https")
    public String getHttps() {

        final URL url = newURL("https://naver.com");
        try {
            URLConnection connection = url.openConnection();
            connection.connect();
        } catch (IOException e) {
            return "fail";
        }

        return "OK";
    }
    @RequestMapping(value = "/jdk/connect/duplicated")
    public String getDuplicated() {

        final URL url = newURL("http://httpbin.org");
        try {
            URLConnection connection = url.openConnection();
            connection.connect();
            connection.connect();
        } catch (IOException e) {
            return "fail";
        }

        return "OK";
    }

    @RequestMapping(value = "/jdk/connect2")
    public String get2() {

        final URL url = newURL("http://httpbin.org");
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            final int responseCode = connection.getResponseCode();
            final List<String> contents = readStream(connection);
        } catch (IOException e) {
            return "fail";
        }


        return "OK";
    }

    @RequestMapping(value = "/jdk/connect2/https")
    public String get2Https() {

        final URL url = newURL("https://naver.com");
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            final int responseCode = connection.getResponseCode();
            final List<String> contents = readStream(connection);
        } catch (IOException e) {
            return "fail";
        }


        return "OK";
    }

    private List<String> readStream(HttpURLConnection connection) throws IOException {
        try (InputStream inputStream = connection.getInputStream()) {
            return IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
        }
    }

    private URL newURL(String spec) {
        try {
            return new URL(spec);
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("invalid url" + spec, exception);
        }
    }
}