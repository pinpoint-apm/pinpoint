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
import com.navercorp.pinpoint.realtime.atc.dto.ATCDemand;
import com.navercorp.pinpoint.realtime.atc.dto.ATCSupply;
import com.navercorp.pinpoint.web.realtime.atc.dao.ATCValueDao;
import com.navercorp.pinpoint.web.realtime.atc.dao.CountingMetricDao;
import com.navercorp.pinpoint.web.realtime.atc.dao.memory.ATCSessionRepository;
import com.navercorp.pinpoint.web.realtime.atc.service.DemandPublishService;
import com.navercorp.pinpoint.web.realtime.atc.service.DemandPublishServiceImpl;
import com.navercorp.pinpoint.web.realtime.atc.service.DemandRegisterService;
import com.navercorp.pinpoint.web.realtime.atc.service.DemandRegisterServiceImpl;
import com.navercorp.pinpoint.web.realtime.atc.service.SupplyFlushService;
import com.navercorp.pinpoint.web.realtime.atc.service.SupplyFlushServiceImpl;
import com.navercorp.pinpoint.web.realtime.atc.service.SupplySubscribeService;
import com.navercorp.pinpoint.web.realtime.atc.service.SupplySubscribeServiceImpl;
import com.navercorp.pinpoint.web.realtime.config.RealtimeWebServiceConfig;
import com.navercorp.pinpoint.web.realtime.service.AgentLookupService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author youngjin.kim2
 */
@Configuration
@Import({ ATCWebDaoConfig.class, RealtimeWebServiceConfig.class })
public class ATCWebServiceConfig {

    @Value("${pinpoint.web.realtime.atc.supply.flush.num-workers:2}")
    int numFlushWorkers;

    @Bean
    DemandPublishService demandPublishService(
            PubChannel<ATCDemand> pubChannel,
            AgentLookupService agentLookupService,
            ATCValueDao valueDao,
            CountingMetricDao countingMetricDao,
            ATCSessionRepository sessionRepository
    ) {
        return new DemandPublishServiceImpl(
                pubChannel,
                agentLookupService,
                valueDao,
                countingMetricDao,
                sessionRepository
        );
    }

    @Bean
    DemandRegisterService demandRegisterService(
            ATCSessionRepository sessionRepository,
            DemandPublishService demandPublishService
    ) {
        return new DemandRegisterServiceImpl(sessionRepository, demandPublishService);
    }

    @Bean
    SupplyFlushService supplyFlushService(
            ATCSessionRepository sessionRepository,
            ATCValueDao valueDao
    ) {
        return new SupplyFlushServiceImpl(sessionRepository, valueDao, numFlushWorkers);
    }

    @Bean
    SupplySubscribeService supplySubscribeService(
            ATCSessionRepository sessionRepository,
            SubChannel<ATCSupply> supplyChannel,
            ATCValueDao valueDao
    ) {
        return new SupplySubscribeServiceImpl(sessionRepository, supplyChannel, valueDao);
    }

}
