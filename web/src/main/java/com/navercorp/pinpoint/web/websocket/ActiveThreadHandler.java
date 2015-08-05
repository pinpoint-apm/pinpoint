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
import com.navercorp.pinpoint.common.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadResponse;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadStatusList;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Author Taejin Koo
 */
public class ActiveThreadHandler extends TextWebSocketHandler implements  PinpointWebSocketHandler {

    private static final String APPLICATION_NAME_KEY = "applicationName";
    private static final String DEFAULT_REQUEST_MAPPING = "/agent/activeThread";

    private final String requestMapping;
    private final AgentService agentSerivce;

    // it will be changed.
    private final long time = 1000;

    private final PinpointThreadFactory threadFactory = new PinpointThreadFactory("ActiveThread Handler", true);
    private final TimerFactory timerFactory = new TimerFactory();

    private final Map<String, List<WebSocketSession>> applicationGroup = new HashMap<String, List<WebSocketSession>>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    private final ObjectMapper jsonConverter = new ObjectMapper();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Timer timer;

    public ActiveThreadHandler(AgentService agentSerivce) {
        this(DEFAULT_REQUEST_MAPPING, agentSerivce);
    }

    public ActiveThreadHandler(String requestMapping, AgentService agentSerivce) {
        this.requestMapping = requestMapping;
        this.agentSerivce = agentSerivce;
    }

    @Override
    public String getRequestMapping() {
        return requestMapping;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession newSession) throws Exception {
        logger.info("ConnectionEstablished : {}", newSession);

        List<NameValuePair> params = URLEncodedUtils.parse(newSession.getUri(), "UTF-8");
        String applicationName = getValue(params, APPLICATION_NAME_KEY);
        if (applicationName == null) {
            logger.warn("Connection established refused. required parameter is missiong({}).", APPLICATION_NAME_KEY);
            newSession.close(CloseStatus.POLICY_VIOLATION);
        }
        newSession.getAttributes().put(APPLICATION_NAME_KEY, applicationName);

        writeLock.lock();
        try {
            List<WebSocketSession> webSocketSessions = applicationGroup.get(applicationName);
            if (webSocketSessions == null) {
                webSocketSessions = new ArrayList<WebSocketSession>();
                applicationGroup.put(applicationName, webSocketSessions);
            }
            webSocketSessions.add(newSession);

            if (timer == null) {
                timer = timerFactory.createHashedWheelTimer(threadFactory, 100, TimeUnit.MILLISECONDS, 512);
                Timeout timeout = timer.newTimeout(new ActiveThreadTimerTask(), time, TimeUnit.MILLISECONDS);
            }
        } finally {
            writeLock.unlock();
        }

        super.afterConnectionEstablished(newSession);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession closeSession, CloseStatus status) throws Exception {
        logger.info("ConnectionClosed : {}, caused : {}", closeSession, status);

        String applicationName = (String) closeSession.getAttributes().get(APPLICATION_NAME_KEY);

        writeLock.lock();
        try {
            if (applicationName != null) {
                List<WebSocketSession> webSocketSessions = applicationGroup.get(applicationName);
                if (webSocketSessions == null) {
                    webSocketSessions = new ArrayList<WebSocketSession>();
                }
                webSocketSessions.remove(closeSession);

                if (webSocketSessions.size() == 0) {
                    applicationGroup.remove(applicationName);
                }

                if (applicationGroup.size() == 0) {
                    if (timer != null) {
                        timer.stop();
                        timer = null;
                    }
                }
            }
        } finally {
            writeLock.unlock();
        }

        super.afterConnectionClosed(closeSession, status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("handleTextMessage. session : {}, message : {}.", session, message);

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
            logger.info("ActiveThreadTimerTask started.");

            readLock.lock();
            try {
                for (Map.Entry<String, List<WebSocketSession>> applicationEntry : applicationGroup.entrySet()) {
                    List<AgentInfoBo> agentInfoList = agentSerivce.get(applicationEntry.getKey());
                    Map<String, TActiveThreadResponse> activeThreadStatuses = agentSerivce.getActiveThreadStatus(agentInfoList);

                    AgentActiveThreadStatusList agentActiveThreadStatusList = new AgentActiveThreadStatusList(activeThreadStatuses.size());
                    agentActiveThreadStatusList.addAll(activeThreadStatuses);
                    String textMessage = jsonConverter.writeValueAsString(agentActiveThreadStatusList);

                    for (WebSocketSession session : applicationEntry.getValue()) {
                        session.sendMessage(new TextMessage(textMessage));
                    }
                }

                if (timer != null) {
                    timer.newTimeout(new ActiveThreadTimerTask(), time, TimeUnit.MILLISECONDS);
                }

            } finally {
                readLock.unlock();
            }
        }

    }

}
