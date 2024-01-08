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
package com.navercorp.pinpoint.log.web.websocket;

import com.navercorp.pinpoint.log.vo.FileKey;
import com.navercorp.pinpoint.log.web.service.LiveTailService;
import com.navercorp.pinpoint.log.web.vo.LiveTailBatch;
import com.navercorp.pinpoint.web.websocket.PinpointWebSocketHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.serializer.Serializer;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.Disposable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class LogWebSocketHandler extends TextWebSocketHandler implements PinpointWebSocketHandler {

    private static final String LIVE_TAIL_DISPOSABLE_ATTR = "pinpoint.live-tail.subscription";

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final Serializer<List<LiveTailBatch>> logPileSerializer;

    private final LiveTailService liveTailService;

    LogWebSocketHandler(
            LiveTailService liveTailService,
            Serializer<List<LiveTailBatch>> logPileSerializer
    ) {
        this.liveTailService = Objects.requireNonNull(liveTailService, "liveTailService");
        this.logPileSerializer = Objects.requireNonNull(logPileSerializer, "logPileSerializer");
    }

    @Override
    public void afterConnectionEstablished(@Nonnull WebSocketSession session) throws Exception {
        try {
            if (session instanceof StandardWebSocketSession webSocketSession) {
                startLiveTail(webSocketSession);
            } else {
                logger.error("Failed to handle live-tail: session is not an instance of StandardWebSocketSession");
                throw new RuntimeException("Failed to handle live-tail: session is not an instance of " +
                        "StandardWebSocketSession");
            }
        } catch (Exception e) {
            logger.error("Failed to handle live-tail", e);
            stopLiveTail(session);
            session.close();
            return;
        }

        super.afterConnectionEstablished(session);
    }

    private void startLiveTail(StandardWebSocketSession session) {
        logger.info("Starting live-tail, session: {}", session);

        Map<String, List<String>> params = session
                .getNativeSession()
                .getRequestParameterMap();

        String hostGroupName = getUniParam(params, "hostGroupName");
        List<String> hostNames = split(getUniParam(params, "hostNames"));
        List<String> fileNames = split(getUniParam(params, "fileNames"));

        List<FileKey> fileKeys = this.liveTailService.getFileKeys(hostGroupName, hostNames, fileNames);
        Disposable disposable = this.liveTailService.tail(fileKeys)
                .subscribe(supply -> sendSupply(session, supply));
        session.getAttributes().put(LIVE_TAIL_DISPOSABLE_ATTR, disposable);
    }

    private static String getUniParam(Map<String, List<String>> params, String key) {
        return CollectionUtils.firstElement(params.get(key));
    }

    private static List<String> split(String str) {
        if (str == null) {
            return List.of();
        }
        return List.of(str.split(","));
    }

    private void stopLiveTail(@Nonnull WebSocketSession session) {
        logger.info("Stopping live-tail, session: {}", session);
        Object disposable = session.getAttributes().get(LIVE_TAIL_DISPOSABLE_ATTR);
        if (disposable instanceof Disposable dis) {
            dis.dispose();
        }
    }

    @Override
    public void afterConnectionClosed(@Nonnull WebSocketSession session, @Nonnull CloseStatus status) throws Exception {
        stopLiveTail(session);
        super.afterConnectionClosed(session, status);
    }


    private void sendSupply(WebSocketSession session, List<LiveTailBatch> pile) {
        try {
            byte[] payload = this.logPileSerializer.serializeToByteArray(pile);
            session.sendMessage(new TextMessage(payload));
        } catch (Exception e) {
            stopLiveTail(session);
            logger.error("Failed to send message", e);
        }
    }

    @Override public void start() {}
    @Override public void stop() {}

    @Override
    public String getRequestMapping() {
        return "/log/liveTail";
    }

    @Override
    public int getPriority() {
        return 0;
    }

}
