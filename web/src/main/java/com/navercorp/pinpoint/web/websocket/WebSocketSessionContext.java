/*
 * Copyright 2017 NAVER Corp.
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

import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Woonduk Kang(emeroad)
 */
public class WebSocketSessionContext {

    public static final String WEBSOCKET_SESSION_CONTEXT_KEY = "pinpoint.websocket.session.context.key";

    private final AtomicBoolean healthCheckSuccess;
    private String applicationName;

    static WebSocketSessionContext getSessionContext(WebSocketSession webSocketSession) {
        final Object context = webSocketSession.getAttributes().get(WEBSOCKET_SESSION_CONTEXT_KEY);
        if (context instanceof WebSocketSessionContext) {
            return (WebSocketSessionContext) context;
        }
        return null;
    }

    public WebSocketSessionContext() {
        this.healthCheckSuccess = new AtomicBoolean(true);
    }

    public boolean changeHealthCheckSuccess() {
        return healthCheckSuccess.compareAndSet(false, true);
    }

    public boolean changeHealthCheckFail() {
        return healthCheckSuccess.compareAndSet(true, false);
    }

    public boolean getHealthCheckState() {
        return healthCheckSuccess.get();
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public String toString() {
        return "WebSocketSessionContext{" +
                "changeHealthCheckSuccess=" + healthCheckSuccess +
                ", applicationName='" + applicationName + '\'' +
                '}';
    }
}
