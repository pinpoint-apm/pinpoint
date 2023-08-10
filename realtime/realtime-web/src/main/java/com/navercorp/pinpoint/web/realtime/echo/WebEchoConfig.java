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
package com.navercorp.pinpoint.web.realtime.echo;

import com.navercorp.pinpoint.channel.ChannelProviderRepository;
import com.navercorp.pinpoint.channel.legacy.DemandMessage;
import com.navercorp.pinpoint.channel.legacy.LegacyMonoClientAdaptor;
import com.navercorp.pinpoint.channel.legacy.SupplyMessage;
import com.navercorp.pinpoint.channel.service.MonoChannelServiceProtocol;
import com.navercorp.pinpoint.channel.service.client.ChannelServiceClient;
import com.navercorp.pinpoint.channel.service.client.MonoChannelServiceClient;
import com.navercorp.pinpoint.realtime.config.EchoServiceProtocolConfig;
import com.navercorp.pinpoint.realtime.dto.Echo;
import com.navercorp.pinpoint.redis.value.Incrementer;
import com.navercorp.pinpoint.redis.value.RedisIncrementer;
import com.navercorp.pinpoint.web.realtime.RealtimeWebCommonConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author youngjin.kim2
 */
@Configuration
@Import({ RealtimeWebCommonConfig.class, EchoServiceProtocolConfig.class })
public class WebEchoConfig {

    @Bean
    MonoChannelServiceClient<Echo, Echo> echoClient(
            ChannelProviderRepository channelProviderRepository,
            MonoChannelServiceProtocol<DemandMessage<Echo>, SupplyMessage<Echo>> protocol
    ) {
        return new LegacyMonoClientAdaptor<>(
                ChannelServiceClient.buildMono(channelProviderRepository, protocol),
                d -> d.getId()
        );
    }

    @Bean
    EchoDao echoDao(MonoChannelServiceClient<Echo, Echo> client) {
        return new ChannelEchoDao(client);
    }

    @Bean("echoIdIncrementer")
    Incrementer echoIdIncrementer(RedisTemplate<String, String> template) {
        return new RedisIncrementer("next-echo-id", template);
    }

    @Bean
    RedisEchoService echoService(
            @Qualifier("echoIdIncrementer") Incrementer inc,
            EchoDao dao
    ) {
        return new RedisEchoService(inc, dao);
    }

}
