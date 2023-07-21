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
package com.navercorp.pinpoint.web.realtime.activethread.count.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.navercorp.pinpoint.web.realtime.activethread.count.dao.ActiveThreadCountDao;
import com.navercorp.pinpoint.web.realtime.activethread.count.dao.ActiveThreadCountWebDaoConfig;
import com.navercorp.pinpoint.web.realtime.service.AgentLookupService;
import com.navercorp.pinpoint.web.realtime.service.RealtimeWebServiceConfig;
import com.navercorp.pinpoint.web.task.TimerTaskDecoratorFactory;
import com.navercorp.pinpoint.web.websocket.PinpointWebSocketTimerTaskDecoratorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
@Import({ActiveThreadCountWebDaoConfig.class, RealtimeWebServiceConfig.class})
public class ActiveThreadCountWebServiceConfig {

    @Value("${pinpoint.web.realtime.atc.periods.emit:PT1S}")
    Duration periodEmit;
    @Value("${pinpoint.web.realtime.atc.periods.refresh:PT10S}")
    Duration periodRefresh;
    @Value("${pinpoint.web.realtime.atc.periods.update:PT30S}")
    Duration periodUpdate;

    @Bean("pubSubATCSessionScheduledExecutor")
    ScheduledExecutorService pubSubATCSessionScheduledExecutor() {
        return Executors.newScheduledThreadPool(
                Runtime.getRuntime().availableProcessors(),
                new ThreadFactoryBuilder().setNameFormat("pubSubATCSessionScheduledExecutor-%d").build()
        );
    }

    @Bean
    ActiveThreadCountService activeThreadCountService(
            ActiveThreadCountDao atcDao,
            AgentLookupService agentLookupService,
            @Qualifier("pubSubATCSessionScheduledExecutor") ScheduledExecutorService scheduledExecutor,
            @Autowired(required = false) @Nullable TimerTaskDecoratorFactory timerTaskDecoratorFactory
    ) {
        return new ActiveThreadCountServiceImpl(
                atcDao,
                agentLookupService,
                scheduledExecutor,
                new ActiveThreadCountSessionImpl.ATCPeriods(periodEmit, periodRefresh, periodUpdate),
                Objects.requireNonNullElseGet(timerTaskDecoratorFactory, () -> new PinpointWebSocketTimerTaskDecoratorFactory())
        );
    }

}
