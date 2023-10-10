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
package com.navercorp.pinpoint.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.channel.ChannelSpringConfig;
import com.navercorp.pinpoint.channel.redis.pubsub.RedisPubSubConfig;
import com.navercorp.pinpoint.channel.redis.stream.RedisStreamConfig;
import com.navercorp.pinpoint.channel.serde.JacksonSerde;
import com.navercorp.pinpoint.channel.service.ChannelServiceProtocol;
import com.navercorp.pinpoint.channel.service.FluxChannelServiceProtocol;
import com.navercorp.pinpoint.channel.service.client.ChannelState;
import com.navercorp.pinpoint.log.vo.FileKey;
import com.navercorp.pinpoint.log.vo.LogPile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.net.URI;
import java.time.Duration;

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
@Import({ RedisPubSubConfig.class, RedisStreamConfig.class, ChannelSpringConfig.class })
public class LogServiceProtocolConfig {

    @Bean
    FluxChannelServiceProtocol<FileKey, LogPile> logProtocol(ObjectMapper objectMapper) {
        return ChannelServiceProtocol.<FileKey, LogPile>builder()
                .setDemandSerde(JacksonSerde.byClass(objectMapper, FileKey.class))
                .setDemandPubChannelURIProvider(demand -> URI.create("pubsub:log:demand:" + demand))
                .setDemandSubChannelURI(URI.create("pubsub:log:demand:*"))
                .setSupplySerde(JacksonSerde.byClass(objectMapper, LogPile.class))
                .setSupplyChannelURIProvider(demand -> URI.create("stream:log:supply:" + demand))
                .setDemandInterval(Duration.ofSeconds(5))
                .setBufferSize(4)
                .setChannelStateFn(supply -> ChannelState.ALIVE)
                .buildFlux();
    }

}
