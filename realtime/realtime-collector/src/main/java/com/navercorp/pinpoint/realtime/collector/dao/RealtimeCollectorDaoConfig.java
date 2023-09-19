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
package com.navercorp.pinpoint.realtime.collector.dao;

import com.navercorp.pinpoint.channel.ChannelProviderRepository;
import com.navercorp.pinpoint.channel.ChannelSpringConfig;
import com.navercorp.pinpoint.channel.redis.kv.RedisKVChannelConfig;
import com.navercorp.pinpoint.channel.redis.kv.RedisKVConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.net.InetAddress;
import java.net.URI;
import java.time.Duration;

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
@Import({ RedisKVChannelConfig.class, ChannelSpringConfig.class })
public class RealtimeCollectorDaoConfig {

    @Value("${pinpoint.modules.realtime.connection-emit.period:PT5S}")
    private Duration connectionListEmitPeriod;

    @Value("${pinpoint.modules.realtime.connection-emit.ttl-margin:PT10S}")
    private Duration connectionListTTLMargin;

    @Bean
    CollectorStateDao pubCollectorStateDao(ChannelProviderRepository channelProviderRepository) throws Exception {
        URI pubChannelURI = URI.create(
                RedisKVConstants.SCHEME + ':' + getConnectionListTTL() + ":collectors:" + getHostname());
        return new PubCollectorStateDao(channelProviderRepository, pubChannelURI);
    }

    private Duration getConnectionListTTL() {
        return this.connectionListEmitPeriod.plus(this.connectionListTTLMargin);
    }

    private static String getHostname() throws Exception {
        return InetAddress.getLocalHost().getHostName();
    }

    public Duration getConnectionListEmitPeriod() {
        return connectionListEmitPeriod;
    }

}
