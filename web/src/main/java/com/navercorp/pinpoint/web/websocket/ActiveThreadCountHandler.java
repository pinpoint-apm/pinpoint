/*
 * Copyright 2015 NAVER Corp.
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

import com.navercorp.pinpoint.common.util.CpuUtils;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.rpc.util.MapUtils;
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.util.SimpleOrderedThreadPool;
import com.navercorp.pinpoint.web.websocket.message.PinpointWebSocketMessage;
import com.navercorp.pinpoint.web.websocket.message.PinpointWebSocketMessageConverter;
import com.navercorp.pinpoint.web.websocket.message.PinpointWebSocketMessageType;
import com.navercorp.pinpoint.web.websocket.message.PongMessage;
import com.navercorp.pinpoint.web.websocket.message.RequestMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Taejin Koo
 */
public class ActiveThreadCountHandler extends TextWebSocketHandler implements PinpointWebSocketHandler {

    public static final String APPLICATION_NAME_KEY = "applicationName";
    private static final String HEALTH_CHECK_WAIT_KEY = "pinpoint.healthCheck.wait";

    static final String API_ACTIVE_THREAD_COUNT = "activeThreadCount";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Object lock = new Object();
    private final AgentService agentService;
    private final List<WebSocketSession> sessionRepository = new CopyOnWriteArrayList<>();
    private final Map<String, PinpointWebSocketResponseAggregator> aggregatorRepository = new ConcurrentHashMap<>();
    private final PinpointWebSocketMessageConverter messageConverter = new PinpointWebSocketMessageConverter();

    private static final String DEFAULT_REQUEST_MAPPING = "/agent/activeThread";
    private final String requestMapping;

    private final AtomicBoolean onTimerTask = new AtomicBoolean(false);

    private SimpleOrderedThreadPool webSocketFlushExecutor;

    private java.util.Timer flushTimer;
    private static final long DEFAULT_FLUSH_DELAY = 1000;
    private final long flushDelay;

    private java.util.Timer healthCheckTimer;
    private static final long DEFAULT_HEALTH_CHECk_DELAY = 60 * 1000;
    private final long healthCheckDelay;

    private java.util.Timer reactiveTimer;
    
    @Autowired(required=false)
    ServerMapDataFilter serverMapDataFilter;

    public ActiveThreadCountHandler(AgentService agentService) {
        this(DEFAULT_REQUEST_MAPPING, agentService);
    }

    public ActiveThreadCountHandler(String requestMapping, AgentService agentService) {
        this(requestMapping, agentService, DEFAULT_FLUSH_DELAY);
    }

    public ActiveThreadCountHandler(String requestMapping, AgentService agentService, long flushDelay) {
        this(requestMapping, agentService, flushDelay, DEFAULT_HEALTH_CHECk_DELAY);
    }

    public ActiveThreadCountHandler(String requestMapping, AgentService agentService, long flushDelay, long healthCheckDelay) {
        this.requestMapping = requestMapping;
        this.agentService = agentService;
        this.flushDelay = flushDelay;
        this.healthCheckDelay = healthCheckDelay;
    }

    @Override
    public void start() {
        PinpointThreadFactory flushThreadFactory = new PinpointThreadFactory(ClassUtils.simpleClassName(this) + "-Flush-Thread", true);
        webSocketFlushExecutor = new SimpleOrderedThreadPool(CpuUtils.cpuCount(), 65535, flushThreadFactory);

        flushTimer = new java.util.Timer(ClassUtils.simpleClassName(this) + "-Flush-Timer", true);
        healthCheckTimer = new java.util.Timer(ClassUtils.simpleClassName(this) + "-HealthCheck-Timer", true);
        reactiveTimer = new java.util.Timer(ClassUtils.simpleClassName(this) + "-Reactive-Timer", true);
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
            flushTimer.cancel();
        }

        if (healthCheckTimer != null) {
            healthCheckTimer.cancel();
        }

        if (reactiveTimer != null) {
            reactiveTimer.cancel();
        }

        if (webSocketFlushExecutor != null) {
            webSocketFlushExecutor.shutdown();
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
                flushTimer.schedule(new ActiveThreadTimerTask(flushDelay), flushDelay);
                healthCheckTimer.schedule(new HealthCheckTimerTask(), DEFAULT_HEALTH_CHECk_DELAY);
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
            if (sessionRepository.isEmpty()) {
                boolean turnOff = onTimerTask.compareAndSet(true, false);
            }
        }

        super.afterConnectionClosed(closeSession, status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession webSocketSession, TextMessage message) throws Exception {
        logger.info("handleTextMessage. session:{}, remote:{}, message:{}.", webSocketSession, webSocketSession.getRemoteAddress(), message.getPayload());

        PinpointWebSocketMessage webSocketMessage = messageConverter.getWebSocketMessage(message.getPayload());
        PinpointWebSocketMessageType webSocketMessageType = webSocketMessage.getType();
        switch (webSocketMessageType) {
            case REQUEST:
                handleRequestMessage0(webSocketSession, (RequestMessage) webSocketMessage);
                break;
            case PONG:
                handlePongMessage0(webSocketSession, (PongMessage) webSocketMessage);
                break;
            default:
                logger.warn("Unexpected WebSocketMessageType received. messageType:{}.", webSocketMessageType);
        }

        // this method will be checked socket status.
        super.handleTextMessage(webSocketSession, message);
    }

