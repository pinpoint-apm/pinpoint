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
package com.navercorp.pinpoint.realtime.collector.service.state;

import com.navercorp.pinpoint.realtime.collector.receiver.ClusterPointLocator;
import com.navercorp.pinpoint.realtime.collector.receiver.RealtimeCollectorReceiverConfig;
import com.navercorp.pinpoint.realtime.serde.CollectorStateSerde;
import com.navercorp.pinpoint.redis.RedisBasicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import reactor.core.scheduler.Schedulers;

import java.net.InetAddress;
import java.time.Duration;

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "pinpoint.modules.realtime.publish-state", havingValue = "true", matchIfMissing = true)
@Import({ RedisBasicConfig.class, RealtimeCollectorReceiverConfig.class })
public class RealtimeCollectorStateServiceConfig {

    @Value("${pinpoint.modules.realtime.connection-emit.period:PT5S}")
    private Duration connectionListEmitPeriod;

    @Value("${pinpoint.modules.realtime.connection-emit.ttl-margin:PT10S}")
    private Duration connectionListTTLMargin;

    @Bean
    public IntervalRunner periodicRedisStatePublisher(
            ClusterPointLocator clusterPointLocator,
            RedisTemplate<String, String> redisTemplate
    ) throws Exception {
        return new IntervalRunner(
                new CollectorStateUpdateRunnable(
                        clusterPointLocator,
                        new RedisCollectorStatePublisherService(
                                redisTemplate,
                                new CollectorStateSerde(),
                                getRedisPublishKey(),
                                getTTL()
                        )
                ),
                this.connectionListEmitPeriod,
                Schedulers.boundedElastic()
        );
    }

    private Duration getTTL() {
        return this.connectionListEmitPeriod.plus(this.connectionListTTLMargin);
    }

    private static String getRedisPublishKey() throws Exception {
        return "collectors:" + InetAddress.getLocalHost().getHostName();
    }

}
