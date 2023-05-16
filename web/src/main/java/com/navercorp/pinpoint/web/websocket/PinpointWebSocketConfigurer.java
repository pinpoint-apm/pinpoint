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

package com.navercorp.pinpoint.web.websocket;


import com.navercorp.pinpoint.web.config.ConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class PinpointWebSocketConfigurer implements WebSocketConfigurer {

    private static final String[] DEFAULT_ALLOWED_ORIGIN = new String[0];

    private static final String WEBSOCKET_SUFFIX = ".pinpointws";

    private final PinpointWebSocketHandlerManager handlerRepository;
    private final ConfigProperties configProperties;
    private final WebSocketHandlerDecoratorFactory webSocketHandlerDecoratorFactory;
    private final CustomHandshakeInterceptor customHandshakeInterceptor;

    public PinpointWebSocketConfigurer(
            PinpointWebSocketHandlerManager handlerRepository,
            ConfigProperties configProperties,
            @Autowired(required = false) WebSocketHandlerDecoratorFactory webSocketHandlerDecoratorFactory,
            @Autowired(required = false) CustomHandshakeInterceptor customHandshakeInterceptor
    ) {
        this.handlerRepository = Objects.requireNonNull(handlerRepository, "handlerRepository");
        this.configProperties = Objects.requireNonNull(configProperties, "configProperties");
        this.webSocketHandlerDecoratorFactory = Objects.requireNonNullElseGet(
                webSocketHandlerDecoratorFactory,
                () -> new DefaultWebSocketHandlerDecoratorFactory()
        );
        this.customHandshakeInterceptor = customHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        final String[] allowedOriginArray = getAllowedOriginArray(configProperties.getWebSocketAllowedOrigins());

        for (PinpointWebSocketHandler handler : handlerRepository.getWebSocketHandlerRepository()) {
            String path = handler.getRequestMapping() + WEBSOCKET_SUFFIX;

            WebSocketHandler webSocketHandler = webSocketHandlerDecoratorFactory.decorate(handler);
            final WebSocketHandlerRegistration webSocketHandlerRegistration = registry.addHandler(webSocketHandler, path);

            webSocketHandlerRegistration.addInterceptors(new HttpSessionHandshakeInterceptor());
            webSocketHandlerRegistration.addInterceptors(new WebSocketSessionContextPrepareHandshakeInterceptor());
            if (customHandshakeInterceptor != null) {
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
