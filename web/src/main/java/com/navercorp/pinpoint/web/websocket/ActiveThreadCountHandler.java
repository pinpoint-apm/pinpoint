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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadCountList;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.apache.http.NameValuePair;
import org.apache.thrift.TException;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author Taejin Koo
 */
public class ActiveThreadCountHandler extends TextWebSocketHandler implements PinpointWebSocketHandler {

    private static final String APPLICATION_NAME_KEY = "applicationName";
    private static final String DEFAULT_REQUEST_MAPPING = "/agent/activeThread";

    private final String requestMapping;
    private final AgentService agentSerivce;

    private final Timer timer;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Object lock = new Object();

    // it will be changed.
    private final long time = 1000;
    private final AtomicBoolean onTimerTask = new AtomicBoolean(false);

    private final List<WebSocketSession> sessionRepository = new CopyOnWriteArrayList<WebSocketSession>();

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
            Timeout timeout = timer.newTimeout(new ActiveThreadTimerTask(), time, TimeUnit.MILLISECONDS);

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
            sessionRepository.remove(closeSession);
            if (sessionRepository.size() == 0) {
                boolean turnOff = onTimerTask.compareAndSet(true, false);
            }
        }

        super.afterConnectionClosed(closeSession, status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("handleTextMessage. session : {}, message : {}.", session, message.getPayload());

        String request = message.getPayload();
        if (request != null && request.startsWith(APPLICATION_NAME_KEY + "=")) {
            String applicationName = request.substring(APPLICATION_NAME_KEY.length() + 1);
            session.getAttributes().put(APPLICATION_NAME_KEY, applicationName);
        }

        // this method will be checked socket status.
        super.handleTextMessage(session, message);
    }

    private class ActiveThreadTimerTask implements TimerTask {

        @Override
        public void run(Timeout timeout) throws Exception {
            try {
                logger.info("ActiveThreadTimerTask started.");

                Map<String, List<WebSocketSession>> applicationGroup = createApplicationGroup(sessionRepository);

                for (Map.Entry<String, List<WebSocketSession>> applicationEntry : applicationGroup.entrySet()) {
                    String applicationName = applicationEntry.getKey();

                    List<AgentInfo> agentInfoList = getAgentInfoList(applicationName);
                    AgentActiveThreadCountList agentActiveThreadCountList = getAgentActiveThreadCount(agentInfoList);
                    doResponse(applicationEntry.getValue(), applicationName, agentActiveThreadCountList);
                }
            } finally {
                if (timer != null && onTimerTask.get()) {
                    timer.newTimeout(new ActiveThreadTimerTask(), time, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    private Map<String, List<WebSocketSession>> createApplicationGroup(List<WebSocketSession> sessionRepository) {
        Map<String, List<WebSocketSession>> applicationGroup = new HashMap<String, List<WebSocketSession>>();
        for (WebSocketSession session : sessionRepository) {
            String applicationName = (String) session.getAttributes().get(APPLICATION_NAME_KEY);

            if (applicationName == null || applicationName.length() == 0) {
                continue;
            }

            if (!applicationGroup.containsKey(applicationName)) {
                applicationGroup.put(applicationName, new ArrayList<WebSocketSession>());
            }

            applicationGroup.get(applicationName).add(session);
        }

        return applicationGroup;
    }

    private List<AgentInfo> getAgentInfoList(String applicationName) {
        try {
            List<AgentInfo> agentInfoList = agentSerivce.getAgentInfoList(applicationName);
            return agentInfoList;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private AgentActiveThreadCountList getAgentActiveThreadCount(List<AgentInfo> agentInfoList) {
        try {
            AgentActiveThreadCountList agentActiveThreadCountList = agentSerivce.getActiveThreadCount(agentInfoList);
            return agentActiveThreadCountList;
        } catch (TException e) {
            logger.warn(e.getMessage(), e);
        }

        return new AgentActiveThreadCountList(0);
    }

    private void doResponse(List<WebSocketSession> webSocketSessions, String applicationName, AgentActiveThreadCountList activeThreadCount) {
        if (webSocketSessions == null) {
            return;
        }

        String textMessage = makeResponseMessage(applicationName, activeThreadCount);

        for (WebSocketSession session : webSocketSessions) {
            try {
                session.sendMessage(new TextMessage(textMessage));
            } catch (IOException e) {
                logger.warn(e.getMessage(), e);
            }
        }
    }

    private String makeResponseMessage(String applicationName, AgentActiveThreadCountList activeThreadCount) {
        Map<String, AgentActiveThreadCountList> response = new HashMap<String, AgentActiveThreadCountList>();
        response.put(applicationName, activeThreadCount);

        try {
            return jsonConverter.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            logger.warn(e.getMessage(), e);
        }

        return createEmptyJsonMessage(applicationName);
    }

    private String createEmptyJsonMessage(String applicationName) {
        StringBuilder emptyJsonMessage = new StringBuilder();
        emptyJsonMessage.append("{");
        emptyJsonMessage.append("\"").append(applicationName).append("\"");
        emptyJsonMessage.append(":");
        emptyJsonMessage.append("{}");
        emptyJsonMessage.append("}");

        return emptyJsonMessage.toString();
    }

}
