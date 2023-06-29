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
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class PinpointWebSocketConfigurer implements WebSocketConfigurer {

    private static final String[] DEFAULT_ALLOWED_ORIGIN = new String[0];

    private static final List<String> WEBSOCKET_PREFIX_LIST = List.of("/", "/api/");
    private static final String WEBSOCKET_SUFFIX = "";

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
        for (final String prefix: WEBSOCKET_PREFIX_LIST) {
            final WebSocketHandler webSocketHandler = webSocketHandlerDecoratorFactory.decorate(handler);
            final String path = pathJoin(prefix, handler.getRequestMapping(), WEBSOCKET_SUFFIX);
            this.setupRegistration(registry.addHandler(webSocketHandler, path));
        }
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

    private static String pathJoin(String prefix, String main, String suffix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        if (!main.startsWith("/")) {
            sb.append("/");
        }
        sb.append(main);
        if (!main.endsWith("/")) {
            sb.append("/");
        }
        sb.append(suffix);
        return sb.toString();
    }

}
