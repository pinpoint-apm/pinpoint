/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.collector.applicationmap.redis.statistics;

import com.navercorp.pinpoint.redis.timeseries.RedisTimeseriesAsyncCommands;
import com.navercorp.pinpoint.redis.timeseries.RedisTimeseriesAsyncCommandsImpl;
import com.navercorp.pinpoint.redis.timeseries.connection.AsyncConnection;
import com.navercorp.pinpoint.redis.timeseries.connection.SimpleAsyncConnection;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author intr3p1d
 */
@Configuration
public class RedisBulkFactory {

    public RedisBulkFactory() {
    }

    @Bean
    public RedisClient redisClient() {
        RedisURI redisURI = RedisURI.create("redis://localhost:6379");
        return RedisClient.create(redisURI);
    }

    private RedisBulkWriter newRedisBulkWriter(RedisClient client) {
        AsyncConnection<String, String> connection = new SimpleAsyncConnection<>(client.connect());
        RedisTimeseriesAsyncCommands commands = new RedisTimeseriesAsyncCommandsImpl(connection);
        return new RedisBulkWriter(commands);
    }


    @Bean
    public RedisBulkWriter inboundBulkWriter() {
        return newRedisBulkWriter();
    }

    @Bean
    public RedisBulkWriter outboundBulkWriter() {
        return newRedisBulkWriter();
    }

    @Bean
    public RedisBulkWriter applicationMapSelfBulkWriter() {
        return newRedisBulkWriter();
    }

}
