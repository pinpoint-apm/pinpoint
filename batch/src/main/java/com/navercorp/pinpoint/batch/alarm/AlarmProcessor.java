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

package com.navercorp.pinpoint.batch.alarm;

import com.google.common.base.Suppliers;
import com.navercorp.pinpoint.batch.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.batch.alarm.collector.DataCollector;
import com.navercorp.pinpoint.batch.alarm.vo.AppAlarmChecker;
import com.navercorp.pinpoint.batch.service.BatchAgentService;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.DataCollectorCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.service.AlarmService;
import com.navercorp.pinpoint.web.vo.Application;
import jakarta.annotation.Nonnull;
import org.springframework.batch.item.ItemProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author minwoo.jung
 */
public class AlarmProcessor implements ItemProcessor<Application, AppAlarmChecker> {

    private static final long activeDuration = TimeUnit.MINUTES.toMillis(5);

    private final AlarmService alarmService;

    private final DataCollectorFactory dataCollectorFactory;

    private final CheckerRegistry checkerRegistry;

    private final BatchAgentService batchAgentService;

    public AlarmProcessor(
            DataCollectorFactory dataCollectorFactory,
            AlarmService alarmService,
            CheckerRegistry checkerRegistry,
            BatchAgentService batchAgentService) {
        this.dataCollectorFactory = Objects.requireNonNull(dataCollectorFactory, "dataCollectorFactory");
        this.alarmService = Objects.requireNonNull(alarmService, "alarmService");
        this.checkerRegistry = Objects.requireNonNull(checkerRegistry, "checkerRegistry");
        this.batchAgentService = Objects.requireNonNull(batchAgentService, "batchAgentService");
    }

    @Override
    public AppAlarmChecker process(@Nonnull Application application) {
        List<AlarmChecker<?>> checkers = getAlarmCheckers(application);
        if (CollectionUtils.isEmpty(checkers)) {
            return null;
        }

        AppAlarmChecker appChecker = new AppAlarmChecker(checkers);
        appChecker.check();

        return appChecker;
    }

    private List<AlarmChecker<?>> getAlarmCheckers(Application application) {
        List<Rule> rules = alarmService.selectRuleByApplicationName(application.getName());

        long now = System.currentTimeMillis();
        Supplier<List<String>> agentIds = getAgentIdsSupplier(application, now);

        AlarmCheckerFactory alarmCheckerFactory = new AlarmCheckerFactory(
                application, agentIds, now, dataCollectorFactory, checkerRegistry);

        List<AlarmChecker<?>> checkers = new ArrayList<>(rules.size());
        for (Rule rule : rules) {
            checkers.add(alarmCheckerFactory.create(rule));
        }
        return checkers;
    }

    private Supplier<List<String>> getAgentIdsSupplier(Application application, long now) {
        Range range = Range.between(now - activeDuration, now);
        return Suppliers.memoize(() -> fetchActiveAgents(application, range));
    }

    private List<String> fetchActiveAgents(Application application, Range activeRange) {
        List<String> agentList = batchAgentService.getIds(application.getName());
        return agentList
                .stream()
                .filter(id -> {
                    Application agent = new Application(id, application.getServiceType());
                    return batchAgentService.isActive(agent, activeRange);
                })
                .toList();
    }

    private static class AlarmCheckerFactory {

        private final long timeSlotEndTime;
        private final Map<DataCollectorCategory, DataCollector> collectorMap = new HashMap<>();

        private final Application application;
        private final Supplier<List<String>> agentIds;
        private final DataCollectorFactory dataCollectorFactory;
        private final CheckerRegistry checkerRegistry;

        public AlarmCheckerFactory(
                Application application,
                Supplier<List<String>> agentIds,
                long timeSlotEndTime,
                DataCollectorFactory dataCollectorFactory,
                CheckerRegistry checkerRegistry) {
            this.application = application;
            this.agentIds = agentIds;
            this.timeSlotEndTime = timeSlotEndTime;
            this.dataCollectorFactory = dataCollectorFactory;
            this.checkerRegistry = Objects.requireNonNull(checkerRegistry, "checkerRegistry");
        }

        public AlarmChecker<?> create(Rule rule) {
            CheckerCategory checkerCategory = CheckerCategory.getValue(rule.getCheckerName());

            DataCollector collector = collectorMap.computeIfAbsent(
                    checkerCategory.getDataCollectorCategory(),
                    k -> dataCollectorFactory.createDataCollector(
                            checkerCategory, application, agentIds, timeSlotEndTime
                    )
            );

            return checkerRegistry
                    .getCheckerFactory(checkerCategory)
                    .createChecker(collector, rule);
        }
    }

}
