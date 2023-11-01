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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class PinpointWebSocketHandlerManager {

    private final List<PinpointWebSocketHandler> webSocketHandlerRepository;

    public PinpointWebSocketHandlerManager(List<PinpointWebSocketHandler> pinpointWebSocketHandlers) {
        this.webSocketHandlerRepository = normalize(pinpointWebSocketHandlers);
    }

    private List<PinpointWebSocketHandler> normalize(List<PinpointWebSocketHandler> handlers) {
        Map<String, PinpointWebSocketHandler> handlerMap = new HashMap<>(handlers.size());
        for (final PinpointWebSocketHandler handler: handlers) {
            final String requestMapping = handler.getRequestMapping();
            final int priority = handler.getPriority();

            final PinpointWebSocketHandler prev = handlerMap.get(requestMapping);
            if (prev == null || prev.getPriority() < priority) {
                handlerMap.put(requestMapping, handler);
            }
        }
        return new ArrayList<>(handlerMap.values());
    }

    @PostConstruct
    public void setUp() {
        for (PinpointWebSocketHandler handler : webSocketHandlerRepository) {
            handler.start();
        }
    }

    @PreDestroy
    public void tearDown() {
        for (PinpointWebSocketHandler handler : webSocketHandlerRepository) {
            handler.stop();
        }
    }

    public List<PinpointWebSocketHandler> getWebSocketHandlerRepository() {
        return Collections.unmodifiableList(webSocketHandlerRepository);
    }

}
