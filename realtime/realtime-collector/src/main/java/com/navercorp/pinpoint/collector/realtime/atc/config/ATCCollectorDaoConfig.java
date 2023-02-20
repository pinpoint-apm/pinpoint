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
import com.navercorp.pinpoint.collector.realtime.atc.dao.empty.EmptyCountingMetricDao;
import com.navercorp.pinpoint.collector.realtime.atc.dao.redis.RedisCountingMetricDao;
import com.navercorp.pinpoint.pubsub.PubChannel;
import com.navercorp.pinpoint.pubsub.SubChannel;
import com.navercorp.pinpoint.realtime.atc.dao.RedisATCDemandSubChannel;
import com.navercorp.pinpoint.realtime.atc.dao.RedisATCSupplyPubChannel;
import com.navercorp.pinpoint.realtime.atc.dto.ATCDemand;
import com.navercorp.pinpoint.realtime.atc.dto.ATCSupply;
import com.navercorp.pinpoint.redis.CommonRedisConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * @author youngjin.kim2
 */
@Configuration
@Import({ CommonRedisConfig.class })
public class ATCCollectorDaoConfig {

    @Bean
    @ConditionalOnProperty(name = "pinpoint.collector.realtime.atc.enable-count-metric", havingValue = "true")
    CountingMetricDao countingMetricDao(ReactiveRedisConnectionFactory connectionFactory) {
        return new RedisCountingMetricDao(connectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean(CountingMetricDao.class)
    CountingMetricDao emptyCountingMetricDao() {
        return new EmptyCountingMetricDao();
    }

    @Bean
    PubChannel<ATCSupply> activeThreadCountSupplyPubChannel(ReactiveRedisConnectionFactory connectionFactory) {
        return new RedisATCSupplyPubChannel(connectionFactory);
    }

    @Bean
    SubChannel<ATCDemand> atcDemandSubChannel(RedisMessageListenerContainer container) {
        return new RedisATCDemandSubChannel(container);
    }

}
