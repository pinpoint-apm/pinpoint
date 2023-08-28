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
package com.navercorp.pinpoint.web.realtime;

import com.navercorp.pinpoint.common.task.TimerTaskDecoratorFactory;
import com.navercorp.pinpoint.web.realtime.activethread.count.dao.ActiveThreadCountDao;
import com.navercorp.pinpoint.web.realtime.activethread.count.service.ActiveThreadCountService;
import com.navercorp.pinpoint.web.realtime.activethread.count.service.ActiveThreadCountServiceImpl;
import com.navercorp.pinpoint.web.realtime.activethread.count.service.ActiveThreadCountSessionImpl;
import com.navercorp.pinpoint.web.realtime.activethread.count.websocket.RedisActiveThreadCountWebSocketHandler;
import com.navercorp.pinpoint.web.realtime.activethread.dump.RedisActiveThreadDumpService;
import com.navercorp.pinpoint.web.realtime.echo.RedisEchoService;
import com.navercorp.pinpoint.web.realtime.service.AgentLookupService;
import com.navercorp.pinpoint.web.service.ActiveThreadDumpService;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.service.EchoService;
import com.navercorp.pinpoint.web.websocket.PinpointWebSocketHandler;
import com.navercorp.pinpoint.web.websocket.PinpointWebSocketTimerTaskDecoratorFactory;
import com.navercorp.pinpoint.web.websocket.message.PinpointWebSocketMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import static com.navercorp.pinpoint.web.WebSocketConfig.ATC_ENDPOINT;

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "pinpoint.modules.realtime", havingValue = "redis")
@Import(RedisRealtimeWebModule.class)
public class RedisRealtimeConfig {

    @Value("${pinpoint.web.realtime.agent-recentness:PT5S}")
    Duration agentRecentness;

    @Bean
    AgentLookupService agentLookupService(AgentInfoService agentInfoService) {
        return new AgentLookupServiceImpl(agentInfoService, agentRecentness);
    }

    @Bean
    PinpointWebSocketHandler redisActiveThreadCountHandler(
            RedisActiveThreadCountWebSocketHandler delegate,
            PinpointWebSocketMessageConverter converter
    ) {
        return new RedisActiveThreadCountHandlerAdaptor(delegate, converter, ATC_ENDPOINT);
    }

    @Bean
    ActiveThreadCountService activeThreadCountService(
            ActiveThreadCountDao atcDao,
            AgentLookupService agentLookupService,
            @Qualifier("pubSubATCSessionScheduledExecutor") ScheduledExecutorService scheduledExecutor,
            ActiveThreadCountSessionImpl.ATCPeriods atcPeriods,
            @Autowired(required = false) @Nullable TimerTaskDecoratorFactory timerTaskDecoratorFactory
    ) {
        return new ActiveThreadCountServiceImpl(
                atcDao,
                agentLookupService,
                scheduledExecutor,
                atcPeriods,
                Objects.requireNonNullElseGet(timerTaskDecoratorFactory,
                        () -> new PinpointWebSocketTimerTaskDecoratorFactory())
        );
    }

    @Bean
    ActiveThreadDumpService redisActiveThreadDumpService(RedisActiveThreadDumpService delegate) {
        return new RedisActiveThreadDumpServiceAdaptor(delegate);
    }

    @Bean
    EchoService redisEchoService(RedisEchoService delegate) {
        return new RedisEchoServiceAdaptor(delegate);
    }

}
