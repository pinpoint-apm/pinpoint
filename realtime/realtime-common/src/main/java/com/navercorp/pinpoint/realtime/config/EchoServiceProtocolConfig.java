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
package com.navercorp.pinpoint.realtime.config;

import com.navercorp.pinpoint.channel.redis.pubsub.RedisPubSubConfig;
import com.navercorp.pinpoint.channel.redis.pubsub.RedisPubSubConstants;
import com.navercorp.pinpoint.channel.serde.JsonSerdeFactory;
import com.navercorp.pinpoint.channel.service.ChannelServiceProtocol;
import com.navercorp.pinpoint.channel.service.MonoChannelServiceProtocol;
import com.navercorp.pinpoint.realtime.dto.Echo;
import com.navercorp.pinpoint.realtime.serde.ClusterKeyDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.net.URI;
import java.time.Duration;

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
@Import(RedisPubSubConfig.class)
public class EchoServiceProtocolConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer clusterKeyDeserializerCustomizer() {
        return builder -> {
            builder.deserializers(new ClusterKeyDeserializer());
        };
    }

    @Bean
    MonoChannelServiceProtocol<Echo, Echo> echoProtocol(
            JsonSerdeFactory factory
    ) {
        return ChannelServiceProtocol.<Echo, Echo>builder()
                .setDemandSerde(factory.byClass(Echo.class))
                .setDemandPubChannelURIProvider(demand -> URI.create(RedisPubSubConstants.SCHEME + ":demand:echo-2"))
                .setDemandSubChannelURI(URI.create(RedisPubSubConstants.SCHEME + ":demand:echo-2"))
                .setSupplySerde(factory.byClass(Echo.class))
                .setSupplyChannelURIProvider(
                        demand -> URI.create(RedisPubSubConstants.SCHEME + ":supply:echo-2:" + demand.getId()))
                .setRequestTimeout(Duration.ofSeconds(3))
                .buildMono();
    }

}
