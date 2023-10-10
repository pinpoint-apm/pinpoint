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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.navercorp.pinpoint.channel.legacy.DemandMessage;
import com.navercorp.pinpoint.channel.legacy.SupplyMessage;
import com.navercorp.pinpoint.channel.redis.pubsub.RedisPubSubConfig;
import com.navercorp.pinpoint.channel.redis.pubsub.RedisPubSubConstants;
import com.navercorp.pinpoint.channel.serde.JacksonSerde;
import com.navercorp.pinpoint.channel.service.ChannelServiceProtocol;
import com.navercorp.pinpoint.channel.service.MonoChannelServiceProtocol;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.realtime.dto.Echo;
import com.navercorp.pinpoint.realtime.serde.ClusterKeyDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Bean("clusterKeyDeserializer")
    SimpleModule clusterKeyDeserializer() {
        SimpleModule jacksonModule = new SimpleModule();
        jacksonModule.addDeserializer(ClusterKey.class, new ClusterKeyDeserializer());
        return jacksonModule;
    }

    @Bean
    MonoChannelServiceProtocol<DemandMessage<Echo>, SupplyMessage<Echo>> echoLegacyProtocol(
            ObjectMapper objectMapper,
            @Qualifier("clusterKeyDeserializer") SimpleModule clusterKeyJacksonModule
    ) {
        objectMapper.registerModule(clusterKeyJacksonModule);
        return ChannelServiceProtocol.<DemandMessage<Echo>, SupplyMessage<Echo>>builder()
                .setDemandSerde(JacksonSerde.byParameterized(objectMapper, DemandMessage.class, Echo.class))
                .setDemandPubChannelURIProvider(
                        demand -> URI.create(RedisPubSubConstants.SCHEME + ":demand:echo:" + demand.getId().getValue()))
                .setDemandSubChannelURI(URI.create(RedisPubSubConstants.SCHEME + ":demand:echo:*"))
                .setSupplySerde(JacksonSerde.byParameterized(objectMapper, SupplyMessage.class, Echo.class))
                .setSupplyChannelURIProvider(
                        demand -> URI.create(RedisPubSubConstants.SCHEME + ":supply:echo:" + demand.getId().getValue()))
                .setRequestTimeout(Duration.ofSeconds(3))
                .buildMono();
    }

    @Bean
    MonoChannelServiceProtocol<Echo, Echo> echoProtocol(
            ObjectMapper objectMapper,
            @Qualifier("clusterKeyDeserializer") SimpleModule clusterKeyJacksonModule
    ) {
        objectMapper.registerModule(clusterKeyJacksonModule);
        return ChannelServiceProtocol.<Echo, Echo>builder()
                .setDemandSerde(JacksonSerde.byClass(objectMapper, Echo.class))
                .setDemandPubChannelURIProvider(demand -> URI.create(RedisPubSubConstants.SCHEME + ":demand:echo-2"))
                .setDemandSubChannelURI(URI.create(RedisPubSubConstants.SCHEME + ":demand:echo-2"))
                .setSupplySerde(JacksonSerde.byClass(objectMapper, Echo.class))
                .setSupplyChannelURIProvider(
                        demand -> URI.create(RedisPubSubConstants.SCHEME + ":supply:echo-2:" + demand.getId()))
                .setRequestTimeout(Duration.ofSeconds(3))
                .buildMono();
    }

}
