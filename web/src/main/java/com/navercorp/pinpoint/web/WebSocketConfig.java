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
package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.web.cluster.ClusterManager;
import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.service.AgentServiceImpl;
import com.navercorp.pinpoint.web.websocket.ActiveThreadCountHandler;
import com.navercorp.pinpoint.web.websocket.CustomHandshakeInterceptor;
import com.navercorp.pinpoint.web.websocket.PinpointWebSocketConfigurer;
import com.navercorp.pinpoint.web.websocket.PinpointWebSocketHandler;
import com.navercorp.pinpoint.web.websocket.PinpointWebSocketHandlerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import java.util.List;

/**
 * @author youngjin.kim2
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig {

    @Bean
    WebSocketConfigurer webSocketConfigurer(
            PinpointWebSocketHandlerManager handlerRepository,
            ConfigProperties configProperties,
            @Autowired(required = false) WebSocketHandlerDecoratorFactory webSocketHandlerDecoratorFactory,
            @Autowired(required = false) CustomHandshakeInterceptor customHandshakeInterceptor
    ) {
        return new PinpointWebSocketConfigurer(
                handlerRepository,
                configProperties,
                webSocketHandlerDecoratorFactory,
                customHandshakeInterceptor
        );
    }

    @Bean
    AgentService agentService(
            AgentInfoService agentInfoService,
            ClusterManager clusterManager,
            @Qualifier("commandHeaderTBaseSerializerFactory") SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory,
            @Qualifier("commandHeaderTBaseDeserializerFactory") DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory
    ) {
        return new AgentServiceImpl(
                agentInfoService,
                clusterManager,
                commandSerializerFactory,
                commandDeserializerFactory
        );
    }

    @Bean
    PinpointWebSocketHandler activeThreadHandler(AgentService agentService) {
        return new ActiveThreadCountHandler("/agent/activeThread", agentService);
    }

    @Bean
    PinpointWebSocketHandlerManager handlerRegister(List<PinpointWebSocketHandler> handlers) {
        return new PinpointWebSocketHandlerManager(handlers);
    }

}
