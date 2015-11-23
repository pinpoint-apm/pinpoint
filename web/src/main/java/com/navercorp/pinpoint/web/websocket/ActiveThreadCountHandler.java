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
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.rpc.util.MapUtils;
import com.navercorp.pinpoint.rpc.util.StringUtils;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.websocket.message.PinpointWebSocketMessage;
import com.navercorp.pinpoint.web.websocket.message.PinpointWebSocketMessageConverter;
import com.navercorp.pinpoint.web.websocket.message.PongMessage;
import com.navercorp.pinpoint.web.websocket.message.RequestMessage;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author Taejin Koo
 */
public class ActiveThreadCountHandler extends TextWebSocketHandler implements PinpointWebSocketHandler {

    private static final String APPLICATION_NAME_KEY = "applicationName";
    private static final String HEALTH_CHECK_WAIT_KEY = "pinpoint.healthCheck.wait";

    static final String API_ACTIVE_THREAD_COUNT = "activeThreadCount";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Object lock = new Object();
    private final AgentService agentSerivce;
    private final List<WebSocketSession> sessionRepository = new CopyOnWriteArrayList<>();
    private final Map<String, PinpointWebSocketResponseAggregator> aggregatorRepository = new HashMap<>();
    private PinpointWebSocketMessageConverter messageConverter = new PinpointWebSocketMessageConverter();

    private static final String DEFAULT_REQUEST_MAPPING = "/agent/activeThread";
    private final String requestMapping;

    private final AtomicBoolean onTimerTask = new AtomicBoolean(false);

    private Timer flushTimer;
    private static final long DEFAULT_FLUSH_DELAY = 1000;
    private static final long DEFAULT_MIN_FLUSH_DELAY = 500;
    private final long flushDelay;

    private Timer  healthCheckTimer;
    private static final long DEFAULT_HEALTH_CHECk_DELAY = 60 * 1000;
    private final long healthCheckDelay;

    private Timer reactiveTimer;

    public ActiveThreadCountHandler(AgentService agentSerivce) {
        this(DEFAULT_REQUEST_MAPPING, agentSerivce);
    }

    public ActiveThreadCountHandler(String requestMapping, AgentService agentSerivce) {
        this(requestMapping, agentSerivce, DEFAULT_FLUSH_DELAY);
    }

    public ActiveThreadCountHandler(String requestMapping, AgentService agentSerivce, long flushDelay) {
        this(requestMapping, agentSerivce, flushDelay, DEFAULT_HEALTH_CHECk_DELAY);
    }

    public ActiveThreadCountHandler(String requestMapping, AgentService agentSerivce, long flushDelay, long healthCheckDelay) {
        this.requestMapping = requestMapping;
        this.agentSerivce = agentSerivce;
        this.flushDelay = flushDelay;
        this.healthCheckDelay = healthCheckDelay;
    }

    @Override
    public void start() {
        PinpointThreadFactory threadFactory = new PinpointThreadFactory(ClassUtils.simpleClassName(this) + "-Timer", true);
        this.flushTimer = TimerFactory.createHashedWheelTimer(threadFactory, 100, TimeUnit.MILLISECONDS, 512);
        this.healthCheckTimer = TimerFactory.createHashedWheelTimer(threadFactory, 100, TimeUnit.MILLISECONDS, 512);
        this.reactiveTimer = TimerFactory.createHashedWheelTimer(threadFactory, 100, TimeUnit.MILLISECONDS, 512);
    }

    @Override
    public void stop() {
        for (PinpointWebSocketResponseAggregator aggregator : aggregatorRepository.values()) {
            if (aggregator != null) {
                aggregator.stop();
            }
        }
        aggregatorRepository.clear();

        if (flushTimer != null) {
            flushTimer.stop();
        }

        if (healthCheckTimer != null) {
            healthCheckTimer.stop();
        }

        if (reactiveTimer != null) {
            reactiveTimer.stop();
        }
    }

