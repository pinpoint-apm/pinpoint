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
import com.navercorp.pinpoint.common.task.TimerTaskDecorator;
import com.navercorp.pinpoint.common.task.TimerTaskDecoratorFactory;
import com.navercorp.pinpoint.realtime.dto.ATCSupply;
import com.navercorp.pinpoint.web.realtime.activethread.count.dao.ActiveThreadCountDao;
import com.navercorp.pinpoint.web.realtime.activethread.count.dto.ActiveThreadCountResponse;
import com.navercorp.pinpoint.web.realtime.service.AgentLookupService;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author youngjin.kim2
 */
public class ActiveThreadCountServiceImpl implements ActiveThreadCountService {

    private static final long MAX_CONNECTION_WAITING_MILLIS = TimeUnit.SECONDS.toMillis(5);

    private final ActiveThreadCountDao atcDao;
    private final AgentLookupService agentLookupService;
    private final TimerTaskDecoratorFactory timerTaskDecoratorFactory;
    private final Scheduler scheduler;
    private final Duration emitPeriod;
    private final Duration updatePeriod;

    public ActiveThreadCountServiceImpl(
            ActiveThreadCountDao atcDao,
            AgentLookupService agentLookupService,
            TimerTaskDecoratorFactory timerTaskDecoratorFactory,
            ScheduledExecutorService scheduledExecutor,
            Duration emitPeriod,
            Duration updatePeriod
    ) {
        this.atcDao = Objects.requireNonNull(atcDao, "atcDao");
        this.agentLookupService = Objects.requireNonNull(agentLookupService, "agentLookupService");
        this.timerTaskDecoratorFactory = Objects.requireNonNull(timerTaskDecoratorFactory, "timerTaskDecoratorFactory");
        this.scheduler = Schedulers.fromExecutorService(Objects.requireNonNull(scheduledExecutor, "scheduledExecutor"));
        this.emitPeriod = Objects.requireNonNull(emitPeriod, "emitPeriod");
        this.updatePeriod = Objects.requireNonNull(updatePeriod, "updatePeriod");
    }

    @Override
    public Flux<ActiveThreadCountResponse> getResponses(String applicationName) {
        TimerTaskDecorator taskDecorator = timerTaskDecoratorFactory.createTimerTaskDecorator();
        SupplyCollector collector = new SupplyCollector(applicationName, emitPeriod.toMillis() * 2);

        Map<ClusterKey, Disposable> disposableMap = new ConcurrentHashMap<>();

        Disposable updateDisposable = this.scheduler.schedulePeriodically(() -> {
            getAgents(taskDecorator, applicationName).subscribe(agents -> {
                for (ClusterKey agent : agents) {
                    Disposable disposable = this.atcDao.getSupplies(agent).subscribe(supply -> {
                        collector.add(supply);
                    });
                    Disposable prev = disposableMap.put(agent, disposable);
                    if (prev != null) {
                        Mono.delay(Duration.ofSeconds(3), this.scheduler).subscribe(t -> prev.dispose());
                    }
                }
                collector.updateAgents(agents);
            });
        }, 0, this.updatePeriod.toMillis(), TimeUnit.MILLISECONDS);

        return Flux.interval(this.emitPeriod, Schedulers.boundedElastic())
                .mapNotNull(collector::compose)
                .doFinally(e -> {
                    updateDisposable.dispose();
                    Mono.delay(Duration.ofMillis(500)).subscribe(t -> {
                        for (Disposable d: disposableMap.values()) {
                            d.dispose();
                        }
                    });
                });
    }

    private static ClusterKey extractKey(ATCSupply supply) {
        return new ClusterKey(supply.getApplicationName(), supply.getAgentId(), supply.getStartTimestamp());
    }

    private Mono<List<ClusterKey>> getAgents(TimerTaskDecorator taskDecorator, String applicationName) {
        return Mono.<List<ClusterKey>>create(sink -> {
            taskDecorator.decorate(new TimerTask() {
                @Override
                public void run() {
                    sink.success(agentLookupService.getRecentAgents(applicationName));
                }
            }).run();
        });
    }

    private static class SupplyCollector {
        String applicationName;
        long supplyExpiredIn;

        long sessionStartedAt = System.currentTimeMillis();
        long shouldConnectUntil = sessionStartedAt + MAX_CONNECTION_WAITING_MILLIS;

        AtomicReference<List<ClusterKey>> agentsRef;
        Map<ClusterKey, ATCSupply> supplyMap = new ConcurrentHashMap<>();
        Map<ClusterKey, Long> updatedAtMap = new ConcurrentHashMap<>();

        SupplyCollector(String applicationName, long supplyExpiredIn) {
            this.agentsRef = new AtomicReference<>(null);
            this.applicationName = applicationName;
            this.supplyExpiredIn = supplyExpiredIn;
        }

        public void add(ATCSupply supply) {
            long now = System.currentTimeMillis();
            ClusterKey key = extractKey(supply);

            if (supply.getValues().isEmpty()) {
                if (supplyMap.get(key) == null) {
                    supplyMap.put(key, supply);
                    updatedAtMap.put(key, now);
                }
            } else {
                supplyMap.put(key, supply);
                updatedAtMap.put(key, now);
            }
        }

        public ActiveThreadCountResponse compose(Long t) {
            List<ClusterKey> agents = this.agentsRef.get();
            if (agents == null) {
                return null;
            }

            long now = System.currentTimeMillis();
            ActiveThreadCountResponse response = new ActiveThreadCountResponse(applicationName, now);
            for (ClusterKey agent: agents) {
                putAgent(response, agent, now);
            }
            return response;
        }

        public void updateAgents(List<ClusterKey> agents) {
            this.agentsRef.set(agents);
        }

        private void putAgent(ActiveThreadCountResponse response, ClusterKey agent, long now) {
            ATCSupply supply = supplyMap.get(agent);
            Long updatedAt = updatedAtMap.get(agent);

            if (supply == null || updatedAt == null) {
                if (now <= this.shouldConnectUntil) {
                    response.putFailureAgent(agent, "CONNECTING");
                } else {
                    response.putFailureAgent(agent, "TIMEOUT");
                }
                return;
            }

            if (supply.getValues().isEmpty()) {
                response.putFailureAgent(agent, "CONNECTED");
                return;
            }

            if (now - updatedAt > this.supplyExpiredIn) {
                response.putFailureAgent(agent, "SLOW RESPONSE");
                return;
            }

            response.putSuccessAgent(agent, supply.getValues());
        }
    }

}
