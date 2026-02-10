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
import com.navercorp.pinpoint.common.server.task.TaskDecoratorFactory;
import com.navercorp.pinpoint.realtime.dto.ATCSupply;
import com.navercorp.pinpoint.web.realtime.activethread.count.dao.ActiveThreadCountDao;
import com.navercorp.pinpoint.web.realtime.activethread.count.dto.ActiveThreadCountResponse;
import com.navercorp.pinpoint.web.realtime.activethread.count.dto.ClusterKeyAndMetadata;
import com.navercorp.pinpoint.web.realtime.service.AgentLookupService;
import org.springframework.core.task.TaskDecorator;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author youngjin.kim2
 */
public class ActiveThreadCountServiceImpl implements ActiveThreadCountService {

    private static final long MAX_CONNECTION_WAITING_MILLIS = TimeUnit.SECONDS.toMillis(5);

    private final ActiveThreadCountDao atcDao;
    private final AgentLookupService agentLookupService;
    private final TaskDecoratorFactory taskDecoratorFactory;
    private final Scheduler scheduler;
    private final Duration emitPeriod;
    private final Duration updatePeriod;

    public ActiveThreadCountServiceImpl(
            ActiveThreadCountDao atcDao,
            AgentLookupService agentLookupService,
            TaskDecoratorFactory taskDecoratorFactory,
            ScheduledExecutorService scheduledExecutor,
            Duration emitPeriod,
            Duration updatePeriod
    ) {
        this.atcDao = Objects.requireNonNull(atcDao, "atcDao");
        this.agentLookupService = Objects.requireNonNull(agentLookupService, "agentLookupService");
        this.taskDecoratorFactory = Objects.requireNonNull(taskDecoratorFactory, "taskDecoratorFactory");
        this.scheduler = Schedulers.fromExecutorService(Objects.requireNonNull(scheduledExecutor, "scheduledExecutor"));
        this.emitPeriod = Objects.requireNonNull(emitPeriod, "emitPeriod");
        this.updatePeriod = Objects.requireNonNull(updatePeriod, "updatePeriod");
    }

    @Override
    public Flux<ActiveThreadCountResponse> getResponses(String applicationName) {
        TaskDecorator taskDecorator = taskDecoratorFactory.createDecorator();
        SupplyCollector collector = new SupplyCollector(applicationName, emitPeriod.toMillis() * 2);

        Map<ClusterKey, Disposable> disposableMap = new ConcurrentHashMap<>();

        Disposable updateDisposable = this.scheduler.schedulePeriodically(() -> {
            getAgents(taskDecorator, applicationName).subscribe(agents -> {
                for (ClusterKeyAndMetadata agentMetadata : agents) {
                    ClusterKey agent = agentMetadata.key();
                    Flux<ATCSupply> supplies = this.atcDao.getSupplies(agent);
                    Disposable disposable = supplies.subscribe(collector::add);
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
                        for (Disposable d : disposableMap.values()) {
                            d.dispose();
                        }
                    });
                });
    }

    private static ClusterKey extractKey(ATCSupply supply) {
        return new ClusterKey(supply.getApplicationName(), supply.getAgentId(), supply.getStartTimestamp());
    }

    private Mono<List<ClusterKeyAndMetadata>> getAgents(TaskDecorator taskDecorator, String applicationName) {
        return Mono.create(sink -> {
            taskDecorator.decorate(new Runnable() {
                @Override
                public void run() {
                    sink.success(agentLookupService.getRecentAgents(applicationName));
                }
            }).run();
        });
    }

    private static class SupplyCollector {
        @SuppressWarnings("rawtypes")
        private static final AtomicReferenceFieldUpdater<SupplyCollector, List> REF
                = AtomicReferenceFieldUpdater.newUpdater(SupplyCollector.class, List.class, "agents");
        String applicationName;
        long supplyExpiredIn;

        long sessionStartedAt = System.currentTimeMillis();
        long shouldConnectUntil = sessionStartedAt + MAX_CONNECTION_WAITING_MILLIS;

        private volatile List<ClusterKeyAndMetadata> agents;
        Map<ClusterKey, ATCSupply> supplyMap = new ConcurrentHashMap<>();
        Map<ClusterKey, Long> updatedAtMap = new ConcurrentHashMap<>();

        SupplyCollector(String applicationName, long supplyExpiredIn) {
            this.agents = null;
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
            @SuppressWarnings("unchecked")
            List<ClusterKeyAndMetadata> agents = REF.get(this);
            if (agents == null) {
                return null;
            }

            long now = System.currentTimeMillis();
            ActiveThreadCountResponse response = new ActiveThreadCountResponse(applicationName, now);
            for (ClusterKeyAndMetadata agentMetadata : agents) {
                putAgent(response, agentMetadata, now);
            }
            return response;
        }

        public void updateAgents(List<ClusterKeyAndMetadata> agents) {
            REF.set(this, agents);
        }

        private void putAgent(ActiveThreadCountResponse response, ClusterKeyAndMetadata agentMetadata, long now) {
            ClusterKey agent = agentMetadata.key();
            ATCSupply supply = supplyMap.get(agent);
            Long updatedAt = updatedAtMap.get(agent);

            if (supply == null || updatedAt == null) {
                if (now <= this.shouldConnectUntil) {
                    response.putFailureAgent(agentMetadata, "CONNECTING");
                } else {
                    response.putFailureAgent(agentMetadata, "TIMEOUT");
                }
                return;
            }

            if (supply.getValues().isEmpty()) {
                response.putFailureAgent(agentMetadata, "CONNECTED");
                return;
            }

            if (now - updatedAt > this.supplyExpiredIn) {
                response.putFailureAgent(agentMetadata, "SLOW RESPONSE");
                return;
            }

            response.putSuccessAgent(agentMetadata, supply.getValues());
        }
    }

}
