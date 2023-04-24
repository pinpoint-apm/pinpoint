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
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
public class AgentProxyPluginController {

    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public AgentProxyPluginController(RequestMappingHandlerMapping handlerMapping) {
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
        return new ApiLinkPage("agent-proxy-plugin-testweb")
                .addHrefTag(list)
                .build();
    }

    @GetMapping("/proxy/apache")
    public Mono<String> proxyApache() {
        final String proxyHeaderValue = "t=" + System.currentTimeMillis() + "999" + " D=12345";

        WebClient client = WebClient.create("http://localhost:18080");
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("")
                .header("Pinpoint-ProxyApache", proxyHeaderValue)
                .retrieve();
        return response.bodyToMono(String.class);
    }

    @GetMapping("/proxy/nginx")
    public Mono<String> proxyNginx() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        timestamp = timestamp.substring(0, timestamp.length() - 3) + "." + timestamp.substring(timestamp.length() - 3);
        final String proxyHeaderValue = "t=" + timestamp + " D=0.000";

        WebClient client = WebClient.create("http://localhost:18080");
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("")
                .header("Pinpoint-ProxyNginx", proxyHeaderValue)
                .retrieve();
        return response.bodyToMono(String.class);
    }

    @GetMapping("/proxy/app")
    public Mono<String> proxyApp() {
        final String proxyHeaderValue = "t=" + String.valueOf(System.currentTimeMillis()) + " app=foo-bar";

        WebClient client = WebClient.create("http://localhost:18080");
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("")
                .header("Pinpoint-ProxyApp", proxyHeaderValue)
                .retrieve();
        return response.bodyToMono(String.class);
    }

    @GetMapping("/proxy/user/apache")
    public Mono<String> proxyUserApache() {
        final String proxyHeaderValue = "t=" + System.currentTimeMillis() + "999" + " D=12345";

        WebClient client = WebClient.create("http://localhost:18080");
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("")
                .header("X-Request", proxyHeaderValue)
                .retrieve();
        return response.bodyToMono(String.class);
    }

    @GetMapping("/proxy/user/nginx")
    public Mono<String> proxyUserNginx() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        timestamp = timestamp.substring(0, timestamp.length() - 3) + "." + timestamp.substring(timestamp.length() - 3);
        final String proxyHeaderValue = "t=" + timestamp + " D=0.000";

        WebClient client = WebClient.create("http://localhost:18080");
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("")
                .header("X-Request", proxyHeaderValue)
                .retrieve();
        return response.bodyToMono(String.class);
    }

    @GetMapping("/proxy/user/app")
    public Mono<String> proxyUserApp() {
        final String proxyHeaderValue = "t=" + String.valueOf(System.currentTimeMillis());

        WebClient client = WebClient.create("http://localhost:18080");
        WebClient.ResponseSpec response = client.method(HttpMethod.GET)
                .uri("")
                .header("X-Request", proxyHeaderValue)
                .retrieve();
        return response.bodyToMono(String.class);
    }
}
