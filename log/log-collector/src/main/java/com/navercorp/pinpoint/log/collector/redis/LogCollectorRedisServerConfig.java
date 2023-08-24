/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.log.collector.redis;

import com.navercorp.pinpoint.channel.ChannelProviderRepository;
import com.navercorp.pinpoint.channel.ChannelSpringConfig;
import com.navercorp.pinpoint.channel.redis.kv.RedisKVChannelConfig;
import com.navercorp.pinpoint.channel.redis.pubsub.RedisPubSubConfig;
import com.navercorp.pinpoint.channel.service.FluxChannelServiceProtocol;
import com.navercorp.pinpoint.channel.service.server.ChannelServiceServer;
import com.navercorp.pinpoint.log.LogServiceProtocolConfig;
import com.navercorp.pinpoint.log.collector.service.LogConsumerService;
import com.navercorp.pinpoint.log.collector.service.LogServiceConfig;
import com.navercorp.pinpoint.log.vo.FileKey;
import com.navercorp.pinpoint.log.vo.LogPile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.util.concurrent.Executors;

/**
 * @author youngjin.kim2
 */
@Configuration
@Import({
        LogServiceProtocolConfig.class,
        LogServiceConfig.class,
        ChannelSpringConfig.class,
        RedisPubSubConfig.class,
        RedisKVChannelConfig.class,
})
public class LogCollectorRedisServerConfig {

    @Value("${pinpoint.log.collector.broadcast-connection-period-millis:10000}")
    private long broadcastConnectionPeriodMillis;

    @Bean
    ChannelServiceServer logPubSubServer(
            ChannelProviderRepository channelProviderRepository,
            FluxChannelServiceProtocol<FileKey, LogPile> protocol,
            LogConsumerService logConsumerService
    ) {
        Duration duration = protocol.getDemandInterval().plus(Duration.ofSeconds(2));
        return ChannelServiceServer.buildFlux(
                channelProviderRepository,
                protocol,
                fileKey -> logConsumerService.tail(fileKey, duration)
        );
    }

    @Bean
    LogFileListServer logConnectionBroadcastingService(
            LogConsumerService service,
            ChannelProviderRepository channelProviderRepository
    ) {
        final Duration broadcastConnectionPeriod = Duration.ofMillis(broadcastConnectionPeriodMillis);
        return new LogFileListServer(
                Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "LogConnection-Broadcaster")),
                service,
                channelProviderRepository,
                broadcastConnectionPeriod
        );
    }

}
