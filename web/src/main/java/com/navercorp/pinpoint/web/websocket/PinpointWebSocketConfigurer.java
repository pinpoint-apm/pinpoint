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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger logger = LogManager.getLogger(PinpointWebSocketConfigurer.class);

    private static final String[] DEFAULT_ALLOWED_ORIGIN = new String[0];

    private final PinpointWebSocketHandlerManager handlerRepository;
    private final String[] allowedOrigins;
    private final WebSocketHandlerDecoratorFactory webSocketHandlerDecoratorFactory;
    private final CustomHandshakeInterceptor customHandshakeInterceptor;

    public PinpointWebSocketConfigurer(
            PinpointWebSocketHandlerManager handlerRepository,
            ConfigProperties configProperties,
            WebSocketHandlerDecoratorFactory webSocketHandlerDecoratorFactory,
            CustomHandshakeInterceptor customHandshakeInterceptor
    ) {
        this.handlerRepository = Objects.requireNonNull(handlerRepository, "handlerRepository");
        Objects.requireNonNull(configProperties, "configProperties");
        this.webSocketHandlerDecoratorFactory = Objects.requireNonNullElseGet(
                webSocketHandlerDecoratorFactory,
                () -> new DefaultWebSocketHandlerDecoratorFactory()
        );
        this.customHandshakeInterceptor = customHandshakeInterceptor;

        this.allowedOrigins = getAllowedOriginArray(configProperties.getWebSocketAllowedOrigins());
    }

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        for (PinpointWebSocketHandler handler : handlerRepository.getWebSocketHandlerRepository()) {
            this.registerPinpointWebSocketHandler(registry, handler);
        }
    }

    private void registerPinpointWebSocketHandler(WebSocketHandlerRegistry registry, PinpointWebSocketHandler handler) {
        final WebSocketHandler webSocketHandler = webSocketHandlerDecoratorFactory.decorate(handler);

        logger.info("Registering WebSocketHandler {} for path {}",
                webSocketHandler.getClass().getSimpleName(), handler.getRequestMapping());
        this.setupRegistration(registry.addHandler(webSocketHandler, handler.getRequestMapping()));
    }

    private void setupRegistration(WebSocketHandlerRegistration registration) {
        registration.addInterceptors(new HttpSessionHandshakeInterceptor());
        registration.addInterceptors(new WebSocketSessionContextPrepareHandshakeInterceptor());
        if (this.customHandshakeInterceptor != null) {
            registration.addInterceptors(this.customHandshakeInterceptor);
        }
        registration.setAllowedOrigins(this.allowedOrigins);
    }

    private static String[] getAllowedOriginArray(String allowedOrigins) {
        if (!StringUtils.hasText(allowedOrigins)) {
            return DEFAULT_ALLOWED_ORIGIN;
        }
        return StringUtils.tokenizeToStringArray(allowedOrigins, ",");
    }

}
