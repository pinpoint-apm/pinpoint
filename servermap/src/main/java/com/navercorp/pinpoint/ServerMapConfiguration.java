/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint;

import com.navercorp.pinpoint.redis.timeseries.RedisTimeseriesAsyncCommands;
import com.navercorp.pinpoint.redis.timeseries.RedisTimeseriesAsyncCommandsImpl;
import com.navercorp.pinpoint.redis.timeseries.connection.AsyncConnection;
import com.navercorp.pinpoint.redis.timeseries.connection.SimpleAsyncConnection;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author intr3p1d
 */
@Configuration
@EnableAsync
@EnableScheduling
@ComponentScan({
        "com.navercorp.pinpoint.servermap",
        "com.navercorp.pinpoint.servermap.service",
        "com.navercorp.pinpoint.servermap.dao.redis",
        "com.navercorp.pinpoint.servermap.dao.hbase",

        "com.navercorp.pinpoint.task",
})
public class ServerMapConfiguration {

    @Bean
    public RedisCommands<String, String> redisCommands() {
        RedisURI redisURI = RedisURI.create("redis://localhost:6379");
        RedisClient client = RedisClient.create(redisURI);
        return client.connect().sync();
    }

    @Bean
    public RedisTimeseriesAsyncCommands redisTimeseriesAsyncCommands() {
        RedisURI redisURI = RedisURI.create("redis://localhost:6379");
        RedisClient client = RedisClient.create(redisURI);
        AsyncConnection<String, String> connection = new SimpleAsyncConnection<>(client.connect());
        RedisTimeseriesAsyncCommands commands = new RedisTimeseriesAsyncCommandsImpl(connection);
        return commands;
    }

}
