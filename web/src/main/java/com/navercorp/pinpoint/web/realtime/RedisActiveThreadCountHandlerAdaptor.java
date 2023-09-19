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
package com.navercorp.pinpoint.web.realtime;

import com.navercorp.pinpoint.web.realtime.activethread.count.websocket.RedisActiveThreadCountWebSocketHandler;
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import com.navercorp.pinpoint.web.websocket.ActiveThreadCountHandler;
import com.navercorp.pinpoint.web.websocket.message.PinpointWebSocketMessageConverter;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class RedisActiveThreadCountHandlerAdaptor extends ActiveThreadCountHandler {

    private final RedisActiveThreadCountWebSocketHandler delegate;

    public RedisActiveThreadCountHandlerAdaptor(
            RedisActiveThreadCountWebSocketHandler delegate,
            PinpointWebSocketMessageConverter converter,
            ServerMapDataFilter serverMapDataFilter,
            String requestMapping
    ) {
        super(converter, serverMapDataFilter, requestMapping);
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override public void start() {}
    @Override public void stop() {}

    @Override
    public void afterConnectionEstablished(@Nonnull WebSocketSession session) {
        this.delegate.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(@Nonnull WebSocketSession session, @Nonnull CloseStatus status) {
        this.delegate.afterConnectionClosed(session, status);
    }

    @Override
    protected void handleActiveThreadCount(WebSocketSession session, String applicationName) {
        this.delegate.handleActiveThreadCount(session, applicationName);
    }

    @Override
    public int getPriority() {
        return 1;
    }

}
