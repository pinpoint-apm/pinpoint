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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.SmartLifecycle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Starts and stops the websocket handlers, driven by the context as a {@link SmartLifecycle}.
 * <p>
 * The {@link #PHASE} is below the phase in which Spring Boot starts and stops the web server
 * ({@code DEFAULT_PHASE - 2048}), so the handlers are started before the port is opened and
 * stopped after it is closed.
 *
 * @author Taejin Koo
 */
public class PinpointWebSocketHandlerManager implements SmartLifecycle {

    public static final int PHASE = SmartLifecycle.DEFAULT_PHASE - 4096;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final List<PinpointWebSocketHandler> webSocketHandlerRepository;

    private volatile boolean running;

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

    @Override
    public void start() {
        if (this.running) {
            return;
        }
        logger.info("Start websocket handlers. handlers:{}", webSocketHandlerRepository.size());
        for (PinpointWebSocketHandler handler : webSocketHandlerRepository) {
            handler.start();
        }
        this.running = true;
    }

    @Override
    public void stop() {
        logger.info("Stop websocket handlers. handlers:{}", webSocketHandlerRepository.size());
        for (PinpointWebSocketHandler handler : webSocketHandlerRepository) {
            handler.stop();
        }
        this.running = false;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public int getPhase() {
        return PHASE;
    }

    public List<PinpointWebSocketHandler> getWebSocketHandlerRepository() {
        return Collections.unmodifiableList(webSocketHandlerRepository);
    }

}
