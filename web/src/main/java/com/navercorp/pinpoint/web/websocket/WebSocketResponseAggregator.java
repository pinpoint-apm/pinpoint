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
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelMessageListenerRepository;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadCount;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadCountList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author Taejin Koo
 */
public class WebSocketResponseAggregator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Object lock = new Object();
    private final ObjectMapper jsonConverter = new ObjectMapper();

    private final String applicationName;
    private final List<WebSocketSession> webSocketSessions = new CopyOnWriteArrayList<>();
    private final ClientStreamChannelMessageListenerRepository<ActiveThreadCountStreamListener> streamMessageListenerRepository;

    private Map<String, AgentActiveThreadCount> activeThreadCountMap;

    public WebSocketResponseAggregator(String applicationName) {
        this.applicationName = applicationName;
        this.streamMessageListenerRepository = new ClientStreamChannelMessageListenerRepository<ActiveThreadCountStreamListener>();

        this.activeThreadCountMap = new HashMap<String, AgentActiveThreadCount>(streamMessageListenerRepository.size());
    }

    public void registerWebSocketSession(WebSocketSession webSocketSession) {
        if (webSocketSession == null) {
            return;
        }

        logger.info("registerWebSocketSession webSocketSession:{}");
        synchronized (lock) {
            this.webSocketSessions.add(webSocketSession);
        }
    }

    public void unregisterWebSocketSession(WebSocketSession webSocketSession) {
        if (webSocketSession == null) {
            return;
        }

        logger.info("unregisterWebSocketSession webSocketSession:{}");
        synchronized (lock) {
            this.webSocketSessions.remove(webSocketSession);
        }
    }

    public int registeredWebSocketSessionCount() {
        synchronized (lock) {
            return this.webSocketSessions.size();
        }
    }

    public void registerStreamMessageListener(String agentId, ActiveThreadCountStreamListener streamMessageListener) {
        streamMessageListenerRepository.put(agentId, streamMessageListener);
    }

    public void unregisterStreamMessageListener(String agentId) {
        streamMessageListenerRepository.remove(agentId);
    }

    public ClientStreamChannelMessageListenerRepository<ActiveThreadCountStreamListener> getStreamMessageListenerRepository() {
        return streamMessageListenerRepository;
    }

    public void response(AgentActiveThreadCount activeThreadCount) {
        if (activeThreadCount == null) {
            return;
        }

        synchronized (lock) {
            this.activeThreadCountMap.put(activeThreadCount.getAgentId(), activeThreadCount);
        }
    }

    public void flush() throws Exception {
        logger.info("flush");

        AgentActiveThreadCountList response = new AgentActiveThreadCountList();
        synchronized (lock) {
            for (ActiveThreadCountStreamListener threadCountStreamListener : streamMessageListenerRepository.values()) {
                String agentId = threadCountStreamListener.getAgentInfo().getAgentId();

                AgentActiveThreadCount agentActiveThreadCount = activeThreadCountMap.get(agentId);
                if (agentActiveThreadCount != null) {
                    response.add(agentActiveThreadCount);
                } else {
                    response.add(threadCountStreamListener.getDefaultFailedResponse());
                }
            }
            activeThreadCountMap = new HashMap<String, AgentActiveThreadCount>(streamMessageListenerRepository.size());
        }

        flush0(response);
    }

    private void flush0(AgentActiveThreadCountList activeThreadCountList) {
        String response = makeResponseMessage(applicationName, activeThreadCountList);

        for (WebSocketSession webSocketSession : webSocketSessions) {
            try {
                logger.debug("flush webSocket:{}, response:{}", webSocketSession, response);
                webSocketSession.sendMessage(new TextMessage(response));
            } catch (IOException e) {
                logger.warn(e.getMessage(), e);
            }
        }
    }

    private String makeResponseMessage(String applicationName, AgentActiveThreadCountList activeThreadCount) {
        Map<String, Object> response = new HashMap<String, Object>();

        response.put("applicationName", applicationName);
        response.put("activeThreadCounts", activeThreadCount);
        response.put("timeStamp", System.currentTimeMillis());

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
