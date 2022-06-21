/*
 * Copyright 2022 NAVER Corp.
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

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Configuration
public class SimpleRoute {

    @Bean
    public RouteLocator route(RouteLocatorBuilder builder, RedisRateLimiter redisRateLimiter) {
        KeyResolver keyResolver = exchange -> Mono.just("test-user");

        return builder.routes()
                .route("simple", r -> r.method(HttpMethod.GET).and().path("/simple")
                        .filters(f -> f
                                .addRequestHeader("foo", "bar")
                                .setPath("/headers")
                                // RequestRateLimiter 를 적용한 부분
                                .requestRateLimiter().configure(config -> {
                                    config.setKeyResolver(keyResolver);
                                    config.setRateLimiter(redisRateLimiter);
                                })
                        )
                        .uri("https://httpbin.org")
                )
                .build();
    }
}
