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
package com.navercorp.pinpoint.collector.realtime.atc.config;

import com.navercorp.pinpoint.collector.realtime.atc.dao.CountingMetricDao;
import com.navercorp.pinpoint.collector.realtime.atc.listener.ActiveThreadCountDemandConsumer;
import com.navercorp.pinpoint.collector.realtime.atc.service.ActiveThreadCountService;
import com.navercorp.pinpoint.collector.realtime.atc.service.SupplyPublishService;
import com.navercorp.pinpoint.collector.realtime.atc.service.cluster.ClusterActiveThreadCountService;
import com.navercorp.pinpoint.collector.realtime.atc.service.redis.RedisSupplyPublishService;
import com.navercorp.pinpoint.collector.realtime.config.RealtimeCollectorServiceConfig;
import com.navercorp.pinpoint.collector.realtime.service.AgentCommandService;
import com.navercorp.pinpoint.pubsub.PubChannel;
import com.navercorp.pinpoint.pubsub.SubConsumer;
import com.navercorp.pinpoint.realtime.atc.dto.ATCDemand;
import com.navercorp.pinpoint.realtime.atc.dto.ATCSupply;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author youngjin.kim2
 */
@Configuration
@Import({ ATCCollectorDaoConfig.class, RealtimeCollectorServiceConfig.class })
public class ATCCollectorServiceConfig {

    @Value("${pinpoint.collector.realtime.atc.demand.duration:12500}")
    long demandDurationMillis;

    @Value("${pinpoint.collector.realtime.atc.supply.throttle.termMillis:100}")
    long throttleTermMillis;

    @Bean
    @ConditionalOnBean(name = "commandHeaderTBaseDeserializerFactory")
    ActiveThreadCountService activeThreadCountService(
            AgentCommandService agentCommandService,
            @Qualifier("commandHeaderTBaseDeserializerFactory")
            DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory
    ) {
        return new ClusterActiveThreadCountService(agentCommandService, deserializerFactory, demandDurationMillis);
    }

    @Bean
    SupplyPublishService supplyPublishService(
            PubChannel<ATCSupply> supplyChannel,
            CountingMetricDao countingMetricDao
    ) {
        return new RedisSupplyPublishService(supplyChannel, countingMetricDao, throttleTermMillis);
    }

    @Bean("activeThreadCountDemandSubConsumer")
    SubConsumer<ATCDemand> activeThreadCountDemandSubConsumer(
            ActiveThreadCountService activeThreadCountService,
            SupplyPublishService publisher
    ) {
        return new ActiveThreadCountDemandConsumer(activeThreadCountService, publisher);
    }

}