    @Override
    public String getRequestMapping() {
        return requestMapping;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession newSession) throws Exception {
        logger.info("ConnectionEstablished. session:{}", newSession);

        synchronized (lock) {
            newSession.getAttributes().put(HEALTH_CHECK_WAIT_KEY, new AtomicBoolean(false));
            sessionRepository.add(newSession);
            boolean turnOn = onTimerTask.compareAndSet(false, true);
            if (turnOn) {
                flushTimer.newTimeout(new ActiveThreadTimerTask(), flushDelay, TimeUnit.MILLISECONDS);
                healthCheckTimer.newTimeout(new HealthCheckTimerTask(), DEFAULT_HEALTH_CHECk_DELAY, TimeUnit.MILLISECONDS);
            }
        }

        super.afterConnectionEstablished(newSession);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession closeSession, CloseStatus status) throws Exception {
        logger.info("ConnectionClose. session:{}, caused:{}", closeSession, status);

        synchronized (lock) {
            unbindingResponseAggregator(closeSession);

            sessionRepository.remove(closeSession);
            if (sessionRepository.size() == 0) {
                boolean turnOff = onTimerTask.compareAndSet(true, false);
            }
        }

        super.afterConnectionClosed(closeSession, status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession webSocketSession, TextMessage message) throws Exception {
        logger.info("handleTextMessage. session:{}, remote:{}, message:{}.", webSocketSession, webSocketSession.getRemoteAddress(), message.getPayload());

        PinpointWebSocketMessage webSocketMessage = messageConverter.getWebSocketMessage(message.getPayload());
        switch (webSocketMessage.getType()) {
            case REQUEST:
                handleRequestMessage0(webSocketSession, (RequestMessage) webSocketMessage);
                break;
            case PONG:
                handlePongMessage0(webSocketSession, (PongMessage) webSocketMessage);
                break;
        }

        // this method will be checked socket status.
        super.handleTextMessage(webSocketSession, message);
    }

    private void handleRequestMessage0(WebSocketSession webSocketSession, RequestMessage requestMessage) {
        String command = requestMessage.getCommand();

        if (API_ACTIVE_THREAD_COUNT.equals(command)) {
            String applicationName = MapUtils.getString(requestMessage.getParams(), APPLICATION_NAME_KEY);
            if (applicationName != null) {
                synchronized (lock) {
                    if (StringUtils.isEquals(applicationName, (String) webSocketSession.getAttributes().get(APPLICATION_NAME_KEY))) {
                        return;
                    }

                    unbindingResponseAggregator(webSocketSession);
                    bindingResponseAggregator(webSocketSession, applicationName);
                }
            }
        }
    }

    private void handlePongMessage0(WebSocketSession webSocketSession, PongMessage pongMessage) {
        Object healthCheckWait = webSocketSession.getAttributes().get(HEALTH_CHECK_WAIT_KEY);
        if (healthCheckWait != null && healthCheckWait instanceof AtomicBoolean) {
            ((AtomicBoolean) healthCheckWait).compareAndSet(true, false);
        }
    }

    @Override
    protected void handlePongMessage(WebSocketSession webSocketSession, org.springframework.web.socket.PongMessage message) throws Exception {
        logger.info("handlePongMessage. session:{}, remote:{}, message:{}.", webSocketSession, webSocketSession.getRemoteAddress(), message.getPayload());

        super.handlePongMessage(webSocketSession, message);
    }

    private void bindingResponseAggregator(WebSocketSession webSocketSession, String applicationName) {
        logger.info("bindingResponseAggregator. session:{}, applicationName:{}.", webSocketSession, applicationName);

        webSocketSession.getAttributes().put(APPLICATION_NAME_KEY, applicationName);
        if (StringUtils.isEmpty(applicationName)) {
            return;
        }

        PinpointWebSocketResponseAggregator responseAggregator = aggregatorRepository.get(applicationName);
        if (responseAggregator == null) {
            responseAggregator = new ActiveThreadCountResponseAggregator(applicationName, agentSerivce, reactiveTimer);
            responseAggregator.start();
            aggregatorRepository.put(applicationName, responseAggregator);
        }

        responseAggregator.addWebSocketSession(webSocketSession);
    }

    private void unbindingResponseAggregator(WebSocketSession webSocketSession) {
        String applicationName = (String) webSocketSession.getAttributes().get(APPLICATION_NAME_KEY);
        logger.info("unbindingResponseAggregator. session:{}, applicationName:{}.", webSocketSession, applicationName);
        if (StringUtils.isEmpty(applicationName)) {
            return;
        }

        PinpointWebSocketResponseAggregator responseAggregator = aggregatorRepository.get(applicationName);
        if (responseAggregator == null) {
            return;
        }

        boolean cleared = responseAggregator.removeWebSocketSessionAndGetIsCleared(webSocketSession);
        if (cleared) {
            aggregatorRepository.remove(applicationName);
            responseAggregator.stop();
        }
    }

    private class ActiveThreadTimerTask implements TimerTask {

        @Override
        public void run(Timeout timeout) throws Exception {
            long startTime = System.currentTimeMillis();
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
                if (flushTimer != null && onTimerTask.get()) {
                    long execTime = System.currentTimeMillis() - startTime;

                    long nextFlushDelay = flushDelay - execTime;
                    if (nextFlushDelay < DEFAULT_MIN_FLUSH_DELAY) {
                        flushTimer.newTimeout(this, DEFAULT_MIN_FLUSH_DELAY, TimeUnit.MILLISECONDS);
                    } else {
                        flushTimer.newTimeout(this, nextFlushDelay, TimeUnit.MILLISECONDS);
                    }
                }
            }
        }

    }

    private class HealthCheckTimerTask implements TimerTask {

        @Override
        public void run(Timeout timeout) throws Exception {
            try {
                logger.info("HealthCheckTimerTask started.");

                // check session state.
                List<WebSocketSession> webSocketSessionList = new ArrayList<>(sessionRepository);
                for (WebSocketSession session : webSocketSessionList) {
                    if (!session.isOpen()) {
                        continue;
                    }

                    Object untilWait = session.getAttributes().get(HEALTH_CHECK_WAIT_KEY);
                    if (untilWait != null && untilWait instanceof AtomicBoolean) {
                        if (((AtomicBoolean) untilWait).get()) {
                            session.close(CloseStatus.SESSION_NOT_RELIABLE);
                        }
                    } else {
                        session.getAttributes().put(HEALTH_CHECK_WAIT_KEY, new AtomicBoolean(false));
                    }
                }

                // send healthcheck packet
                String pingTextMessage = messageConverter.getPingTextMessage();
                TextMessage pingMessage = new TextMessage(pingTextMessage);

                webSocketSessionList = new ArrayList<>(sessionRepository);
                for (WebSocketSession session : webSocketSessionList) {
                    if (!session.isOpen()) {
                        continue;
                    }

                    Object untilWait = session.getAttributes().get(HEALTH_CHECK_WAIT_KEY);
                    if (untilWait != null && untilWait instanceof AtomicBoolean) {
                        ((AtomicBoolean) untilWait).compareAndSet(false, true);
                    } else {
                        session.getAttributes().put(HEALTH_CHECK_WAIT_KEY, new AtomicBoolean(true));
                    }

                    session.sendMessage(pingMessage);
                }
            } finally {
                if (healthCheckTimer != null && onTimerTask.get()) {
                    healthCheckTimer.newTimeout(this, healthCheckDelay, TimeUnit.MILLISECONDS);
                }
            }
        }

    }

}
