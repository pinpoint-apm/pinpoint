package com.navercorp.pinpoint.alarm.web;

import com.navercorp.pinpoint.alarm.dao.AlarmChannelBindingDao;
import com.navercorp.pinpoint.alarm.dao.AlarmHistoryV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleLocalConfigDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmStateDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateItemDao;
import com.navercorp.pinpoint.alarm.service.AlarmRuleResolutionContext;
import com.navercorp.pinpoint.alarm.service.EffectiveAlarmRuleResolver;
import com.navercorp.pinpoint.alarm.vo.AlarmChannelOwnerType;
import com.navercorp.pinpoint.alarm.vo.AlarmHistoryV2;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleDetails;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleLocalConfig;
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmState;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplateItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

/**
 * Individual rules: the row the evaluation batch reads, and the local overrides layered
 * on top of it.
 * <p>
 * A rule is either standalone -- carrying its own config -- or stamped from a bundle
 * item, in which case the config is inherited and only the differences live in
 * {@code alarm_rule_local_config}. Both shapes go through the same writes here, which is
 * why validation and the inherit/override decision are delegated rather than branched on
 * in each method.
 */
@Service
public class AlarmRuleService {
    private final AlarmRuleV2Dao ruleDao;
    private final AlarmApplicationResolver applicationResolver;
    private final AlarmRuleLocalConfigDao localConfigDao;
    private final AlarmTemplateDao templateDao;
    private final AlarmTemplateItemDao templateItemDao;
    private final AlarmChannelBindingDao channelBindingDao;
    private final AlarmHistoryV2Dao historyDao;
    private final AlarmStateDao stateDao;
    private final EffectiveAlarmRuleResolver effectiveRuleResolver;
    private final AlarmBundleLocks locks;
    private final AlarmConfigValidator configValidator;
    private final AlarmRuleStamper ruleStamper;
    private final AlarmRuleDeleter ruleDeleter;

    public AlarmRuleService(AlarmRuleV2Dao ruleDao,
                            AlarmRuleLocalConfigDao localConfigDao,
                            AlarmTemplateDao templateDao,
                            AlarmTemplateItemDao templateItemDao,
                            AlarmChannelBindingDao channelBindingDao,
                            AlarmHistoryV2Dao historyDao,
                            AlarmStateDao stateDao,
                            EffectiveAlarmRuleResolver effectiveRuleResolver,
                            AlarmApplicationResolver applicationResolver,
                            AlarmBundleLocks locks,
                            AlarmConfigValidator configValidator,
                            AlarmRuleStamper ruleStamper,
                            AlarmRuleDeleter ruleDeleter) {
        this.ruleDao = Objects.requireNonNull(ruleDao, "ruleDao");
        this.applicationResolver = Objects.requireNonNull(applicationResolver, "applicationResolver");
        this.localConfigDao = Objects.requireNonNull(localConfigDao, "localConfigDao");
        this.templateDao = Objects.requireNonNull(templateDao, "templateDao");
        this.templateItemDao = Objects.requireNonNull(templateItemDao, "templateItemDao");
        this.channelBindingDao = Objects.requireNonNull(channelBindingDao, "channelBindingDao");
        this.historyDao = Objects.requireNonNull(historyDao, "historyDao");
        this.stateDao = Objects.requireNonNull(stateDao, "stateDao");
        this.effectiveRuleResolver = Objects.requireNonNull(effectiveRuleResolver, "effectiveRuleResolver");
        this.locks = Objects.requireNonNull(locks, "locks");
        this.configValidator = Objects.requireNonNull(configValidator, "configValidator");
        this.ruleStamper = Objects.requireNonNull(ruleStamper, "ruleStamper");
        this.ruleDeleter = Objects.requireNonNull(ruleDeleter, "ruleDeleter");
    }

    // ---- Rule CRUD ----

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public AlarmRuleV2 createRule(AlarmRuleV2 rule) {
        AlarmOwnerships.verifyServiceName(rule.getServiceName());
        verifyApplication(rule);
        rule.setConditions(AlarmConfigValidator.normalizeConditions(rule.getConditions()));
        locks.lockBundleHeadersOfItems(rule.getServiceName(), rule.getTemplateItemId());
        AlarmTemplateItem templateItem = locks.resolveTemplateItemUnderLock(rule);
        configValidator.validateRuleConfig(rule, templateItem);
        configValidator.roundUpIntervals(rule);
        ruleStamper.insertWithInitialState(rule);
        saveLocalConfig(rule);
        return rule;
    }

    public AlarmRuleResponse getRuleResponse(Long id, AlarmApplication application) {
        AlarmRuleDetails details = ruleDao.selectRuleDetailsById(id);
        if (details == null) {
            throw new AlarmResourceNotFoundException("Rule not found: " + id);
        }
        AlarmRuleV2 rule = AlarmOwnerships.requireRule(details.getRule(), id);
        AlarmOwnerships.verifyRule(rule, application);
        return toRuleResponse(rule, details.getLocalConfig(), details.getTemplateItem(), details.getTemplate());
    }

    public List<AlarmRuleV2> getRulesByApplication(AlarmApplication application) {
        return ruleDao.selectRulesByApplication(application.getServiceName(),
                application.getApplicationName(), application.getApplicationType());
    }

