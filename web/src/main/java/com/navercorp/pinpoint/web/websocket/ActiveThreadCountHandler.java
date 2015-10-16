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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.rpc.util.StringUtils;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import com.navercorp.pinpoint.web.service.AgentService;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author Taejin Koo
 */
public class ActiveThreadCountHandler extends TextWebSocketHandler implements PinpointWebSocketHandler {

    private static final String DEFAULT_REQUEST_MAPPING = "/agent/activeThread";

    private static final String APPLICATION_NAME_KEY = "applicationName";

    private static final long DEFAULT_FLUSH_DELAY = 1000;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Object lock = new Object();

    private final String requestMapping;
    private final AgentService agentSerivce;

    private Timer timer;
    private final long flushDelay = DEFAULT_FLUSH_DELAY;

    private final AtomicBoolean onTimerTask = new AtomicBoolean(false);

    private final List<WebSocketSession> sessionRepository = new CopyOnWriteArrayList<WebSocketSession>();

    private final Map<String, PinpointWebSocketResponseAggregator> aggregatorRepository = new HashMap<String, PinpointWebSocketResponseAggregator>();

    private final ObjectMapper jsonConverter = new ObjectMapper();

    public ActiveThreadCountHandler(AgentService agentSerivce) {
        this(DEFAULT_REQUEST_MAPPING, agentSerivce);
    }

    public ActiveThreadCountHandler(String requestMapping, AgentService agentSerivce) {
        this.requestMapping = requestMapping;
        this.agentSerivce = agentSerivce;
    }

    @Override
    public void start() {
        this.timer = TimerFactory.createHashedWheelTimer(ClassUtils.simpleClassName(this) + "-Timer", 100, TimeUnit.MILLISECONDS, 512);
    }

    @Override
    public void stop() {
        for (PinpointWebSocketResponseAggregator aggregator : aggregatorRepository.values()) {
            if (aggregator != null) {
                aggregator.stop();
            }
        }
        aggregatorRepository.clear();

        if (timer != null) {
            timer.stop();
        }
    }

    @Override
    public String getRequestMapping() {
        return requestMapping;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession newSession) throws Exception {
        logger.info("ConnectionEstablished : {}", newSession);

        synchronized (lock) {
            sessionRepository.add(newSession);
            boolean turnOn = onTimerTask.compareAndSet(false, true);
            if (turnOn) {
                timer.newTimeout(new ActiveThreadTimerTask(), flushDelay, TimeUnit.MILLISECONDS);
            }
        }

        super.afterConnectionEstablished(newSession);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession closeSession, CloseStatus status) throws Exception {
        logger.info("ConnectionClosed : {}, caused : {}", closeSession, status);

        synchronized (lock) {
            closeAggregator(closeSession);

            sessionRepository.remove(closeSession);
            if (sessionRepository.size() == 0) {
                boolean turnOff = onTimerTask.compareAndSet(true, false);
            }
        }

        super.afterConnectionClosed(closeSession, status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession webSocketSession, TextMessage message) throws Exception {
        logger.info("handleTextMessage. session : {}, message : {}.", webSocketSession, message.getPayload());

        String request = message.getPayload();
        if (request != null && request.startsWith(APPLICATION_NAME_KEY + "=")) {
            String applicationName = request.substring(APPLICATION_NAME_KEY.length() + 1);
            synchronized (lock) {
                String oldApplicationName = (String) webSocketSession.getAttributes().get(APPLICATION_NAME_KEY);
                if (applicationName!= null && applicationName.equals(oldApplicationName)) {
                    return;
                }

                closeAggregator(webSocketSession);
                if (!StringUtils.isEmpty(applicationName)) {
                    webSocketSession.getAttributes().put(APPLICATION_NAME_KEY, applicationName);
                    openAggregator(webSocketSession);
                }
            }
        }

        // this method will be checked socket status.
        super.handleTextMessage(webSocketSession, message);
    }

    private void openAggregator(WebSocketSession webSocketSession) {
        String applicationName = (String) webSocketSession.getAttributes().get(APPLICATION_NAME_KEY);
        if (StringUtils.isEmpty(applicationName)) {
            return;
        }

        PinpointWebSocketResponseAggregator aggregator = aggregatorRepository.get(applicationName);
        if (aggregator == null) {
            aggregator = new ActiveThreadCountResponseAggregator(applicationName, agentSerivce, timer);
            aggregator.start();
            aggregatorRepository.put(applicationName, aggregator);
        }

        aggregator.addWebSocketSession(webSocketSession);
    }

    private void closeAggregator(WebSocketSession webSocketSession) {
        String applicationName = (String) webSocketSession.getAttributes().get(APPLICATION_NAME_KEY);
        if (StringUtils.isEmpty(applicationName)) {
            return;
        }

        PinpointWebSocketResponseAggregator aggregator = aggregatorRepository.get(applicationName);
        if (aggregator == null) {
            return;
        }

        boolean cleared = aggregator.removeWebSocketSessionAndGetIsCleared(webSocketSession);
        if (cleared) {
            aggregatorRepository.remove(applicationName);
            aggregator.stop();
        }
    }

    private class ActiveThreadTimerTask implements TimerTask {

        @Override
        public void run(Timeout timeout) throws Exception {
            try {
                logger.info("ActiveThreadTimerTask started.");

                Collection<PinpointWebSocketResponseAggregator> values = aggregatorRepository.values();
                for (PinpointWebSocketResponseAggregator aggregator : values) {
                    try {
                        aggregator.flush();
                    } catch (Exception e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            } finally {
                if (timer != null && onTimerTask.get()) {
                    timer.newTimeout(this, flushDelay, TimeUnit.MILLISECONDS);
                }
            }
        }

    }

}
