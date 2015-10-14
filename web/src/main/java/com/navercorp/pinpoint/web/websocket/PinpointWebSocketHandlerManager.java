/*
 *
 *  * Copyright 2014 NAVER Corp.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.navercorp.pinpoint.web.websocket;

import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author Taejin Koo
 */
public class PinpointWebSocketHandlerManager implements WebSocketHandlerManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final List<PinpointWebSocketHandler> webSocketHandlerRepository = new ArrayList<PinpointWebSocketHandler>();

    private final PinpointThreadFactory threadFactory = new PinpointThreadFactory("WebSocket-Handler-Manager", true);

    private final Timer timer;

    public PinpointWebSocketHandlerManager() {
        timer = TimerFactory.createHashedWheelTimer(threadFactory, 100, TimeUnit.MILLISECONDS, 512);
    }

    @PreDestroy
    public void destory() {
        if (timer != null) {
            timer.stop();
        }
    }

    @Override
    public void register(PinpointWebSocketHandler handler) {
        webSocketHandlerRepository.add(handler);
    }

    public List<PinpointWebSocketHandler> getWebSocketHandlerRepository() {
        return new ArrayList<PinpointWebSocketHandler>(webSocketHandlerRepository);
    }

    @Override
    public Timer getTimer() {
        return timer;
    }

}
