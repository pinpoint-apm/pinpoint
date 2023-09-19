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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.service.AgentServiceImpl;
import com.navercorp.pinpoint.web.websocket.CustomHandshakeInterceptor;
import com.navercorp.pinpoint.web.websocket.PinpointWebSocketConfigurer;
import com.navercorp.pinpoint.web.websocket.PinpointWebSocketHandler;
import com.navercorp.pinpoint.web.websocket.PinpointWebSocketHandlerManager;
import com.navercorp.pinpoint.web.websocket.message.PinpointWebSocketMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
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
    public WebSocketConfigurer webSocketConfigurer(
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
    public AgentService agentService(AgentInfoService agentInfoService) {
        return new AgentServiceImpl(agentInfoService);
    }

    @Bean
    public PinpointWebSocketHandlerManager handlerRegister(List<PinpointWebSocketHandler> handlers) {
        return new PinpointWebSocketHandlerManager(handlers);
    }

    @Bean
    public PinpointWebSocketMessageConverter pinpointWebSocketMessageConverter(ObjectMapper mapper) {
        return new PinpointWebSocketMessageConverter(mapper);
    }

}
