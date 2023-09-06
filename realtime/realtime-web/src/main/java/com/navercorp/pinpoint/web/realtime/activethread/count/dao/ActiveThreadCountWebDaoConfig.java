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
package com.navercorp.pinpoint.web.realtime.activethread.count.dao;

import com.navercorp.pinpoint.channel.ChannelProviderRepository;
import com.navercorp.pinpoint.channel.legacy.DemandMessage;
import com.navercorp.pinpoint.channel.legacy.LegacyFluxClientAdaptor;
import com.navercorp.pinpoint.channel.legacy.SupplyMessage;
import com.navercorp.pinpoint.channel.service.FluxChannelServiceProtocol;
import com.navercorp.pinpoint.channel.service.client.ChannelServiceClient;
import com.navercorp.pinpoint.channel.service.client.FluxChannelServiceClient;
import com.navercorp.pinpoint.realtime.config.ATCServiceProtocolConfig;
import com.navercorp.pinpoint.realtime.dto.ATCDemand;
import com.navercorp.pinpoint.realtime.dto.ATCSupply;
import com.navercorp.pinpoint.redis.value.Incrementer;
import com.navercorp.pinpoint.redis.value.RedisIncrementer;
import com.navercorp.pinpoint.web.realtime.RealtimeWebCommonConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import reactor.core.scheduler.Schedulers;

/**
 * @author youngjin.kim2
 */
@Configuration
@Import({ RealtimeWebCommonConfig.class, ATCServiceProtocolConfig.class })
public class ActiveThreadCountWebDaoConfig {

    @Bean
    @ConditionalOnProperty(name = "pinpoint.modules.realtime.atc.version", havingValue = "v1", matchIfMissing = true)
    FluxChannelServiceClient<ATCDemand, ATCSupply> atcClientV1(
            ChannelProviderRepository channelProviderRepository,
            FluxChannelServiceProtocol<DemandMessage<ATCDemand>, SupplyMessage<ATCSupply>> protocol
    ) {
        return new LegacyFluxClientAdaptor<>(ChannelServiceClient.buildFlux(
                channelProviderRepository,
                protocol,
                Schedulers.newParallel("atc", Runtime.getRuntime().availableProcessors())
        ), d -> d.getId());
    }

    @Bean
    @ConditionalOnProperty(name = "pinpoint.modules.realtime.atc.version", havingValue = "v2")
    FluxChannelServiceClient<ATCDemand, ATCSupply> atcClientV2(
            ChannelProviderRepository channelProviderRepository,
            FluxChannelServiceProtocol<ATCDemand, ATCSupply> protocol
    ) {
        return ChannelServiceClient.buildFlux(
                channelProviderRepository,
                protocol,
                Schedulers.newParallel("atc", Runtime.getRuntime().availableProcessors())
        );
    }

    @Bean("atcIdIncrementer")
    Incrementer atcIdIncrementer(RedisTemplate<String, String> template) {
        return new RedisIncrementer("next-atc-id", template);
    }

    @Bean
    ActiveThreadCountDao activeThreadCountDao(
            @Qualifier("atcIdIncrementer") Incrementer inc,
            FluxChannelServiceClient<ATCDemand, ATCSupply> client
    ) {
        return new ChannelActiveThreadCountDao(inc, client);
    }

}
