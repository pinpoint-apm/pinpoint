/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pinpoint.test.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisLettuceEventListenerCommandLineRunner implements CommandLineRunner {

    @Autowired
    private ReactiveStringRedisTemplate reactiveStringRedisTemplate;
    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    public void run(String... args) throws Exception {
        reactiveStringRedisTemplate.listenToChannel("channel-test")
                .doOnNext(message -> {
                    System.out.println("Received message: " + message);
                    publisher.publishEvent(new SimpleEvent(this, message.getMessage()));
                })
                .subscribe();
        System.out.println("RUN");
    }
}
