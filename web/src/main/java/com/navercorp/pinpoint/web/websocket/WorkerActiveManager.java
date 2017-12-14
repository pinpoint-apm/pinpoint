/*
 * Copyright 2014 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.AgentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Taejin Koo
 */
public class WorkerActiveManager {

    private static final long DEFAULT_RECONNECT_DELAY = 5000;
    private static final long DEFAULT_AGENT_CHECk_DELAY = 10000;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PinpointWebSocketResponseAggregator responseAggregator;

    private final String applicationName;
    private final AgentService agentService;

    private final Timer timer;

    private final AtomicBoolean isStopped = new AtomicBoolean();

    private final Object lock = new Object();

    private final AtomicBoolean onReconnectTimerTask = new AtomicBoolean(false);
    private final Set<String> reactiveWorkerRepository = new CopyOnWriteArraySet<>();

    private final AtomicBoolean onAgentCheckTimerTask = new AtomicBoolean(false);
    private final List<String> defaultAgentIdList = new CopyOnWriteArrayList<>();

    public WorkerActiveManager(PinpointWebSocketResponseAggregator responseAggregator, AgentService agentService, Timer timer) {
        this.responseAggregator = responseAggregator;

        this.applicationName = responseAggregator.getApplicationName();
        this.agentService = agentService;

        this.timer = timer;
    }

    public void close() {
        synchronized (lock) {
            isStopped.compareAndSet(false, true);

            onReconnectTimerTask.set(false);
            reactiveWorkerRepository.clear();

            onAgentCheckTimerTask.set(false);
            defaultAgentIdList.clear();
        }
    }

    public void addReactiveWorker(AgentInfo agentInfo) {
        if (applicationName.equals(agentInfo.getApplicationName())) {
            addReactiveWorker(agentInfo.getAgentId());
        }
    }

    public void addReactiveWorker(String agentId) {
        logger.info("addReactiveWorker. applicationName:{}, agent:{}", applicationName, agentId);

        synchronized (lock) {
            if (isStopped.get()) {
                return;
            }

            reactiveWorkerRepository.add(agentId);

            boolean turnOn = onReconnectTimerTask.compareAndSet(false, true);
            logger.info("addReactiveWorker turnOn:{}", turnOn);
            if (turnOn) {
                timer.schedule(new ReactiveTimerTask(), DEFAULT_RECONNECT_DELAY);
            }
        }
    }

    public void startAgentCheckJob() {
        logger.info("startAgentCheckJob. applicationName:{}", applicationName);

        boolean turnOn = onAgentCheckTimerTask.compareAndSet(false, true);
        if (turnOn) {
            timer.schedule(new AgentCheckTimerTask(), DEFAULT_AGENT_CHECk_DELAY);
        }
    }

    private class ReactiveTimerTask extends TimerTask {

        @Override
        public void run() {
            logger.info("ReactiveTimerTask started.");

            Set<String> reactiveWorkerCandidates = new HashSet<>(reactiveWorkerRepository.size());
            synchronized (lock) {
                reactiveWorkerCandidates.addAll(reactiveWorkerRepository);
                reactiveWorkerRepository.clear();
                boolean turnOff = onReconnectTimerTask.compareAndSet(true, false);
            }

            for (String agentId : reactiveWorkerCandidates) {
                try {
                    AgentInfo newAgentInfo = agentService.getAgentInfo(applicationName, agentId);
                    if (newAgentInfo != null) {
                        responseAggregator.addActiveWorker(newAgentInfo);
                    }
                } catch (Exception e) {
                    logger.warn("failed while to get AgentInfo(applicationName:{}, agentId:{}). error:{}.", applicationName, agentId, e.getMessage(), e);
                }
            }
        }

    }

    private class AgentCheckTimerTask extends TimerTask {

        @Override
        public void run() {
            logger.info("AgentCheckTimerTask started.");

            List<AgentInfo> agentInfoList = Collections.emptyList();
            try {
                agentInfoList = agentService.getRecentAgentInfoList(applicationName);
            } catch (Exception e) {
                logger.warn("failed while to get RecentAgentInfoList(applicationName:{}). error:{}.", applicationName, e.getMessage(), e);
            }

            try {
                for (AgentInfo agentInfo : agentInfoList) {
                    String agentId = agentInfo.getAgentId();
                    if (defaultAgentIdList.contains(agentId)) {
                        continue;
                    }

                    AgentStatus agentStatus = agentInfo.getStatus();
                    if (agentStatus != null && agentStatus.getState() != AgentLifeCycleState.UNKNOWN) {
                        addActiveWorker(agentInfo);
                    } else if (agentService.isConnected(agentInfo)) {
                        addActiveWorker(agentInfo);
                    }
                }
            } finally {
                if (timer != null && onAgentCheckTimerTask.get() && !isStopped.get()) {
                    timer.schedule(new AgentCheckTimerTask(), DEFAULT_AGENT_CHECk_DELAY);
                }
            }
        }

        private void addActiveWorker(AgentInfo agentInfo) {
            try {
                responseAggregator.addActiveWorker(agentInfo);
                defaultAgentIdList.add(agentInfo.getAgentId());
            } catch (Exception e) {
                logger.warn("failed while adding active worker. error:{}", e.getMessage(), e);
            }
        }

    }

}
