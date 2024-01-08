/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.websocket;

import com.navercorp.pinpoint.rpc.util.MapUtils;
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import com.navercorp.pinpoint.web.websocket.message.PinpointWebSocketMessage;
import com.navercorp.pinpoint.web.websocket.message.PinpointWebSocketMessageConverter;
import com.navercorp.pinpoint.web.websocket.message.PinpointWebSocketMessageType;
import com.navercorp.pinpoint.web.websocket.message.RequestMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public abstract class ActiveThreadCountHandler extends TextWebSocketHandler implements PinpointWebSocketHandler {

    public static final String APPLICATION_NAME_KEY = "applicationName";
    public static final String DEFAULT_REQUEST_MAPPING = "/agent/activeThread";

    static final String API_ACTIVE_THREAD_COUNT = "activeThreadCount";

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final PinpointWebSocketMessageConverter messageConverter;
    private final String requestMapping;
    private final ServerMapDataFilter serverMapDataFilter;

    public ActiveThreadCountHandler(
            PinpointWebSocketMessageConverter converter,
            ServerMapDataFilter serverMapDataFilter,
            String requestMapping
    ) {
        this.messageConverter = Objects.requireNonNull(converter, "converter");
        this.serverMapDataFilter = serverMapDataFilter;
        this.requestMapping = Objects.requireNonNullElse(requestMapping, DEFAULT_REQUEST_MAPPING);
    }

    @Override
    public String getRequestMapping() {
        return requestMapping;
    }

    private WebSocketSessionContext getSessionContext(WebSocketSession webSocketSession) {
        final WebSocketSessionContext sessionContext = WebSocketSessionContext.getSessionContext(webSocketSession);
        if (sessionContext == null) {
            throw new IllegalStateException("WebSocketSessionContext not initialized");
        }
        return sessionContext;
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession webSocketSession, TextMessage message) throws Exception {
        logger.info("handleTextMessage. session:{}, remote:{}, message:{}.", webSocketSession, webSocketSession.getRemoteAddress(), message.getPayload());

        PinpointWebSocketMessage webSocketMessage = messageConverter.getWebSocketMessage(message.getPayload());
        PinpointWebSocketMessageType webSocketMessageType = webSocketMessage.getType();
        switch (webSocketMessageType) {
            case REQUEST -> handleRequestMessage0(webSocketSession, (RequestMessage) webSocketMessage);
            case PONG -> handlePongMessage0(webSocketSession);
            default -> logger.warn("Unexpected WebSocketMessageType received. messageType:{}.", webSocketMessageType);
        }

        // this method will be checked socket status.
        super.handleTextMessage(webSocketSession, message);
    }

    private void handleRequestMessage0(WebSocketSession webSocketSession, RequestMessage requestMessage) {
        if (serverMapDataFilter != null && serverMapDataFilter.filter(webSocketSession, requestMessage)) {
            closeSession(webSocketSession, serverMapDataFilter.getCloseStatus(requestMessage));
            return;
        }
        
        final String command = requestMessage.getCommand();
        if (API_ACTIVE_THREAD_COUNT.equals(command)) {
            handleActiveThreadCount(webSocketSession, requestMessage);
        } else {
            logger.debug("unknown command:{}", command);
        }
    }

    private void handleActiveThreadCount(WebSocketSession webSocketSession, RequestMessage requestMessage) {
        final String applicationName = MapUtils.getString(requestMessage.getParameters(), APPLICATION_NAME_KEY);
        if (applicationName != null) {
            handleActiveThreadCount(webSocketSession, applicationName);
        }
    }

    protected abstract void handleActiveThreadCount(WebSocketSession webSocketSession, String applicationName);

    private void closeSession(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    private void handlePongMessage0(WebSocketSession webSocketSession) {
        final WebSocketSessionContext sessionContext = getSessionContext(webSocketSession);
        sessionContext.changeHealthCheckSuccess();
    }

    @Override
    protected void handlePongMessage(@NonNull WebSocketSession webSocketSession, org.springframework.web.socket.PongMessage message) throws Exception {
        logger.info("handlePongMessage. session:{}, remote:{}, message:{}.", webSocketSession, webSocketSession.getRemoteAddress(), message.getPayload());

        super.handlePongMessage(webSocketSession, message);
    }

}
