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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.task.TimerTaskDecorator;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadCount;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadCountList;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.AgentStatus;
import com.navercorp.pinpoint.web.websocket.message.PinpointWebSocketMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Taejin Koo
 */
public class ActiveThreadCountResponseAggregator implements PinpointWebSocketResponseAggregator {

    private static final String APPLICATION_NAME = "applicationName";
    private static final String ACTIVE_THREAD_COUNTS = "activeThreadCounts";
    private static final String TIME_STAMP = "timeStamp";

    private final static int LOG_RECORD_RATE = 60;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String applicationName;
    private final AgentService agentService;
    private final Timer timer;
    private final TimerTaskDecorator timerTaskDecorator;

    private final Object workerManagingLock = new Object();
    private final List<WebSocketSession> webSocketSessions = new CopyOnWriteArrayList<>();
    private final ConcurrentMap<String, ActiveThreadCountWorker> activeThreadCountWorkerRepository = new ConcurrentHashMap<>();

    private final Object aggregatorLock = new Object();
    private final PinpointWebSocketMessageConverter messageConverter;

    private final AtomicInteger flushCount = new AtomicInteger(0);

    private volatile boolean isStopped = false;
    private WorkerActiveManager workerActiveManager;

    private Map<String, AgentActiveThreadCount> activeThreadCountMap = new HashMap<>();

    public ActiveThreadCountResponseAggregator(String applicationName, AgentService agentService, Timer timer, TimerTaskDecorator timerTaskDecorator) {
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.agentService = Objects.requireNonNull(agentService, "agentService");

        this.timer = Objects.requireNonNull(timer, "timer");
        this.timerTaskDecorator = Objects.requireNonNull(timerTaskDecorator, "timerTaskDecorator");

        this.messageConverter = new PinpointWebSocketMessageConverter();
    }

    @Override
    public void start() {
        synchronized (workerManagingLock) {
            workerActiveManager = new WorkerActiveManager(this, agentService, timer, timerTaskDecorator);
        }
    }

    @Override
    public void stop() {
        synchronized (workerManagingLock) {
            isStopped = true;

            if (workerActiveManager != null) {
                this.workerActiveManager.close();
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

        List<AgentInfo> agentInfoList = agentService.getRecentAgentInfoList(applicationName);
        synchronized (workerManagingLock) {
            if (isStopped) {
                return;
            }

            for (AgentInfo agentInfo : agentInfoList) {
                AgentStatus agentStatus = agentInfo.getStatus();
                if (agentStatus != null && agentStatus.getState() != AgentLifeCycleState.UNKNOWN) {
                    activeWorker(agentInfo);
                } else if (agentService.isConnected(agentInfo)) {
                    activeWorker(agentInfo);
                }
            }

            final boolean added = webSocketSessions.add(webSocketSession);
            if (added && webSocketSessions.size() == 1) {
                workerActiveManager.startAgentCheckJob();
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
            if (removed && webSocketSessions.isEmpty()) {
                for (ActiveThreadCountWorker activeThreadCountWorker : activeThreadCountWorkerRepository.values()) {
                    activeThreadCountWorker.stop();
                }
                activeThreadCountWorkerRepository.clear();
                return true;
            }
        }

        return false;
    }

    @Override
    public void addActiveWorker(AgentInfo agentInfo) {
        logger.info("activeWorker applicationName:{}, agentId:{}", applicationName, agentInfo.getAgentId());

        if (!applicationName.equals(agentInfo.getApplicationName())) {
            return;
        }

        synchronized (workerManagingLock) {
            if (isStopped) {
                return;
            }
            activeWorker(agentInfo);
        }
    }

    private void activeWorker(AgentInfo agentInfo) {
        String agentId = agentInfo.getAgentId();

        synchronized (workerManagingLock) {
            ActiveThreadCountWorker worker = activeThreadCountWorkerRepository.get(agentId);
            if (worker == null) {
                worker = new ActiveThreadCountWorker(agentService, agentInfo, this, workerActiveManager);
                worker.start(agentInfo);

                activeThreadCountWorkerRepository.put(agentId, worker);
            } else {
                worker.reactive(agentInfo);
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
        flush(null);
    }

    @Override
    public void flush(Executor executor) throws Exception {
        if ((flushCount.getAndIncrement() % LOG_RECORD_RATE) == 0) {
            logger.info("flush started. applicationName:{}", applicationName);
        }

        if (isStopped) {
            return;
        }

        AgentActiveThreadCountList response = new AgentActiveThreadCountList();
        synchronized (aggregatorLock) {
            for (ActiveThreadCountWorker activeThreadCountWorker : activeThreadCountWorkerRepository.values()) {
                String agentId = activeThreadCountWorker.getAgentId();

                AgentActiveThreadCount agentActiveThreadCount = activeThreadCountMap.get(agentId);
                if (agentActiveThreadCount != null) {
                    response.add(agentActiveThreadCount);
                } else {
                    response.add(activeThreadCountWorker.getDefaultFailResponse());
                }
            }
            activeThreadCountMap = new HashMap<>(activeThreadCountWorkerRepository.size());
        }

        TextMessage webSocketTextMessage = createWebSocketTextMessage(response);
        if (webSocketTextMessage != null) {
            if (executor == null) {
                flush0(webSocketTextMessage);
            } else {
                flushAsync0(webSocketTextMessage, executor);
            }
        }
    }

    private TextMessage createWebSocketTextMessage(AgentActiveThreadCountList activeThreadCountList) {
        Map<String, Object> resultMap = createResultMap(activeThreadCountList, System.currentTimeMillis());
        try {
            String response = messageConverter.getResponseTextMessage(ActiveThreadCountHandler.API_ACTIVE_THREAD_COUNT, resultMap);
            TextMessage responseTextMessage = new TextMessage(response);
            return responseTextMessage;
        } catch (JsonProcessingException e) {
            logger.warn("failed while to convert message. applicationName:{}, original:{}, message:{}.", applicationName, resultMap, e.getMessage(), e);
        }
        return null;
    }

    private void flush0(TextMessage webSocketMessage) {
        for (WebSocketSession webSocketSession : webSocketSessions) {
            try {
                logger.debug("flush webSocketSession:{}, response:{}", webSocketSession, webSocketMessage);
                webSocketSession.sendMessage(webSocketMessage);
            } catch (Exception e) {
                logger.warn("failed while flushing message to webSocket. session:{}, message:{}, error:{}", webSocketSession, webSocketMessage, e.getMessage(), e);
            }
        }
    }

    private void flushAsync0(TextMessage webSocketMessage, Executor executor) {
        for (WebSocketSession webSocketSession : webSocketSessions) {
            if (webSocketSession == null) {
                logger.warn("failed caused webSocketSession is null. applicationName:{}", applicationName);
                continue;
            }
            executor.execute(new OrderedWebSocketFlushRunnable(webSocketSession, webSocketMessage));
        }
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    private Map createResultMap(AgentActiveThreadCountList activeThreadCount, long timeStamp) {
        Map<String, Object> response = new HashMap<>();

        response.put(APPLICATION_NAME, applicationName);
        response.put(ACTIVE_THREAD_COUNTS, activeThreadCount);
        response.put(TIME_STAMP, timeStamp);

        return response;
    }

}
