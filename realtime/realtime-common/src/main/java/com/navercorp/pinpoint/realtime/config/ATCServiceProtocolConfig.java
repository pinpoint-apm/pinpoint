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
import com.navercorp.pinpoint.channel.ChannelSpringConfig;
import com.navercorp.pinpoint.channel.legacy.DemandMessage;
import com.navercorp.pinpoint.channel.legacy.SupplyMessage;
import com.navercorp.pinpoint.channel.redis.pubsub.RedisPubSubConfig;
import com.navercorp.pinpoint.channel.redis.pubsub.RedisPubSubConstants;
import com.navercorp.pinpoint.channel.serde.JacksonSerde;
import com.navercorp.pinpoint.channel.service.ChannelServiceProtocol;
import com.navercorp.pinpoint.channel.service.FluxChannelServiceProtocol;
import com.navercorp.pinpoint.channel.service.client.ChannelState;
import com.navercorp.pinpoint.realtime.dto.ATCDemand;
import com.navercorp.pinpoint.realtime.dto.ATCSupply;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.net.URI;
import java.time.Duration;

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
@Import({ RedisPubSubConfig.class, ChannelSpringConfig.class })
public class ATCServiceProtocolConfig {

    @Bean
    FluxChannelServiceProtocol<DemandMessage<ATCDemand>, SupplyMessage<ATCSupply>> atcLegacyProtocol(
            ObjectMapper objectMapper
    ) {
        return ChannelServiceProtocol.<DemandMessage<ATCDemand>, SupplyMessage<ATCSupply>>builder()
                .setDemandSerde(JacksonSerde.byParameterized(objectMapper, DemandMessage.class, ATCDemand.class))
                .setDemandPubChannelURIProvider(
                        demand -> URI.create(RedisPubSubConstants.SCHEME + ":demand:atc:" + demand.getId().getValue()))
                .setDemandSubChannelURI(URI.create(RedisPubSubConstants.SCHEME + ":demand:atc:*"))
                .setSupplySerde(JacksonSerde.byParameterized(objectMapper, SupplyMessage.class, ATCSupply.class))
                .setSupplyChannelURIProvider(
                        demand -> URI.create(RedisPubSubConstants.SCHEME + ":supply:atc:" + demand.getId().getValue()))
                .setDemandInterval(Duration.ofSeconds(10))
                .setBufferSize(4)
                .setChannelStateFn(supply -> ChannelState.ALIVE)
                .buildFlux();
    }

    @Bean
    FluxChannelServiceProtocol<ATCDemand, ATCSupply> atcProtocol(ObjectMapper objectMapper) {
        return ChannelServiceProtocol.<ATCDemand, ATCSupply>builder()
                .setDemandSerde(JacksonSerde.byClass(objectMapper, ATCDemand.class))
                .setDemandPubChannelURIProvider(demand -> URI.create(RedisPubSubConstants.SCHEME + ":demand:atc-2"))
                .setDemandSubChannelURI(URI.create(RedisPubSubConstants.SCHEME + ":demand:atc-2"))
                .setSupplySerde(JacksonSerde.byClass(objectMapper, ATCSupply.class))
                .setSupplyChannelURIProvider(ATCServiceProtocolConfig::getATCSupplyChannelURI)
                .setDemandInterval(Duration.ofSeconds(5))
                .setBufferSize(4)
                .setChannelStateFn(supply -> ChannelState.ALIVE)
                .buildFlux();
    }

    private static URI getATCSupplyChannelURI(ATCDemand demand) {
        return URI.create(RedisPubSubConstants.SCHEME +
                ":supply:atc-2:" +
                demand.getApplicationName() + ':' +
                demand.getAgentId() + ':' +
                demand.getStartTimestamp());
    }

}
