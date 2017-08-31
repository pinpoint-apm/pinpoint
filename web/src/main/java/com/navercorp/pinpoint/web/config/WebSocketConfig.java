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


import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.websocket.PinpointWebSocketHandler;
import com.navercorp.pinpoint.web.websocket.PinpointWebSocketHandlerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Arrays;
import java.util.List;

/**
 * @author Taejin Koo
 */
@Configuration
@EnableWebSocket
@Component
public class WebSocketConfig implements WebSocketConfigurer {

    private static final String[] DEFAULT_ALLOWED_ORIGIN = new String[0];

    private static final String WEBSOCKET_SUFFIX = ".pinpointws";

    @Autowired
    private PinpointWebSocketHandlerManager handlerRepository;

    @Autowired
    private ConfigProperties configProperties;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String[] allowedOriginArray = getAllowedOriginArray(configProperties.getWebSocketAllowedOrigins());

        for (PinpointWebSocketHandler handler : handlerRepository.getWebSocketHandlerRepository()) {
            registry.addHandler(handler, handler.getRequestMapping() + WEBSOCKET_SUFFIX).addInterceptors(new HttpSessionHandshakeInterceptor()).setAllowedOrigins(allowedOriginArray);
        }
    }

    private String[] getAllowedOriginArray(String allowedOrigins) {
        if (!StringUtils.hasText(allowedOrigins)) {
            return DEFAULT_ALLOWED_ORIGIN;
        }

        String[] splitString = StringUtils.split(allowedOrigins, ",");
        if (ArrayUtils.isEmpty(splitString)) {
            return new String[]{StringUtils.trimAllWhitespace(allowedOrigins)};
        } else {
            String[] result = new String[splitString.length];
            for (int i = 0; i < splitString.length; i++) {
                result[i] = StringUtils.trimAllWhitespace(splitString[i]);
            }
            return result;
        }
    }

}
