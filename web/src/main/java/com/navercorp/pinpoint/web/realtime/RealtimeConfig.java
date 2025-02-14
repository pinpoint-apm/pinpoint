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

import com.navercorp.pinpoint.common.server.task.TaskDecoratorFactory;
import com.navercorp.pinpoint.common.server.frontend.export.FrontendConfigExporter;
import com.navercorp.pinpoint.web.realtime.activethread.count.dao.ActiveThreadCountDao;
import com.navercorp.pinpoint.web.realtime.activethread.count.service.ActiveThreadCountService;
import com.navercorp.pinpoint.web.realtime.activethread.count.service.ActiveThreadCountServiceImpl;
import com.navercorp.pinpoint.web.realtime.activethread.count.websocket.RedisActiveThreadCountWebSocketHandler;
import com.navercorp.pinpoint.web.realtime.activethread.dump.RedisActiveThreadDumpService;
import com.navercorp.pinpoint.web.realtime.echo.RedisEchoService;
import com.navercorp.pinpoint.web.realtime.service.AgentLookupService;
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import com.navercorp.pinpoint.web.service.ActiveThreadDumpService;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.service.ApplicationAgentListService;
import com.navercorp.pinpoint.web.service.EchoService;
import com.navercorp.pinpoint.web.websocket.PinpointWebSocketHandler;
import com.navercorp.pinpoint.web.websocket.WebSocketTaskDecoratorFactory;
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

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
public class RealtimeConfig {

    @Value("${pinpoint.modules.realtime.enabled:false}")
    boolean activatedRealtime;

    @Bean
    FrontendConfigExporter realtimeFrontendConfigExporter() {
        return new RealtimeFrontendConfigExporter(activatedRealtime, activatedRealtime);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "pinpoint.modules.realtime.enabled", havingValue = "true")
    @Import(RedisRealtimeWebModule.class)
    public static class ActiveThreadCountConfig {

        @Value("${pinpoint.web.realtime.agent-recentness:PT5M}")
        Duration agentRecentness;

        @Bean
        AgentLookupService agentLookupService(ApplicationAgentListService applicationAgentListService) {
            return new AgentLookupServiceImpl(applicationAgentListService, agentRecentness);
        }

        @Bean
        PinpointWebSocketHandler redisActiveThreadCountHandler(
                RedisActiveThreadCountWebSocketHandler delegate,
                PinpointWebSocketMessageConverter converter,
                @Autowired(required = false) ServerMapDataFilter serverMapDataFilter
        ) {
            return new RedisActiveThreadCountHandlerAdaptor(delegate, converter, serverMapDataFilter, null);
        }

        @Bean
        ActiveThreadCountService activeThreadCountService(
                ActiveThreadCountDao atcDao,
                AgentLookupService agentLookupService,
                @Qualifier("pubSubATCSessionScheduledExecutor") ScheduledExecutorService scheduledExecutor,
                ActiveThreadCountService.ATCPeriods atcPeriods,
                @Autowired(required = false) @Nullable TaskDecoratorFactory taskDecoratorFactory
        ) {
            return new ActiveThreadCountServiceImpl(
                    atcDao,
                    agentLookupService,
                    Objects.requireNonNullElseGet(taskDecoratorFactory,
                            WebSocketTaskDecoratorFactory::new),
                    scheduledExecutor,
                    atcPeriods.getPeriodEmit(),
                    atcPeriods.getPeriodUpdate()
            );
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "pinpoint.modules.realtime.enabled", havingValue = "true")
    @Import(RedisRealtimeWebModule.class)
    public static class ActiveThreadDumpConfig {

        @Bean
        ActiveThreadDumpService redisActiveThreadDumpService(RedisActiveThreadDumpService delegate) {
            return new RedisActiveThreadDumpServiceAdaptor(delegate);
        }

    }

    @Bean
    @ConditionalOnProperty(name = "pinpoint.modules.realtime.enabled", havingValue = "false", matchIfMissing = true)
    ActiveThreadDumpService emptyActiveThreadDumpService() {
        return new EmptyActiveThreadDumpService();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "pinpoint.modules.realtime.enabled", havingValue = "true")
    @Import(RedisRealtimeWebModule.class)
    public static class EchoConfig {

        @Bean
        EchoService redisEchoService(RedisEchoService delegate) {
            return new RedisEchoServiceAdaptor(delegate);
        }

        @Bean
        EchoController echoController(AgentService agentService, EchoService echoService) {
            return new EchoController(agentService, echoService);
        }

    }

}
