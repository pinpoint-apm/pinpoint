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

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.web.cluster.ClusterKeyAndStatus;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.task.TimerTaskDecorator;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Taejin Koo
 */
public class WorkerActiveManager {

    private static final long DEFAULT_RECONNECT_DELAY = 5000;
    private static final long DEFAULT_AGENT_CHECK_DELAY = 10000;

    private static final long DEFAULT_AGENT_LOOKUP_TIME = TimeUnit.HOURS.toMillis(1);

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final PinpointWebSocketResponseAggregator responseAggregator;

    private final String applicationName;
    private final AgentService agentService;

    private final Timer timer;
    private final TimerTaskDecorator timerTaskDecorator;

    private final AtomicBoolean isStopped = new AtomicBoolean();

    private final Object lock = new Object();

    private final AtomicBoolean onReconnectTimerTask = new AtomicBoolean(false);
    private final Set<String> reactiveWorkerRepository = new CopyOnWriteArraySet<>();

    private final AtomicBoolean onAgentCheckTimerTask = new AtomicBoolean(false);
    private final List<String> defaultAgentIdList = new CopyOnWriteArrayList<>();

    public WorkerActiveManager(PinpointWebSocketResponseAggregator responseAggregator, AgentService agentService, Timer timer, TimerTaskDecorator timerTaskDecorator) {
        this.responseAggregator = Objects.requireNonNull(responseAggregator, "responseAggregator");
        this.agentService = Objects.requireNonNull(agentService, "agentService");

        this.timer = Objects.requireNonNull(timer, "timer");
        this.timerTaskDecorator = Objects.requireNonNull(timerTaskDecorator, "timerTaskDecorator");

        this.applicationName = this.responseAggregator.getApplicationName();
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

    public void addReactiveWorker(ClusterKey clusterKey) {
        if (this.applicationName.equals(clusterKey.getApplicationName())) {
            addReactiveWorker(clusterKey.getAgentId());
        }
    }

    public void addReactiveWorker(String applicationName, String agentId) {
        if (this.applicationName.equals(applicationName)) {
            addReactiveWorker(agentId);
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
                TimerTask reactiveTimerTask = timerTaskDecorator.decorate(new ReactiveTimerTask());
                timer.schedule(reactiveTimerTask, DEFAULT_RECONNECT_DELAY);
            }
        }
    }

    public void startAgentCheckJob() {
        logger.info("startAgentCheckJob. applicationName:{}", applicationName);

        boolean turnOn = onAgentCheckTimerTask.compareAndSet(false, true);
        if (turnOn) {
            TimerTask agentCheckTimerTask = timerTaskDecorator.decorate(new AgentCheckTimerTask());
            timer.schedule(agentCheckTimerTask, DEFAULT_AGENT_CHECK_DELAY);
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
                    final ClusterKey clusterKey = agentService.getClusterKey(applicationName, agentId);
                    if (clusterKey != null) {
                           responseAggregator.addActiveWorker(clusterKey);
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
            if(logger.isDebugEnabled()) {
                logger.debug("AgentCheckTimerTask started.");
            }

            List<ClusterKeyAndStatus> agentInfoList = Collections.emptyList();
            try {
                agentInfoList = agentService.getRecentAgentInfoList(applicationName, DEFAULT_AGENT_LOOKUP_TIME);
            } catch (Exception e) {
                logger.warn("failed while to get RecentAgentInfoList(applicationName:{}). error:{}.", applicationName, e.getMessage(), e);
            }

            try {
                for (ClusterKeyAndStatus clusterKeyStatus : agentInfoList) {
                    final ClusterKey clusterKey = clusterKeyStatus.getClusterKey();
                    final String agentId = clusterKey.getAgentId();
                    if (defaultAgentIdList.contains(agentId)) {
                        continue;
                    }

                    final AgentStatus agentStatus = clusterKeyStatus.getStatus();
                    if (agentStatus != null && agentStatus.getState() != AgentLifeCycleState.UNKNOWN) {
                        addActiveWorker(clusterKey);
                    } else if (agentService.isConnected(clusterKey)) {
                        addActiveWorker(clusterKey);
                    }
                }
            } finally {
                final Timer timer = WorkerActiveManager.this.timer;
                if (timer != null && onAgentCheckTimerTask.get() && !isStopped.get()) {
                    TimerTask agentCheckTimerTask = timerTaskDecorator.decorate(new AgentCheckTimerTask());
                    timer.schedule(agentCheckTimerTask, DEFAULT_AGENT_CHECK_DELAY);
                }
            }
        }

        private void addActiveWorker(ClusterKey clusterKey) {
            try {
                responseAggregator.addActiveWorker(clusterKey);
                defaultAgentIdList.add(clusterKey.getAgentId());
            } catch (Exception e) {
                logger.warn("failed while adding active worker. error:{}", e.getMessage(), e);
            }
        }

    }

}
