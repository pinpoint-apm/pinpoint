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
package com.navercorp.pinpoint.web.realtime.atc.config;

import com.navercorp.pinpoint.pubsub.PubChannel;
import com.navercorp.pinpoint.pubsub.SubChannel;
import com.navercorp.pinpoint.realtime.atc.dao.RedisATCDemandPubChannel;
import com.navercorp.pinpoint.realtime.atc.dao.RedisATCSupplySubChannel;
import com.navercorp.pinpoint.realtime.atc.dto.ATCDemand;
import com.navercorp.pinpoint.realtime.atc.dto.ATCSupply;
import com.navercorp.pinpoint.redis.CommonRedisConfig;
import com.navercorp.pinpoint.web.realtime.atc.dao.ATCValueDao;
import com.navercorp.pinpoint.web.realtime.atc.dao.CountingMetricDao;
import com.navercorp.pinpoint.web.realtime.atc.dao.empty.EmptyCountingMetricDao;
import com.navercorp.pinpoint.web.realtime.atc.dao.memory.ATCSessionRepository;
import com.navercorp.pinpoint.web.realtime.atc.dao.memory.InMemoryATCValueDao;
import com.navercorp.pinpoint.web.realtime.atc.dao.redis.RedisCountingMetricDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.concurrent.TimeUnit;

/**
 * @author youngjin.kim2
 */
@Configuration
@Import({ CommonRedisConfig.class })
public class ATCWebDaoConfig {

    @Value("${pinpoint.web.realtime.atc.supply.expireInMs:3000}")
    long supplyExpireInMs;

    @Bean
    ATCValueDao atcValueDao() {
        final long supplyExpireInNanos = TimeUnit.MILLISECONDS.toNanos(supplyExpireInMs);
        return new InMemoryATCValueDao(supplyExpireInNanos);
    }

    @Bean
    @ConditionalOnProperty(name = "pinpoint.web.realtime.atc.enable-count-metric", havingValue = "true")
    CountingMetricDao countingMetricDao(ReactiveRedisConnectionFactory connectionFactory) {
        return new RedisCountingMetricDao(connectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean(CountingMetricDao.class)
    CountingMetricDao emptyCountingMetricDao() {
        return new EmptyCountingMetricDao();
    }

    @Bean
    ATCSessionRepository atcSessionRepository() {
        return new ATCSessionRepository();
    }

    @Bean
    SubChannel<ATCSupply> atcSupplySubChannel(RedisMessageListenerContainer container) {
        return new RedisATCSupplySubChannel(container);
    }

    @Bean
    PubChannel<ATCDemand> atcDemandPubChannel(ReactiveRedisConnectionFactory connectionFactory) {
        return new RedisATCDemandPubChannel(connectionFactory);
    }

}
