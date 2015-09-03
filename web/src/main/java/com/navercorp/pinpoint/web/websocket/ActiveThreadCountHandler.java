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
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadCountList;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.apache.http.NameValuePair;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Author Taejin Koo
 */
public class ActiveThreadCountHandler extends TextWebSocketHandler implements PinpointWebSocketHandler {

    private static final String APPLICATION_NAME_KEY = "applicationName";
    private static final String DEFAULT_REQUEST_MAPPING = "/agent/activeThread";

    private final String requestMapping;
    private final AgentService agentSerivce;

    // it will be changed.
    private final long time = 1000;

    private final PinpointThreadFactory threadFactory = new PinpointThreadFactory("ActiveThread Handler", true);
    private final TimerFactory timerFactory = new TimerFactory();

    private final List<WebSocketSession> sessionRepository = new CopyOnWriteArrayList<WebSocketSession>();

    private final ObjectMapper jsonConverter = new ObjectMapper();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Timer timer;

    public ActiveThreadCountHandler(WebSocketHandlerRegister register, AgentService agentSerivce) {
        this(register, DEFAULT_REQUEST_MAPPING, agentSerivce);
    }

    public ActiveThreadCountHandler(WebSocketHandlerRegister register, String requestMapping, AgentService agentSerivce) {
        this.requestMapping = requestMapping;
        this.agentSerivce = agentSerivce;
        this.timer = register.getTimer();

        register.register(this);
        Timeout timeout = timer.newTimeout(new ActiveThreadTimerTask(), time, TimeUnit.MILLISECONDS);
    }

    @Override
    public String getRequestMapping() {
        return requestMapping;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession newSession) throws Exception {
        logger.info("ConnectionEstablished : {}", newSession);

        sessionRepository.add(newSession);

        super.afterConnectionEstablished(newSession);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession closeSession, CloseStatus status) throws Exception {
        logger.info("ConnectionClosed : {}, caused : {}", closeSession, status);

        sessionRepository.remove(closeSession);

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

    private String getValue(List<NameValuePair> params, String key) {
        for (NameValuePair nv : params) {
            if (key.equals(nv.getName())) {
                return nv.getValue();
            }
        }

        return null;
    }

    private class ActiveThreadTimerTask implements TimerTask {

        @Override
        public void run(Timeout timeout) throws Exception {
            if (sessionRepository.size() != 0) {
                logger.info("ActiveThreadTimerTask started.");

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

                for (Map.Entry<String, List<WebSocketSession>> applicationEntry : applicationGroup.entrySet()) {
                    String applicationName = applicationEntry.getKey();

                    List<AgentInfo> agentInfoList = agentSerivce.getAgentInfoList(applicationName);
                    AgentActiveThreadCountList agentActiveThreadCountList = agentSerivce.getActiveThreadCount(agentInfoList);

                    Map<String, AgentActiveThreadCountList> response = new HashMap<String, AgentActiveThreadCountList>();
                    response.put(applicationName, agentActiveThreadCountList);

                    String textMessage = jsonConverter.writeValueAsString(response);
                    for (WebSocketSession session : applicationEntry.getValue()) {
                        session.sendMessage(new TextMessage(textMessage));
                    }
                }
            }

            if (timer != null) {
                timer.newTimeout(new ActiveThreadTimerTask(), time, TimeUnit.MILLISECONDS);
            }
        }
    }

}
