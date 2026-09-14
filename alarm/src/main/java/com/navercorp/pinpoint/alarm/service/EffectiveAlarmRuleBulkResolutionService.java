package com.navercorp.pinpoint.alarm.service;

import com.navercorp.pinpoint.alarm.dao.AlarmRuleLocalConfigDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateItemDao;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplateItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class EffectiveAlarmRuleBulkResolutionService {

    private static final Logger logger = LogManager.getLogger(EffectiveAlarmRuleBulkResolutionService.class);

    private final AlarmRuleLocalConfigDao localConfigDao;
    private final AlarmTemplateItemDao templateItemDao;
    private final AlarmTemplateDao templateDao;
    private final EffectiveAlarmRuleResolver resolver;

    public EffectiveAlarmRuleBulkResolutionService(AlarmRuleLocalConfigDao localConfigDao,
                                                   AlarmTemplateItemDao templateItemDao,
                                                   AlarmTemplateDao templateDao,
                                                   EffectiveAlarmRuleResolver resolver) {
        this.localConfigDao = Objects.requireNonNull(localConfigDao, "localConfigDao");
        this.templateItemDao = Objects.requireNonNull(templateItemDao, "templateItemDao");
        this.templateDao = Objects.requireNonNull(templateDao, "templateDao");
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    public ResolveResult resolveWithFailures(List<AlarmRuleV2> rules) {
        if (CollectionUtils.isEmpty(rules)) {
            return new ResolveResult(List.of(), List.of());
        }

        AlarmRuleResolutionContext context =
                AlarmRuleResolutionContext.load(rules, localConfigDao, templateItemDao, templateDao);

        List<AlarmRuleV2> effectiveRules = new ArrayList<>(rules.size());
        List<ResolveFailure> failures = new ArrayList<>();
        for (AlarmRuleV2 rule : rules) {
            try {
                AlarmTemplateItem item = context.itemOf(rule);
                effectiveRules.add(resolver.resolve(
                        rule, item, context.templateOf(item), context.localConfigOf(rule)));
            } catch (RuntimeException e) {
                logger.error("Failed to resolve effective alarm rule: {}", rule, e);
                failures.add(new ResolveFailure(rule, e));
            }
        }
        return new ResolveResult(List.copyOf(effectiveRules), List.copyOf(failures));
    }

    public record ResolveResult(List<AlarmRuleV2> rules, List<ResolveFailure> failures) {
    }

    public record ResolveFailure(AlarmRuleV2 rule, RuntimeException exception) {
    }
}
