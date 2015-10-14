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
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelMessageListenerRepository;
import com.navercorp.pinpoint.rpc.util.StringUtils;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.vo.AgentInfo;
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

    static final String DEFAULT_REQUEST_MAPPING = "/agent/activeThread";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Object lock = new Object();

    private final String requestMapping;
    private final AgentService agentSerivce;
    private final Timer timer;

    // it will be changed.
    private final long time = 1000;
    private final AtomicBoolean onTimerTask = new AtomicBoolean(false);

    private final List<WebSocketSession> sessionRepository = new CopyOnWriteArrayList<WebSocketSession>();

    private final Map<String, WebSocketResponseAggregator> aggregatorRepository = new HashMap<String, WebSocketResponseAggregator>();

    private final ObjectMapper jsonConverter = new ObjectMapper();

    public ActiveThreadCountHandler(WebSocketHandlerRegister register, AgentService agentSerivce) {
        this(register, DEFAULT_REQUEST_MAPPING, agentSerivce);
    }

    public ActiveThreadCountHandler(WebSocketHandlerRegister register, String requestMapping, AgentService agentSerivce) {
        this.requestMapping = requestMapping;
        this.agentSerivce = agentSerivce;
        this.timer = register.getTimer();

        register.register(this);
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
                timer.newTimeout(new ActiveThreadTimerTask(), time, TimeUnit.MILLISECONDS);
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

        WebSocketResponseAggregator aggregator = aggregatorRepository.get(applicationName);
        if (aggregator == null) {
            aggregator = new WebSocketResponseAggregator(applicationName);
            aggregatorRepository.put(applicationName, aggregator);
        }

        ClientStreamChannelMessageListenerRepository<ActiveThreadCountStreamListener> streamMessageListenerRepository = aggregator.getStreamMessageListenerRepository();
        List<AgentInfo> agentInfoList = agentSerivce.getAgentInfoList(applicationName);

        for (AgentInfo agentInfo : agentInfoList) {
            String agentId = agentInfo.getAgentId();
            if (!streamMessageListenerRepository.contains(agentId)) {
                ActiveThreadCountStreamListener streamListener = new ActiveThreadCountStreamListener(agentSerivce, agentInfo, aggregator);
                streamListener.start();
            }
        }

        aggregator.registerWebSocketSession(webSocketSession);
    }

    private void closeAggregator(WebSocketSession webSocketSession) {
        String applicationName = (String) webSocketSession.getAttributes().get(APPLICATION_NAME_KEY);
        if (StringUtils.isEmpty(applicationName)) {
            return;
        }

        WebSocketResponseAggregator aggregator = aggregatorRepository.get(applicationName);
        if (aggregator == null) {
            return;
        }

        aggregator.unregisterWebSocketSession(webSocketSession);
        if (aggregator.registeredWebSocketSessionCount() == 0) {
            for (ActiveThreadCountStreamListener r : aggregator.getStreamMessageListenerRepository().values()) {
                r.stop();
            }

            aggregatorRepository.remove(applicationName);
        }
    }

    private class ActiveThreadTimerTask implements TimerTask {

        @Override
        public void run(Timeout timeout) throws Exception {
            try {
                logger.info("ActiveThreadTimerTask started.");

                Collection<WebSocketResponseAggregator> values = aggregatorRepository.values();
                for (WebSocketResponseAggregator aggregator : values) {
                    try {
                        aggregator.flush();
                    } catch (Exception e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            } finally {
                if (timer != null && onTimerTask.get()) {
                    timer.newTimeout(new ActiveThreadTimerTask(), time, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

}
