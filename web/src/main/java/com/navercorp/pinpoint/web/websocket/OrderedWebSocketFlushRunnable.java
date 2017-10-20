/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.web.websocket;

import com.navercorp.pinpoint.web.util.SimpleOrderedThreadPool;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author Taejin Koo
 */
public class OrderedWebSocketFlushRunnable implements Runnable, SimpleOrderedThreadPool.HashSelector {


    private static final Logger LOGGER = LoggerFactory.getLogger(OrderedWebSocketFlushRunnable.class);

    private final WebSocketSession webSocketSession;
    private final TextMessage webSocketMessage;

    private final boolean sessionCloseOnError;

    public OrderedWebSocketFlushRunnable(WebSocketSession webSocketSession, TextMessage webSocketMessage) {
        this(webSocketSession, webSocketMessage, false);
    }

    public OrderedWebSocketFlushRunnable(WebSocketSession webSocketSession, TextMessage webSocketMessage, boolean sessionCloseOnError) {
        if (webSocketSession == null) {
            throw new NullPointerException("webSocketSession null.");
        }
        if (webSocketMessage == null) {
            throw new NullPointerException("webSocketMessage null.");

        }

        this.webSocketSession = webSocketSession;
        this.webSocketMessage = webSocketMessage;
        this.sessionCloseOnError = sessionCloseOnError;
    }

    @Override
    public int select() {
        String webSocketSessionId = webSocketSession.getId();
        if (StringUtils.isEmpty(webSocketSessionId)) {
            webSocketSessionId = RandomStringUtils.random(1);
        }

        return webSocketSessionId.hashCode();
    }

    @Override
    public void run() {
        try {
            webSocketSession.sendMessage(webSocketMessage);
        } catch (Exception e) {
            LOGGER.warn("failed while flushing message to webSocket. session:{}, message:{}, error:{}", webSocketSession, webSocketMessage, e.getMessage(), e);
            if (sessionCloseOnError) {
                closeSession(webSocketSession);
            }
        }
    }

    private void closeSession(WebSocketSession session) {
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

}
