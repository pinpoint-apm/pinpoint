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
package com.navercorp.pinpoint.realtime.collector;

import com.navercorp.pinpoint.realtime.collector.receiver.ClusterPointLocator;
import com.navercorp.pinpoint.realtime.collector.receiver.EmptyClusterPointLocator;
import com.navercorp.pinpoint.realtime.collector.receiver.EmptyCommandService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
@Import({ RealtimeCollectorServerConfig.class })
public class RealtimeCollectorModule {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(value = "pinpoint.modules.realtime.enabled", havingValue = "false", matchIfMissing = true)
    public static class DisabledRealtimeCollectorConfig {

        @Bean("commandService")
        public EmptyCommandService emptyCommandService() {
            return new EmptyCommandService();
        }

        @Bean
        public ClusterPointLocator emptyClusterPointLocator() {
            return new EmptyClusterPointLocator();
        }

    }

}
