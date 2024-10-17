/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import net.spy.memcached.ArcusClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@RestController
public class ArcusPluginController {
    private static final String KEY = "test:hello";
    private final static String ARCUS = "ARCUS";
    private final static String ARCUS_FUTURE_GET = "ARCUS_FUTURE_GET";
    private final static String ARCUS_EHCACHE_FUTURE_GET = "ARCUS_EHCACHE_FUTURE_GET";

    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    private ArcusClient arcusClient;

    @Autowired
    public ArcusPluginController(RequestMappingHandlerMapping handlerMapping) {
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
        return new ApiLinkPage("redis-lettuce-plugin-testweb")
                .addHrefTag(list)
                .build();
    }

    @GetMapping("/arcus/set")
    public String set() throws Exception {
        Future<Boolean> future = arcusClient.set(KEY, 600, "Hello, Arcus!");

        future.get(700L, TimeUnit.MILLISECONDS);
        return "OK";
    }

    @GetMapping("/arcus/asyncGet")
    public String asyncGet() throws Exception {
        Future<Object> future = arcusClient.asyncGet(KEY);

        return (String) future.get(3000L, TimeUnit.MILLISECONDS);
    }

    @GetMapping("/arcus/get")
    public String get() {
        return (String) arcusClient.get(KEY);
    }

    @GetMapping("/arcus/frontcache")
    public String frontCache() {
        String value =  (String) arcusClient.get(KEY);
        return arcusClient.getLocalCacheManager().getElement(KEY).toString();
    }
}
