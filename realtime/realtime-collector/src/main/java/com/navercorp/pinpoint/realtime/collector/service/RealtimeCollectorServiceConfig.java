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
package com.navercorp.pinpoint.realtime.collector.service;

import com.navercorp.pinpoint.realtime.collector.dao.CollectorStateDao;
import com.navercorp.pinpoint.realtime.collector.dao.RealtimeCollectorDaoConfig;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import reactor.core.scheduler.Schedulers;

/**
 * @author youngjin.kim2
 */
@Configuration
@Import({ RealtimeCollectorDaoConfig.class })
public class RealtimeCollectorServiceConfig {

    @Bean
    public AgentCommandService agentCommandService(
            AgentConnectionRepository agentConnectionRepository,
            @Qualifier("commandHeaderTBaseDeserializerFactory")
            DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory
    ) {
        return new ClusterAgentCommandService(agentConnectionRepository, deserializerFactory);
    }

    @Bean
    public IntervalRunner periodicConnectionRedisPubChannelEmitter(
            RealtimeCollectorDaoConfig daoConfig,
            CollectorStateDao dao,
            AgentConnectionRepository connectionRepository
    ) {
        Runnable r = new CollectorStateUpdateRunnable(connectionRepository, dao);
        return new IntervalRunner(r, daoConfig.getConnectionListEmitPeriod(), Schedulers.boundedElastic());
    }

}
