/*
 * Copyright 2018 NAVER Corp.
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

import org.junit.jupiter.api.Test;
import org.springframework.context.SmartLifecycle;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PinpointWebSocketHandlerManagerTest {

    private static PinpointWebSocketHandler handler(String requestMapping, int priority) {
        PinpointWebSocketHandler handler = mock(PinpointWebSocketHandler.class);
        when(handler.getRequestMapping()).thenReturn(requestMapping);
        when(handler.getPriority()).thenReturn(priority);
        return handler;
    }

    @Test
    void handlersAreStartedAndStoppedByTheLifecycle() {
        PinpointWebSocketHandler handler1 = handler("/a", 0);
        PinpointWebSocketHandler handler2 = handler("/b", 0);
        PinpointWebSocketHandlerManager manager = new PinpointWebSocketHandlerManager(List.of(handler1, handler2));

        // not started by the constructor
        verify(handler1, never()).start();
        verify(handler2, never()).start();
        assertThat(manager.isRunning()).isFalse();
        assertThat(manager.isAutoStartup()).isTrue();

        manager.start();
        assertThat(manager.isRunning()).isTrue();
        verify(handler1).start();
        verify(handler2).start();

        manager.stop();
        assertThat(manager.isRunning()).isFalse();
        verify(handler1).stop();
        verify(handler2).stop();
    }

    @Test
    void onlyTheHandlerOfTheHighestPriorityIsStarted() {
        PinpointWebSocketHandler low = handler("/a", 0);
        PinpointWebSocketHandler high = handler("/a", 1);
        PinpointWebSocketHandlerManager manager = new PinpointWebSocketHandlerManager(List.of(low, high));

        manager.start();

        verify(high).start();
        verify(low, never()).start();
    }

    @Test
    void startsBeforeAndStopsAfterTheWebServer() {
        PinpointWebSocketHandlerManager manager = new PinpointWebSocketHandlerManager(List.of());

        // WebServerApplicationContext.START_STOP_LIFECYCLE_PHASE of Spring Boot
        int webServerPhase = SmartLifecycle.DEFAULT_PHASE - 2048;
        assertThat(manager.getPhase()).isLessThan(webServerPhase);
    }
}
