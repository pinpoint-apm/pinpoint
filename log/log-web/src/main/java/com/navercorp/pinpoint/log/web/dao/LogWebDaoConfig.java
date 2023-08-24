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
package com.navercorp.pinpoint.log.web.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.channel.ChannelProviderRepository;
import com.navercorp.pinpoint.channel.redis.pubsub.RedisPubSubConfig;
import com.navercorp.pinpoint.channel.service.FluxChannelServiceProtocol;
import com.navercorp.pinpoint.channel.service.client.ChannelServiceClient;
import com.navercorp.pinpoint.channel.service.client.FluxChannelServiceClient;
import com.navercorp.pinpoint.log.LogServiceProtocolConfig;
import com.navercorp.pinpoint.log.vo.FileKey;
import com.navercorp.pinpoint.log.vo.LogPile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import reactor.core.scheduler.Schedulers;

/**
 * @author youngjin.kim2
 */
@Configuration
@Import({ RedisPubSubConfig.class, LogServiceProtocolConfig.class })
public class LogWebDaoConfig {

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    FluxChannelServiceClient<FileKey, LogPile> liveTailClient(
            ChannelProviderRepository channelProviderRepository,
            FluxChannelServiceProtocol<FileKey, LogPile> protocol
    ) {
        return ChannelServiceClient.buildFlux(
                channelProviderRepository,
                protocol,
                Schedulers.newParallel("liveTail", Runtime.getRuntime().availableProcessors())
        );
    }

    @Bean
    LiveTailDao liveTailDao(
            RedisTemplate<String, String> template,
            FluxChannelServiceClient<FileKey, LogPile> client
    ) {
        return new LiveTailDaoImpl(template, client);
    }

}
