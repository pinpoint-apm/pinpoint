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
package com.navercorp.pinpoint.web.realtime.activethread.count.websocket;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.realtime.dto.ATCSupply;
import com.navercorp.pinpoint.web.realtime.activethread.count.dao.ActiveThreadCountWebDaoConfig;
import com.navercorp.pinpoint.web.realtime.activethread.count.dao.FetcherFactory;
import com.navercorp.pinpoint.web.realtime.service.AgentLookupService;
import com.navercorp.pinpoint.web.realtime.service.RealtimeWebServiceConfig;
import com.navercorp.pinpoint.web.task.TimerTaskDecoratorFactory;
import com.navercorp.pinpoint.web.websocket.PinpointWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/**
 * @author youngjin.kim2
 */
@Configuration
@Import({ ActiveThreadCountWebDaoConfig.class, RealtimeWebServiceConfig.class })
public class ActiveThreadCountWebSocketConfig {

    @Bean
    PinpointWebSocketHandler redisActiveThreadCountHandler(
            FetcherFactory<ClusterKey, ATCSupply> fetcherFactory,
            AgentLookupService agentLookupService,
            @Autowired(required = false) TimerTaskDecoratorFactory timerTaskDecoratorFactory
    ) {
        return new ActiveThreadCountHandlerImpl(
                fetcherFactory,
                agentLookupService,
                timerTaskDecoratorFactory,
                Flux.interval(Duration.ofMillis(1000), Schedulers.newParallel("atcFlusher", 8))
        );
    }

}
