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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
public class GrpcPluginController {

    @Autowired
    private HelloWorldServer helloWorldServer;

    @Autowired
    private HelloWorldStreamServer helloWorldStreamServer;

    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public GrpcPluginController(RequestMappingHandlerMapping handlerMapping) {
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
        return new ApiLinkPage("grpc-plugin-testweb")
                .addHrefTag(list)
                .build();
    }

    @GetMapping("/client/get")
    public Mono<String> clientGet() throws Exception {
        HelloWorldClient helloWorldClient = new HelloWorldClient("127.0.0.1", helloWorldServer.getBindPort());
        String result = helloWorldClient.greet("hello", 2);

        return Mono.just(result);
    }

    @GetMapping("/client/stream")
    public Mono<String> clientStream() throws Exception {
        HelloWorldStreamClient helloWorldStreamClient = new HelloWorldStreamClient("127.0.0.1", helloWorldStreamServer.getBindPort());
        try {
            helloWorldStreamClient.greet(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return Mono.just("OK");
    }
}
