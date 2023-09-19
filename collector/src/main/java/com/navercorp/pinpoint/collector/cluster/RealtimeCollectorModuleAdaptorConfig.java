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
package com.navercorp.pinpoint.collector.cluster;

import com.navercorp.pinpoint.collector.cluster.route.StreamRouteHandler;
import com.navercorp.pinpoint.realtime.collector.RealtimeCollectorModule;
import com.navercorp.pinpoint.realtime.collector.service.AgentConnectionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "pinpoint.modules.realtime.enabled", havingValue = "true")
@Import(RealtimeCollectorModule.class)
public class RealtimeCollectorModuleAdaptorConfig {

    @Bean
    @ConditionalOnBean(StreamRouteHandler.class)
    AgentConnectionRepository agentConnectionRepository(
            StreamRouteHandler streamRouteHandler,
            ClusterPointRepository<?> clusterPointRepository
    ) {
        return new AgentConnectionRepositoryImpl(streamRouteHandler, clusterPointRepository);
    }

}
