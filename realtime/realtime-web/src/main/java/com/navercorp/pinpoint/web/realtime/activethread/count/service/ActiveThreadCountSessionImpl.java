/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.web.realtime.activethread.count.service;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.realtime.dto.ATCSupply;
import com.navercorp.pinpoint.web.realtime.activethread.count.dao.ActiveThreadCountDao;
import com.navercorp.pinpoint.web.realtime.activethread.count.dto.ActiveThreadCountResponse;
import com.navercorp.pinpoint.web.realtime.service.AgentLookupService;
import com.navercorp.pinpoint.web.task.TimerTaskDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author youngjin.kim2
 */
public class ActiveThreadCountSessionImpl implements ActiveThreadCountSession {

    private static final Logger logger = LogManager.getLogger(ActiveThreadCountServiceImpl.class);
    private static final long MAX_CONNECTION_WAITING_MILLIS = TimeUnit.SECONDS.toMillis(5);

    private final String applicationName;
    private final ActiveThreadCountDao atcDao;
    private final AgentLookupService agentLookupService;
    private final ScheduledExecutorService scheduledExecutor;
    private final ATCPeriods periods;
    private final TimerTaskDecorator timerTaskDecorator;

    private final Object lock = new Object();

    private enum State {
        IDLE,
        RUNNING,
        COMPLETE,
    }

    private volatile State state = State.IDLE;

    private final Sinks.Many<ActiveThreadCountResponse> sink = Sinks.many().replay().all();
    private final Map<ClusterKey, ATCSupply> supplyMap = new ConcurrentHashMap<>();
    private final AtomicReference<List<ClusterKey>> agentsRef = new AtomicReference<>(List.of());
    private final AtomicLong startedAt = new AtomicLong(0);

    ActiveThreadCountSessionImpl(
            String applicationName,
            ActiveThreadCountDao atcDao,
            AgentLookupService agentLookupService,
            ScheduledExecutorService scheduledExecutor,
            ATCPeriods periods,
            TimerTaskDecorator timerTaskDecorator
    ) {
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.atcDao = Objects.requireNonNull(atcDao, "atcDao");
        this.agentLookupService = Objects.requireNonNull(agentLookupService, "agentLookupService");
        this.scheduledExecutor = Objects.requireNonNull(scheduledExecutor, "scheduledExecutor");
        this.periods = Objects.requireNonNullElseGet(periods, ATCPeriods::new);
        this.timerTaskDecorator = Objects.requireNonNull(timerTaskDecorator, "timerTaskDecorator");
    }

    @Override
    public Flux<ActiveThreadCountResponse> start() {
        synchronized (lock) {
            if (this.state != State.IDLE) {
                throw new RuntimeException("Could not start: already running");
            }
            this.state = State.RUNNING;
            start0();
        }

        return this.sink.asFlux();
    }

    private void start0() {
        updateAgents();
        startPeriodicTasks();
        logger.debug("Started atc for {} - {}", this.applicationName, this);
    }

    private void startPeriodicTasks() {
        this.startedAt.set(System.currentTimeMillis());

        PeriodicRunner periodicRunner = PeriodicRunner
                .executedBy(this.scheduledExecutor)
                .continueWhen(() -> this.state == State.RUNNING);

        periodicRunner.runWithFixedDelay(
                this::emitNext, Duration.ZERO, this.periods.getPeriodEmit());
        periodicRunner.runWithFixedDelay(
                this::refreshATC, Duration.ZERO, this.periods.getPeriodRefresh());
        periodicRunner.runWithFixedDelay(
                this::updateAgents, this.periods.getPeriodUpdate(), this.periods.getPeriodUpdate());
    }

    @Override
    public void dispose() {
        logger.debug("Disposing atc for {} - {}", this.applicationName, this);
        synchronized (lock) {
            this.state = State.COMPLETE;
        }
    }

    private void emitNext() {
        ActiveThreadCountResponse response = this.buildResponse();
        this.sink.emitNext(response, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    private void refreshATC() {
        logger.debug("Updating atc for {} - {}", applicationName, this);
        for (ClusterKey agent: agentsRef.get()) {
            atcDao.getSupplies(agent).subscribe(supply -> {
                logger.trace("Put {}: {}", agent, supply.getValues());
                if (supply.getMessage().getMessage().equals("CONNECTED")) {
                    supplyMap.putIfAbsent(agent, supply);
                } else {
                    supplyMap.put(agent, supply);
                }
            });
        }
    }

    private void updateAgents() {
        logger.debug("Updating agents for {} - {}", applicationName, this);
        this.timerTaskDecorator.decorate(new TimerTask() {
            @Override
            public void run() {
                List<ClusterKey> agents = agentLookupService.getRecentAgents(applicationName);
                agentsRef.set(agents);
            }
        }).run();
    }

    private ActiveThreadCountResponse buildResponse() {
        long now = System.currentTimeMillis();
        ActiveThreadCountResponse response = new ActiveThreadCountResponse(applicationName, now);
        for (ClusterKey agent: this.agentsRef.get()) {
            ATCSupply supply = supplyMap.get(agent);
            this.putAgent(response, agent, supply);
        }
        return response;
    }

    private void putAgent(ActiveThreadCountResponse response, ClusterKey agentKey, ATCSupply supply) {
        if (supply != null && !supply.getValues().isEmpty()) {
            response.putSuccessAgent(agentKey, supply.getValues());
        } else {
            long connectUntil = this.startedAt.get() + MAX_CONNECTION_WAITING_MILLIS;
            response.putFailureAgent(agentKey, supply, connectUntil);
        }
    }

    static class ATCPeriods {
        private final Duration periodEmit;
        private final Duration periodRefresh;
        private final Duration periodUpdate;

        public ATCPeriods(Duration periodEmit, Duration periodRefresh, Duration periodUpdate) {
            this.periodEmit = periodEmit;
            this.periodRefresh = periodRefresh;
            this.periodUpdate = periodUpdate;
        }

        public ATCPeriods() {
            this.periodEmit = Duration.ofSeconds(1);
            this.periodRefresh = Duration.ofSeconds(10);
            this.periodUpdate = Duration.ofSeconds(30);
        }

        public Duration getPeriodEmit() {
            return periodEmit;
        }
        public Duration getPeriodRefresh() {
            return periodRefresh;
        }
        public Duration getPeriodUpdate() {
            return periodUpdate;
        }

    }

}
