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

import com.navercorp.pinpoint.channel.ChannelSpringConfig;
import com.navercorp.pinpoint.channel.redis.pubsub.RedisPubSubConfig;
import com.navercorp.pinpoint.channel.redis.pubsub.RedisPubSubConstants;
import com.navercorp.pinpoint.channel.serde.JsonSerdeFactory;
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
    FluxChannelServiceProtocol<ATCDemand, ATCSupply> atcProtocol(JsonSerdeFactory factory) {
        return ChannelServiceProtocol.<ATCDemand, ATCSupply>builder()
                .setDemandSerde(factory.byClass(ATCDemand.class))
                .setDemandPubChannelURIProvider(demand -> URI.create(RedisPubSubConstants.SCHEME + ":demand:atc-2"))
                .setDemandSubChannelURI(URI.create(RedisPubSubConstants.SCHEME + ":demand:atc-2"))
                .setSupplySerde(factory.byClass(ATCSupply.class))
                .setSupplyChannelURIProvider(ATCServiceProtocolConfig::getATCSupplyChannelURI)
                .setDemandInterval(Duration.ofSeconds(5))
                .setBufferSize(4)
                .setChannelStateFn(supply -> ChannelState.ALIVE)
                .buildFlux();
    }

    private static URI getATCSupplyChannelURI(ATCDemand demand) {
        return URI.create(RedisPubSubConstants.SCHEME +
                ":supply:atc-2:" +
                demand.getServiceName() + ':' +
                demand.getApplicationName() + ':' +
                demand.getAgentId() + ':' +
                demand.getStartTimestamp());
    }

}
