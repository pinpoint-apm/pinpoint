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
import com.navercorp.pinpoint.realtime.dto.ATDDemand;
import com.navercorp.pinpoint.realtime.dto.ATDSupply;
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
public class ATDServiceProtocolConfig {

    @Bean
    MonoChannelServiceProtocol<ATDDemand, ATDSupply> atdProtocol(
            JsonSerdeFactory factory
    ) {
        return ChannelServiceProtocol.<ATDDemand, ATDSupply>builder()
                .setDemandSerde(factory.byClass(ATDDemand.class))
                .setDemandPubChannelURIProvider(demand -> URI.create(RedisPubSubConstants.SCHEME + ":demand:atd-2"))
                .setDemandSubChannelURI(URI.create(RedisPubSubConstants.SCHEME + ":demand:atd-2"))
                .setSupplySerde(factory.byClass(ATDSupply.class))
                .setSupplyChannelURIProvider(
                        demand -> URI.create(RedisPubSubConstants.SCHEME + ":supply:atd-2:" + demand.getId()))
                .setRequestTimeout(Duration.ofSeconds(3))
                .buildMono();
    }

}
