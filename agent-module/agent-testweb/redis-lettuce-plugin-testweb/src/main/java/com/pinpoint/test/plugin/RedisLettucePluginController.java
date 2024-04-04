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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
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
public class RedisLettucePluginController {

    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    @Autowired
    public RedisLettucePluginController(RequestMappingHandlerMapping handlerMapping) {
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

    @GetMapping("/basic/get")
    public String get() {
        stringRedisTemplate.opsForValue().set("foo", "bar");

        return stringRedisTemplate.opsForValue().get("foo");
    }

    @GetMapping("/basic/callback")
    public String basicCallback() {
        stringRedisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                Long size = connection.dbSize();
                ((StringRedisConnection) connection).set("foo", "bar");
                return ((StringRedisConnection) connection).get("foo");
            }
        });

        return "OK";
    }

    @GetMapping("/stream/read")
    public String streamRead() {
        stringRedisTemplate.opsForStream().read(StreamReadOptions.empty().count(2), StreamOffset.latest("stream"));

        return "OK";
    }

    @GetMapping("/pipe")
    public String pipe() {
        int batchSize = 3;
        List<Object> results = stringRedisTemplate.executePipelined(
                new RedisCallback<Object>() {
                    public Object doInRedis(RedisConnection connection) throws DataAccessException {
                        StringRedisConnection stringRedisConn = (StringRedisConnection) connection;
                        for (int i = 0; i < batchSize; i++) {
                            stringRedisConn.rPop("myqueue");
                        }
                        return null;
                    }
                });

        return "OK";
    }

    @GetMapping("/reactive/get")
    public Mono<String> reactiveGet() {
        reactiveStringRedisTemplate.opsForValue().set("foo", "bar");

        return reactiveStringRedisTemplate.opsForValue().get("foo");
    }
}
