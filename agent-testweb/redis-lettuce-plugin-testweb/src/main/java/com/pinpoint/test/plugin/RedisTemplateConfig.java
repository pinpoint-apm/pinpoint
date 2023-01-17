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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisTemplateConfig {
    private static final String HOST1 = "localhost";
    private static final String HOST2 = "localhost";
    private static final String HOST3 = "localhost";
    private static final int PORT1 = 18001;
    private static final int PORT2 = 18002;
    private static final int PORT3 = 18003;

    @Bean
    public StringRedisTemplate redisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate(redisConnectionFactory());
        template.setEnableTransactionSupport(true);
        return template;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration();
        clusterConfiguration.addClusterNode(new RedisNode(HOST1, PORT1));
        clusterConfiguration.addClusterNode(new RedisNode(HOST2, PORT2));
        clusterConfiguration.addClusterNode(new RedisNode(HOST3, PORT3));
        return new LettuceConnectionFactory(clusterConfiguration);
    }

    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate() {
        ReactiveStringRedisTemplate template = new ReactiveStringRedisTemplate(reactiveRedisConnectionFactory());
        return template;
    }

    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration();
        clusterConfiguration.addClusterNode(new RedisNode(HOST1, PORT1));
        clusterConfiguration.addClusterNode(new RedisNode(HOST2, PORT2));
        clusterConfiguration.addClusterNode(new RedisNode(HOST3, PORT3));
        return new LettuceConnectionFactory(clusterConfiguration);
    }
}
