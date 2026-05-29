/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.sampling.tail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.UncheckedIOException;

@Configuration(proxyBeanMethods = false)
public class TailSamplingRedisConfig {

    /**
     * Redis connection factory for tail-sampling.
     * host, port, and password are resolved from {@code spring.data.redis.*} properties,
     * allowing a clustered collector to point at a shared Redis instance.
     */
    @Bean("tailSamplingRedisConnectionFactory")
    public RedisConnectionFactory tailSamplingRedisConnectionFactory(
            @Value("${spring.data.redis.host:localhost}") String host,
            @Value("${spring.data.redis.port:6379}") int port,
            @Value("${spring.data.redis.password:}") String password) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        if (password != null && !password.isEmpty()) {
            config.setPassword(RedisPassword.of(password));
        }
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean("tailSamplingRedisTemplate")
    public RedisTemplate<String, byte[]> tailSamplingRedisTemplate(
            @org.springframework.beans.factory.annotation.Qualifier("tailSamplingRedisConnectionFactory")
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.byteArray());
        template.afterPropertiesSet();
        return template;
    }

    @Bean("tailAcceptScript")
    public byte[] tailAcceptScript() {
        return loadScript("redis/tail-accept.lua");
    }

    @Bean("tailDecideScript")
    public byte[] tailDecideScript() {
        return loadScript("redis/tail-decide.lua");
    }

    private static byte[] loadScript(String path) {
        try {
            return StreamUtils.copyToByteArray(new ClassPathResource(path).getInputStream());
        } catch (IOException e) {
            throw new UncheckedIOException("failed to load lua script: " + path, e);
        }
    }
}
