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
import com.navercorp.pinpoint.web.vo.AgentActiveThreadCount;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadCountList;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author Taejin Koo
 */
public class ActiveThreadCountResponseAggregator implements PinpointWebSocketResponseAggregator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Object aggregatorLock = new Object();
    private final Object workerManagingLock = new Object();
    private volatile boolean isStopped = false;

    private final ObjectMapper jsonConverter = new ObjectMapper();

    private final String applicationName;
    private final AgentService agentService;

    private final List<WebSocketSession> webSocketSessions = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<String, ActiveThreadCountWorker> activeThreadCountWorkerRepository = new ConcurrentHashMap<String, ActiveThreadCountWorker>();

    private final Timer timer;

    private StreamConnectionManager streamConnectionManager;

    private Map<String, AgentActiveThreadCount> activeThreadCountMap;

    public ActiveThreadCountResponseAggregator(String applicationName, AgentService agentService, Timer timer) {
        this.applicationName = applicationName;
        this.agentService = agentService;

        this.activeThreadCountMap = new HashMap<String, AgentActiveThreadCount>();
        this.timer = timer;
    }

    @Override
    public void start() {
        streamConnectionManager = new StreamConnectionManager(this, agentService, timer);
    }

    @Override
    public void stop() {
        synchronized (workerManagingLock) {
            isStopped = true;

            if (streamConnectionManager != null) {
                this.streamConnectionManager.close();
            }

            for (ActiveThreadCountWorker worker : activeThreadCountWorkerRepository.values()) {
                if (worker != null) {
                    worker.stop();
                }
            }

            activeThreadCountWorkerRepository.clear();
        }
    }

    @Override
    public void addWebSocketSession(WebSocketSession webSocketSession) {
        if (webSocketSession == null) {
            return;
        }

        logger.info("addWebSocketSession. applicationName:{}, webSocketSession:{}", applicationName, webSocketSession);

        List<AgentInfo> agentInfoList = agentService.getAgentInfoList(applicationName);
        synchronized (workerManagingLock) {
            if (isStopped) {
                return;
            }

            for (AgentInfo agentInfo : agentInfoList) {
                String agentId = agentInfo.getAgentId();

                if (!activeThreadCountWorkerRepository.contains(agentId)) {
                    ActiveThreadCountWorker activeThreadCountWorker = new ActiveThreadCountWorker(agentService, agentInfo, this, streamConnectionManager);
                    activeThreadCountWorker.start();

                    activeThreadCountWorkerRepository.put(agentId, activeThreadCountWorker);
                }
            }

            boolean added = webSocketSessions.add(webSocketSession);
            if (added && webSocketSessions.size() == 1) {
                streamConnectionManager.startAgentCheckJob();
            }
        }
    }

    // return when aggregator cleared.
    @Override
    public boolean removeWebSocketSessionAndGetIsCleared(WebSocketSession webSocketSession) {
        if (webSocketSession == null) {
            return false;
        }

        logger.info("removeWebSocketSessionAndGetIsCleared. applicationName{}, webSocketSession:{}", applicationName, webSocketSession);

        synchronized (workerManagingLock) {
            if (isStopped) {
                return true;
            }

            boolean removed = webSocketSessions.remove(webSocketSession);
            if (removed) {
                if (webSocketSessions.size() == 0) {
                    for (ActiveThreadCountWorker activeThreadCountWorker : activeThreadCountWorkerRepository.values()) {
                        activeThreadCountWorker.stop();
                    }
                    activeThreadCountWorkerRepository.clear();
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void addAgent(AgentInfo agentInfo) {

        String agentId = agentInfo.getAgentId();
        logger.info("addAgent agentId:{}", agentId);

        synchronized (workerManagingLock) {
            if (isStopped) {
                return;
            }

            if (!activeThreadCountWorkerRepository.containsKey(agentId)) {
                ActiveThreadCountWorker activeThreadCountWorker = new ActiveThreadCountWorker(agentService, agentInfo, this, streamConnectionManager);
                activeThreadCountWorker.start();

                activeThreadCountWorkerRepository.put(agentId, activeThreadCountWorker);
            }
        }
    }

    @Override
    public void response(AgentActiveThreadCount activeThreadCount) {
        if (activeThreadCount == null) {
            return;
        }

        synchronized (aggregatorLock) {
            this.activeThreadCountMap.put(activeThreadCount.getAgentId(), activeThreadCount);
        }
    }

    @Override
    public void flush() throws Exception {
        logger.info("flush");

        AgentActiveThreadCountList response = new AgentActiveThreadCountList();
        synchronized (aggregatorLock) {
            for (ActiveThreadCountWorker threadCountStreamListener : activeThreadCountWorkerRepository.values()) {
                String agentId = threadCountStreamListener.getAgentInfo().getAgentId();

                AgentActiveThreadCount agentActiveThreadCount = activeThreadCountMap.get(agentId);
                if (agentActiveThreadCount != null) {
                    response.add(agentActiveThreadCount);
                } else {
                    response.add(threadCountStreamListener.getDefaultFailedResponse());
                }
            }
            activeThreadCountMap = new HashMap<String, AgentActiveThreadCount>(activeThreadCountWorkerRepository.size());
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

    @Override
    public String getApplicationName() {
        return applicationName;
    }

}