    @Transactional(transactionManager = "transactionManager", readOnly = true,
            isolation = Isolation.REPEATABLE_READ)
    public List<AlarmRuleResponse> getRuleResponsesByApplication(AlarmApplication application) {
        List<AlarmRuleV2> rules = getRulesByApplication(application);
        AlarmRuleResolutionContext context =
                AlarmRuleResolutionContext.load(rules, localConfigDao, templateItemDao, templateDao);
        return rules.stream()
                .map(rule -> toRuleResponse(rule, context))
                .toList();
    }

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void updateRule(Long id, AlarmRuleV2 rule) {
        AlarmOwnerships.verifyServiceName(rule.getServiceName());
        // A rule update can move between bundles, so both headers are locked before the rule row.
        AlarmRuleV2 currentRule = locks.lockRuleAfterBundleHeaders(id, rule.getServiceName(),
                locked -> AlarmOwnerships.verifyRule(locked, rule.getServiceName(), rule.getApplicationName()),
                rule.getTemplateItemId());
        rule.setId(id);
        verifyApplication(rule);
        rule.setConditions(AlarmConfigValidator.normalizeConditions(rule.getConditions()));
        AlarmTemplateItem templateItem = locks.resolveTemplateItemUnderLock(rule);
        configValidator.validateRuleConfig(rule, templateItem);
        configValidator.roundUpIntervals(rule);
        ruleDao.updateRule(rule);
        saveLocalConfig(rule);
        if (rule.getTemplateItemId() != null) {
            channelBindingDao.deleteByOwner(AlarmChannelOwnerType.RULE, rule.getId());
        }
    }

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void updateEnabled(String serviceName, String applicationName, Long id, boolean enabled) {
        AlarmOwnerships.verifyServiceName(serviceName);
        locks.lockRuleAfterBundleHeaders(id, serviceName,
                locked -> AlarmOwnerships.verifyRule(locked, serviceName, applicationName));
        ruleDao.updateEnabled(id, enabled);
        AlarmState state = stateDao.selectByRuleId(id);
        if (state == null) {
            state = new AlarmState(id);
        }
        state.setNextCheckAt(enabled ? LocalDateTime.now(ZoneOffset.UTC) : null);
        stateDao.upsert(state);
    }

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void deleteRule(String serviceName, String applicationName, Long id) {
        AlarmOwnerships.verifyServiceName(serviceName);
        AlarmRuleV2 rule = locks.getRuleForUpdate(id);
        AlarmOwnerships.verifyRule(rule, serviceName, applicationName);
        ruleDeleter.deleteRule(id);
    }

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void deleteRulesByApplication(AlarmApplication application) {
        if (application == null) {
            return;
        }
        ruleDeleter.deleteRulesByApplication(application);
    }

    // ---- Rule state & history ----

    public AlarmState getRuleState(Long ruleId, AlarmApplication application) {
        requireRuleForApplication(ruleId, application);
        return stateDao.selectByRuleId(ruleId);
    }

    public List<AlarmHistoryV2> getRuleHistory(Long ruleId, AlarmApplication application, int limit) {
        requireRuleForApplication(ruleId, application);
        return historyDao.selectByRuleId(ruleId, limit);
    }


    private AlarmRuleV2 requireRuleForApplication(Long ruleId, AlarmApplication application) {
        AlarmRuleV2 rule = AlarmOwnerships.requireRule(ruleDao.selectRuleById(ruleId), ruleId);
        AlarmOwnerships.verifyRule(rule, application);
        return rule;
    }

    private void verifyApplication(AlarmRuleV2 rule) {
        AlarmApplication application = applicationOf(rule);
        applicationResolver.verifyExists(application);
    }

    private static AlarmApplication applicationOf(AlarmRuleV2 rule) {
        return new AlarmApplication(rule.getServiceName(), rule.getApplicationName(),
                rule.getApplicationType());
    }

    private AlarmRuleResponse toRuleResponse(AlarmRuleV2 rule, AlarmRuleResolutionContext context) {
        AlarmRuleLocalConfig localConfig = context.localConfigOf(rule);
        AlarmTemplateItem templateItem = context.itemOf(rule);
        AlarmRuleResponse response =
                toRuleResponse(rule, localConfig, templateItem, context.templateOf(templateItem));
        // List view already carries the resolved values via the top-level fields (see from());
        // drop the nested payload to keep the list response lean.
        response.setLocalConfig(null);
        response.setTemplateItem(null);
        return response;
    }

    private AlarmRuleResponse toRuleResponse(AlarmRuleV2 rule,
                                             AlarmRuleLocalConfig localConfig,
                                             AlarmTemplateItem templateItem,
                                             AlarmTemplate template) {
        AlarmRuleV2 effectiveRule = effectiveRuleResolver.resolve(rule, templateItem, template, localConfig);
        return AlarmRuleResponse.from(effectiveRule, localConfig, templateItem);
    }

    private void saveLocalConfig(AlarmRuleV2 rule) {
        AlarmRuleLocalConfig config = new AlarmRuleLocalConfig();
        config.setRuleId(rule.getId());
        config.setName(rule.getName());
        config.setDescription(rule.getDescription());
        config.setSeverity(rule.getSeverity());
        config.setCheckIntervalSec(rule.getCheckIntervalSec());
        config.setActionIntervalSec(rule.getActionIntervalSec());
        config.setConditions(rule.getConditions());
        config.setFilters(rule.getFilters());
        if (rule.getTemplateItemId() != null && config.isEmpty()) {
            localConfigDao.delete(rule.getId());
            return;
        }
        localConfigDao.upsert(config);
    }
}
