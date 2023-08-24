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
package com.navercorp.pinpoint.channel.redis.kv;

import com.navercorp.pinpoint.channel.ChannelProvider;
import com.navercorp.pinpoint.channel.ChannelProviderRegistry;
import com.navercorp.pinpoint.channel.PubChannelProvider;
import com.navercorp.pinpoint.channel.SubChannelProvider;
import com.navercorp.pinpoint.redis.RedisBasicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
@Import(RedisBasicConfig.class)
public class RedisKVChannelConfig {

    @Bean
    public ChannelProviderRegistry redisKeyValueChannelProvider(RedisTemplate<String, String> template) {
        Scheduler scheduler = Schedulers.newParallel("kv-channel-poller", Runtime.getRuntime().availableProcessors());
        PubChannelProvider pub = new RedisKVPubChannelProvider(template);
        SubChannelProvider sub = new RedisKVSubChannelProvider(template, scheduler);
        return ChannelProviderRegistry.of(RedisKVConstants.SCHEME, ChannelProvider.pair(pub, sub));
    }

}
