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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
public class SpringCloudGatewayPluginTestStarter {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudGatewayPluginTestStarter.class, args);
    }

    // tag::route-locator[]
    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder, RedisRateLimiter redisRateLimiter) {
        KeyResolver keyResolver = exchange -> Mono.just("test-user");
        final String httpUri = "http://httpbin.org";
        return builder.routes()
                .route(p -> p
                        .path("/get")
                        .filters(f -> f.addRequestHeader("Hello", "World").requestRateLimiter().configure(config -> {
                            config.setKeyResolver(keyResolver);
                            config.setRateLimiter(redisRateLimiter);
                        }))
                        .uri(httpUri))
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
    // end::route-locator[]

    // tag::fallback[]
    @RequestMapping("/fallback")
    public Mono<String> fallback() {
        return Mono.just("fallback");
    }
    // end::fallback[]
}