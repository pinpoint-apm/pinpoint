/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.config;


import com.navercorp.pinpoint.web.websocket.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private static final String[] DEFAULT_ALLOWED_ORIGIN = new String[0];

    private static final String WEBSOCKET_SUFFIX = ".pinpointws";

    @Autowired
    private PinpointWebSocketHandlerManager handlerRepository;

    @Autowired
    private ConfigProperties configProperties;

    @Autowired(required = false)
    private WebSocketHandlerDecoratorFactory webSocketHandlerDecoratorFactory = new DefaultWebSocketHandlerDecoratorFactory();

    @Autowired(required = false)
    private CustomHandshakeInterceptor customHandshakeInterceptor = null;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        final String[] allowedOriginArray = getAllowedOriginArray(configProperties.getWebSocketAllowedOrigins());

        for (PinpointWebSocketHandler handler : handlerRepository.getWebSocketHandlerRepository()) {
            String path = handler.getRequestMapping() + WEBSOCKET_SUFFIX;

            WebSocketHandler webSocketHandler = webSocketHandlerDecoratorFactory.decorate(handler);
            final WebSocketHandlerRegistration webSocketHandlerRegistration = registry.addHandler(webSocketHandler, path);

            webSocketHandlerRegistration.addInterceptors(new HttpSessionHandshakeInterceptor());
            webSocketHandlerRegistration.addInterceptors(new WebSocketSessionContextPrepareHandshakeInterceptor());
            if (Objects.nonNull(customHandshakeInterceptor)) {
                webSocketHandlerRegistration.addInterceptors(customHandshakeInterceptor);
            }
            webSocketHandlerRegistration.setAllowedOrigins(allowedOriginArray);
        }
    }

    private String[] getAllowedOriginArray(String allowedOrigins) {
        if (!StringUtils.hasText(allowedOrigins)) {
            return DEFAULT_ALLOWED_ORIGIN;
        }
        return StringUtils.tokenizeToStringArray(allowedOrigins, ",");
    }

}
