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
package com.navercorp.pinpoint.realtime.collector.activethread.count.service;

import com.navercorp.pinpoint.realtime.collector.service.AgentCommandService;
import com.navercorp.pinpoint.realtime.collector.service.RealtimeCollectorServiceConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author youngjin.kim2
 */
@Configuration
@Import({ RealtimeCollectorServiceConfig.class })
public class CollectorActiveThreadCountServiceConfig {

    @Value("${pinpoint.collector.realtime.atc.demand.duration:14500}")
    private long demandDurationMillis;

    @Value("${pinpoint.collector.realtime.atc.supply.throttle.termMillis:100}")
    private long throttleTermMillis;

    @Bean
    @ConditionalOnBean(name = "commandHeaderTBaseDeserializerFactory")
    ActiveThreadCountService activeThreadCountService(AgentCommandService agentCommandService) {
        return new ActiveThreadCountServiceImpl(agentCommandService, demandDurationMillis, throttleTermMillis);
    }

}
