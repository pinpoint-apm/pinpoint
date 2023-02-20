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
package com.navercorp.pinpoint.web.realtime.atc.dto;

import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * @author youngjin.kim2
 */
public class ATCSession {

    private final WebSocketSession webSocketSession;
    private final long createdAt;
    private String demandApplicationName = null;

    private ATCSession(WebSocketSession webSocketSession) {
        this.webSocketSession = webSocketSession;
        this.createdAt = System.nanoTime();
    }

    public static ATCSession of(WebSocketSession webSocketSession) {
        return new ATCSession(webSocketSession);
    }

    public WebSocketSession getWebSocketSession() {
        return webSocketSession;
    }

    public void setDemand(String applicationName) {
        this.demandApplicationName = applicationName;
    }

    public String getDemandApplicationName() {
        return demandApplicationName;
    }

    public void sendMessage(WebSocketMessage<?> message) throws IOException {
        webSocketSession.sendMessage(message);
    }

    public long getCreatedAt() {
        return createdAt;
    }

}