    private void handleRequestMessage0(WebSocketSession webSocketSession, RequestMessage requestMessage) {
        if (serverMapDataFilter != null && serverMapDataFilter.filter(webSocketSession, requestMessage)) {
            closeSession(webSocketSession, serverMapDataFilter.getCloseStatus(requestMessage));
            return;
        }
        
        String command = requestMessage.getCommand();

        if (API_ACTIVE_THREAD_COUNT.equals(command)) {
            String applicationName = MapUtils.getString(requestMessage.getParams(), APPLICATION_NAME_KEY);
            if (applicationName != null) {
                synchronized (lock) {
                    if (StringUtils.equals(applicationName, (String) webSocketSession.getAttributes().get(APPLICATION_NAME_KEY))) {
                        return;
                    }

                    unbindingResponseAggregator(webSocketSession);
                    if (webSocketSession.isOpen()) {
                        bindingResponseAggregator(webSocketSession, applicationName);
                    } else {
                        logger.warn("WebSocketSession is not opened. skip binding.");
                    }
                }
            }
        }
    }
    
    private void closeSession(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    private void handlePongMessage0(WebSocketSession webSocketSession, PongMessage pongMessage) {
        Object healthCheckWait = webSocketSession.getAttributes().get(HEALTH_CHECK_WAIT_KEY);
        if (healthCheckWait instanceof AtomicBoolean) {
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
            responseAggregator = new ActiveThreadCountResponseAggregator(applicationName, agentService, reactiveTimer);
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

    private class ActiveThreadTimerTask extends java.util.TimerTask {

        private final long startTimeMillis;
        private final long delay;

        private int times = 0;

        public ActiveThreadTimerTask(long delay) {
            this(System.currentTimeMillis(), delay, 0);
        }

        public ActiveThreadTimerTask(long startTimeMillis, long delay, int times) {
            this.startTimeMillis = startTimeMillis;
            this.delay = delay;
            this.times = times;
        }

        @Override
        public void run() {
            try {
                logger.info("ActiveThreadTimerTask started.");

                Collection<PinpointWebSocketResponseAggregator> values = aggregatorRepository.values();
                for (final PinpointWebSocketResponseAggregator aggregator : values) {
                    try {
                        aggregator.flush(webSocketFlushExecutor);
                    } catch (Exception e) {
                        logger.warn("failed while flushing ActiveThreadCount to aggregator. applicationName:{}, error:{}", aggregator.getApplicationName(), e.getMessage(), e);
                    }
                }
            } finally {
                long waitTimeMillis = getWaitTimeMillis();

                if (flushTimer != null && onTimerTask.get()) {
                    flushTimer.schedule(new ActiveThreadTimerTask(startTimeMillis, delay, times), waitTimeMillis);
                }
            }
        }

        private long getWaitTimeMillis() {
            long waitTime = -1L;

            long currentTime = System.currentTimeMillis();
            while (waitTime <= 0) {
                waitTime = startTimeMillis + (delay * times) - currentTime;
                times++;
            }

            return waitTime;
        }
    }

    private class HealthCheckTimerTask extends java.util.TimerTask {

        @Override
        public void run() {
            try {
                logger.info("HealthCheckTimerTask started.");

                // check session state.
                List<WebSocketSession> webSocketSessionList = new ArrayList<>(sessionRepository);
                for (WebSocketSession session : webSocketSessionList) {
                    if (!session.isOpen()) {
                        continue;
                    }

                    Object untilWait = session.getAttributes().get(HEALTH_CHECK_WAIT_KEY);
                    if (untilWait instanceof AtomicBoolean) {
                        if (((AtomicBoolean) untilWait).get()) {
                            closeSession(session, CloseStatus.SESSION_NOT_RELIABLE);
                        }
                    } else {
                        session.getAttributes().put(HEALTH_CHECK_WAIT_KEY, new AtomicBoolean(false));
                    }
                }

                // send healthCheck packet
                String pingTextMessage = messageConverter.getPingTextMessage();
                TextMessage pingMessage = new TextMessage(pingTextMessage);

                webSocketSessionList = new ArrayList<>(sessionRepository);
                for (WebSocketSession session : webSocketSessionList) {
                    if (!session.isOpen()) {
                        continue;
                    }

                    Object untilWait = session.getAttributes().get(HEALTH_CHECK_WAIT_KEY);
                    if (untilWait instanceof AtomicBoolean) {
                        ((AtomicBoolean) untilWait).compareAndSet(false, true);
                    } else {
                        session.getAttributes().put(HEALTH_CHECK_WAIT_KEY, new AtomicBoolean(true));
                    }

                    sendPingMessage(session, pingMessage);
                }
            } finally {
                if (healthCheckTimer != null && onTimerTask.get()) {
                    healthCheckTimer.schedule(new HealthCheckTimerTask(), healthCheckDelay);
                }
            }
        }


        private void sendPingMessage(WebSocketSession session, TextMessage pingMessage) {
            try {
                webSocketFlushExecutor.execute(new OrderedWebSocketFlushRunnable(session, pingMessage, true));
            } catch (RuntimeException e) {
                logger.warn("failed while to execute. error:{}.", e.getMessage(), e);
            }
        }
    }

}
