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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
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
public class OkHttpPluginController {

    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public OkHttpPluginController(RequestMappingHandlerMapping handlerMapping) {
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
        return new ApiLinkPage("okhttp-plugin-testweb")
                .addHrefTag(list)
                .build();
    }

    @GetMapping("/client/post")
    public Mono<String> clientPost() throws Exception {
        OkHttpClient httpClient = new OkHttpClient();
        RequestBody body = RequestBody.create("{foo:bar}", MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder().url("http://naver.com").post(body).build();

        Response response = httpClient.newCall(request).execute();

        return Mono.just(response.message());
    }

    @GetMapping("/client/get")
    public Mono<String> clientGet(ServerWebExchange exchange) throws Exception {
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().url("http://naver.com").build();
        Response response = httpClient.newCall(request).execute();
        return Mono.just(response.message());
    }

    @GetMapping("/client/local")
    public Mono<String> clientLocal() throws Exception {
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().url("http://localhost:18080").build();
        Response response = httpClient.newCall(request).execute();
        return Mono.just(response.message());
    }

    @GetMapping("/client/unknown")
    public Mono<String> clientUnknown() throws Exception {
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().url("http://falfjlajdflajflajlf").build();
        Response response = httpClient.newCall(request).execute();
        return Mono.just(response.message());
    }
}
