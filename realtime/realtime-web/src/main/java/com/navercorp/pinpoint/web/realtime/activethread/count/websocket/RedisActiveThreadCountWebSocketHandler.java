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
package com.navercorp.pinpoint.web.realtime.activethread.count.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.channel.serde.JacksonSerde;
import com.navercorp.pinpoint.web.realtime.activethread.count.dto.ActiveThreadCountResponse;
import com.navercorp.pinpoint.web.realtime.activethread.count.service.ActiveThreadCountService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.serializer.Serializer;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import reactor.core.Disposable;

import java.io.IOException;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class RedisActiveThreadCountWebSocketHandler {

    private static final Logger logger = LogManager.getLogger(RedisActiveThreadCountWebSocketHandler.class);

    private final ActiveThreadCountService atcService;
    private final Serializer<ActiveThreadCountResponse> responseSerializer;

    public RedisActiveThreadCountWebSocketHandler(
            ActiveThreadCountService atcSessionFactory,
            ObjectMapper objectMapper
    ) {
        this.atcService = Objects.requireNonNull(atcSessionFactory, "atcSessionFactory");
        this.responseSerializer = JacksonSerde.byClass(objectMapper, ActiveThreadCountResponse.class);
    }

    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        logger.info("ATC Connection Established. session: {}", session);
        HandlerSession.initialize(session, this.atcService, this.responseSerializer);
    }

    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        logger.info("ATC Connection Closed. session: {}, status: {}", session, status);
        HandlerSession.dispose(session);
    }

    public void handleActiveThreadCount(WebSocketSession wsSession, String applicationName) {
        logger.info("ATC Requested. session: {}, applicationName: {}", wsSession, applicationName);
        HandlerSession handlerSession = HandlerSession.get(wsSession);
        if (handlerSession == null) {
            logger.error("CustomSession is not initialized");
            return;
        }
        handlerSession.start(applicationName);
    }

    private static class HandlerSession implements Disposable {

        private static final String ATTR_KEY = "handlerSession";

        private final WebSocketSession wsSession;
        private final ActiveThreadCountService atcService;
        private final Serializer<ActiveThreadCountResponse> responseSerializer;
        private String applicationName;
        private Disposable disposable;

        private final Object lock = new Object();

        private HandlerSession(
                WebSocketSession wsSession,
                ActiveThreadCountService atcService,
                Serializer<ActiveThreadCountResponse> responseSerializer
        ) {
            this.wsSession = wsSession;
            this.atcService = atcService;
            this.responseSerializer = responseSerializer;
        }

        public static HandlerSession get(WebSocketSession wsSession) {
            Object t = wsSession.getAttributes().get(ATTR_KEY);
            if (t instanceof HandlerSession handlerSession) {
                return handlerSession;
            }
            return null;
        }

        public static void initialize(
                WebSocketSession wsSession,
                ActiveThreadCountService atcService,
                Serializer<ActiveThreadCountResponse> responseSerializer
        ) {
            HandlerSession prev = get(wsSession);
            if (prev != null) {
                return;
            }
            HandlerSession handlerSession = new HandlerSession(wsSession, atcService, responseSerializer);
            wsSession.getAttributes().put(ATTR_KEY, handlerSession);
        }

        public static void dispose(WebSocketSession wsSession) {
            HandlerSession that = get(wsSession);
            if (that != null) {
                that.dispose();
            }
        }

        void start(String applicationName) {
            synchronized (lock) {
                if (this.applicationName != null && this.applicationName.equals(applicationName)) {
                    logger.error("Already started with application {}", this.applicationName);
                    return;
                }
                dispose();
                start0(applicationName);
            }
        }

        private void start0(String applicationName) {
            try {
                this.applicationName = applicationName;
                this.disposable = this.atcService.getResponses(applicationName).subscribe(this::sendMessage);
            } catch (Exception e) {
                logger.error("Failed to start atc session");
                throw new RuntimeException(e);
            }
        }

        @Override
        public void dispose() {
            synchronized (lock) {
                if (this.disposable != null) {
                    this.disposable.dispose();
                }
                this.applicationName = null;
                this.disposable = null;
            }
        }

        private void sendMessage(ActiveThreadCountResponse response) {
            try {
                TextMessage message = new TextMessage(this.responseSerializer.serializeToByteArray(response));
                synchronized (lock) {
                    this.wsSession.sendMessage(message);
                }
            } catch (IOException e) {
                logger.error("Failed to send message to {}", this.wsSession);
            }
        }

    }

}
