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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class PinpointWebSocketHandlerManager {

    private final List<PinpointWebSocketHandler> webSocketHandlerRepository;

    public PinpointWebSocketHandlerManager(List<PinpointWebSocketHandler> pinpointWebSocketHandlers) {
        webSocketHandlerRepository = Collections.unmodifiableList(new ArrayList<>(pinpointWebSocketHandlers));
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
        return new ArrayList<>(webSocketHandlerRepository);
    }

}
