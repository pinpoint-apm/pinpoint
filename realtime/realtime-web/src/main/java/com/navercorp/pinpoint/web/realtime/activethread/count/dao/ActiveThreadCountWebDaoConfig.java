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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubClientFactory;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubFluxClient;
import com.navercorp.pinpoint.realtime.RealtimePubSubServiceDescriptors;
import com.navercorp.pinpoint.realtime.dto.ATCDemand;
import com.navercorp.pinpoint.realtime.dto.ATCSupply;
import com.navercorp.pinpoint.redis.pubsub.RedisPubSubConfig;
import com.navercorp.pinpoint.web.realtime.RealtimeWebCommonConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.concurrent.TimeUnit;

/**
 * @author youngjin.kim2
 */
@Configuration
@Import({ RealtimeWebCommonConfig.class, RedisPubSubConfig.class })
public class ActiveThreadCountWebDaoConfig {

    @Value("${pinpoint.web.realtime.atc.supply.expireInMs:3000}")
    private long supplyExpireInMs;

    @Value("${pinpoint.web.realtime.atc.supply.prepareInMs:10000}")
    private long prepareInMs;

    @Bean
    PubSubFluxClient<ATCDemand, ATCSupply> atcEndpoint(PubSubClientFactory clientFactory) {
        return clientFactory.build(RealtimePubSubServiceDescriptors.ATC);
    }

    @Bean
    Cache<ClusterKey, Fetcher<ATCSupply>> fetcherCache() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .initialCapacity(512)
                .maximumSize(65536)
                .build();
    }

    @Bean
    FetcherFactory<ClusterKey, ATCSupply> atcSupplyFetcherFactory(
            PubSubFluxClient<ATCDemand, ATCSupply> endpoint,
            Cache<ClusterKey, Fetcher<ATCSupply>> fetcherCache
    ) {
        final long recordMaxAgeNanos = TimeUnit.MILLISECONDS.toNanos(supplyExpireInMs);
        final long prepareInNanos = TimeUnit.MILLISECONDS.toNanos(prepareInMs);
        final ActiveThreadCountFetcherFactory fetcherFactory
                = new ActiveThreadCountFetcherFactory(endpoint, recordMaxAgeNanos, prepareInNanos);
        return new CachedFetcherFactory<>(fetcherFactory, fetcherCache);
    }

}
