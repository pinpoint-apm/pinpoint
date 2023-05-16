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

import com.navercorp.pinpoint.batch.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.batch.alarm.collector.DataCollector;
import com.navercorp.pinpoint.batch.alarm.vo.AppAlarmChecker;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.DataCollectorCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.service.AlarmService;
import com.navercorp.pinpoint.web.vo.Application;
import org.springframework.batch.item.ItemProcessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author minwoo.jung
 */
public class AlarmProcessor implements ItemProcessor<Application, AppAlarmChecker> {

    private static final long activeDuration = TimeUnit.MINUTES.toMillis(5);

    private final AlarmService alarmService;

    private final DataCollectorFactory dataCollectorFactory;

    private final ApplicationIndexDao applicationIndexDao;

    private final AgentInfoService agentInfoService;

    public AlarmProcessor(
            DataCollectorFactory dataCollectorFactory,
            AlarmService alarmService,
            ApplicationIndexDao applicationIndexDao,
            AgentInfoService agentInfoService
    ) {
        this.dataCollectorFactory = Objects.requireNonNull(dataCollectorFactory, "dataCollectorFactory");
        this.alarmService = Objects.requireNonNull(alarmService, "alarmService");
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
    }

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
        List<Rule> rules = alarmService.selectRuleByApplicationId(application.getName());
        List<AlarmChecker<?>> checkers = new ArrayList<>(rules.size());

        long now = System.currentTimeMillis();
        List<String> agentIds = prepareActiveAgentIds(application, rules, now);

        RuleTransformer transformer = new RuleTransformer(application, agentIds, now, dataCollectorFactory);
        for (Rule rule: rules) {
            checkers.add(transformer.apply(rule));
        }

        return checkers;
    }

    @Nullable
    private List<String> prepareActiveAgentIds(Application application, List<Rule> rules, long now) {
        Range activeRange = Range.between(now - activeDuration, now);
        List<String> agentIds = null;
        if (isRequireAgentList(rules)) {
            agentIds = fetchActiveAgents(application.getName(), activeRange);
        }
        return agentIds;
    }

    private static boolean isRequireAgentList(List<Rule> rules) {
        return rules.stream()
                .anyMatch(rule ->
                        CheckerCategory.getValue(rule.getCheckerName())
                            .getDataCollectorCategory()
                            .isRequireAgentList()
                );
    }

    private List<String> fetchActiveAgents(String applicationId, Range activeRange) {
        return applicationIndexDao.selectAgentIds(applicationId)
                .stream()
                .filter(id -> agentInfoService.isActiveAgent(id, activeRange))
                .collect(Collectors.toUnmodifiableList());
    }

    private static class RuleTransformer implements Function<Rule, AlarmChecker<?>> {

        private static final CheckerRegistry checkerRegistry = CheckerRegistry.newCheckerRegistry();

        private final long timeSlotEndTime;
        private final Map<DataCollectorCategory, DataCollector> collectorMap = new HashMap<>();

        private final Application application;
        private final List<String> agentIds;
        private final DataCollectorFactory dataCollectorFactory;

        public RuleTransformer(
                Application application,
                List<String> agentIds,
                long timeSlotEndTime,
                DataCollectorFactory dataCollectorFactory
        ) {
            this.application = application;
            this.agentIds = agentIds;
            this.timeSlotEndTime = timeSlotEndTime;
            this.dataCollectorFactory = dataCollectorFactory;
        }

        @Override
        public AlarmChecker<?> apply(Rule rule) {
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
