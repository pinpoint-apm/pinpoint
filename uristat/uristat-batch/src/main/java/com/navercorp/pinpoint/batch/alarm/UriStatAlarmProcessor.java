package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.alarm.checker.UriStatAlarmChecker;
import com.navercorp.pinpoint.batch.alarm.checker.UriStatAlarmCheckerRegistry;
import com.navercorp.pinpoint.batch.alarm.collector.UriStatDataCollectorFactory;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.pinot.alarm.checker.PinotAlarmChecker;
import com.navercorp.pinpoint.pinot.alarm.checker.PinotAlarmCheckers;
import com.navercorp.pinpoint.pinot.alarm.collector.PinotDataCollector;
import com.navercorp.pinpoint.pinot.alarm.condition.AlarmCondition;
import com.navercorp.pinpoint.pinot.alarm.condition.AlarmConditionFactory;
import com.navercorp.pinpoint.pinot.alarm.service.PinotAlarmService;
import com.navercorp.pinpoint.pinot.alarm.vo.PinotAlarmKey;
import com.navercorp.pinpoint.pinot.alarm.vo.PinotAlarmRule;
import jakarta.annotation.Nonnull;
import org.springframework.batch.item.ItemProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class UriStatAlarmProcessor implements ItemProcessor<PinotAlarmKey, PinotAlarmCheckers> {
    private final PinotAlarmService alarmService;
    private final UriStatDataCollectorFactory uriStatDataCollectorFactory;
    private final AlarmConditionFactory alarmConditionFactory;

    public UriStatAlarmProcessor(PinotAlarmService alarmService, UriStatDataCollectorFactory uriStatDataCollectorFactory, AlarmConditionFactory alarmConditionFactory) {
        this.alarmService = Objects.requireNonNull(alarmService, "pinotAlarmService");
        this.uriStatDataCollectorFactory = Objects.requireNonNull(uriStatDataCollectorFactory, "dataCollectorFactory");
        this.alarmConditionFactory = Objects.requireNonNull(alarmConditionFactory, "alarmConditionFactory");
    }

    @Override
    public PinotAlarmCheckers process(@Nonnull PinotAlarmKey alarmKeys) {
        List<PinotAlarmChecker<? extends Number>> checkers = getAlarmCheckers(alarmKeys);
        if (CollectionUtils.isEmpty(checkers)) {
            return null;
        }
        long now = System.currentTimeMillis();
        PinotAlarmCheckers appChecker = new PinotAlarmCheckers(checkers);
        appChecker.check(now);

        return appChecker;
    }

    private List<PinotAlarmChecker<? extends Number>> getAlarmCheckers(PinotAlarmKey alarmKeys) {
        List<PinotAlarmRule> rules = alarmService.selectRulesByKeys(alarmKeys);

        RuleTransformer transformer = new RuleTransformer(uriStatDataCollectorFactory, alarmConditionFactory);
        for (PinotAlarmRule rule: rules) {
            transformer.accept(rule);
        }

        return transformer.buildAlarmCheckers();
    }

    private static class RuleTransformer implements Consumer<PinotAlarmRule> {

        private static final UriStatAlarmCheckerRegistry checkerRegistry = UriStatAlarmCheckerRegistry.newCheckerRegistry();
        private final Map<UriStatAlarmChecker, Map<AlarmCondition, List<PinotAlarmRule>>> ruleMap = new HashMap<>();
        private final UriStatDataCollectorFactory uriStatDataCollectorFactory;
        private final AlarmConditionFactory alarmConditionFactory;
        private final List<PinotAlarmChecker<? extends Number>> alarmCheckers;

        public RuleTransformer(
                UriStatDataCollectorFactory uriStatDataCollectorFactory,
                AlarmConditionFactory alarmConditionFactory
        ) {
            this.uriStatDataCollectorFactory = uriStatDataCollectorFactory;
            this.alarmConditionFactory = alarmConditionFactory;
            this.alarmCheckers = new ArrayList<>();
        }

        @Override
        public void accept(PinotAlarmRule rule) {
            UriStatAlarmChecker alarmChecker = UriStatAlarmChecker.getValue(rule.getCheckerName());

            Map<AlarmCondition, List<PinotAlarmRule>> conditionMap = ruleMap.computeIfAbsent(alarmChecker, k -> new HashMap<>());

            AlarmCondition alarmCondition = alarmChecker.getAlarmConditionGetter().apply(rule);
            List<PinotAlarmRule> rules = conditionMap.computeIfAbsent(alarmCondition, k -> new ArrayList<>());
            rules.add(rule);
        }

        public List<PinotAlarmChecker<? extends Number>> buildAlarmCheckers() {
            for (Map.Entry<UriStatAlarmChecker, Map<AlarmCondition, List<PinotAlarmRule>>> entry : ruleMap.entrySet()) {
                final UriStatAlarmChecker alarmChecker = entry.getKey();
                final Map<AlarmCondition, List<PinotAlarmRule>> value = entry.getValue();

                PinotDataCollector<? extends Number> dataCollector = uriStatDataCollectorFactory.getDataCollector(alarmChecker);
                for (Map.Entry<AlarmCondition, List<PinotAlarmRule>> dataEntry : value.entrySet()) {
                    final AlarmCondition alarmCondition = dataEntry.getKey();
                    final List<PinotAlarmRule> categorizedRules = dataEntry.getValue();
                    PinotAlarmChecker<? extends Number> checker = checkerRegistry.getCheckerFactory(alarmChecker).createChecker(categorizedRules, dataCollector, alarmCondition);
                    alarmCheckers.add(checker);
                }
            }
            return alarmCheckers;
        }
    }
}
