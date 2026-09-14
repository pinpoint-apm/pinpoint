package com.navercorp.pinpoint.alarm.web;

import com.navercorp.pinpoint.alarm.dao.AlarmRuleV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateItemDao;
import com.navercorp.pinpoint.alarm.service.AlarmDataSourceRegistry;
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplateItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Template bundles: the header, its items, and applying the pair to an application.
 * <p>
 * A bundle is a definition; the rules stamped from it are what the batch evaluates. Every
 * write here therefore reaches into {@code alarm_rule_v2} as well -- adding an item
 * stamps a rule into every application the bundle is applied to, and unapplying deletes
 * them again -- which is why it takes its locks through {@link AlarmBundleLocks} in the
 * same header-then-rule order the rule side uses.
 */
@Service
public class AlarmTemplateService {

    private final AlarmRuleV2Dao ruleDao;
    private final AlarmTemplateDao templateDao;
    private final AlarmTemplateItemDao templateItemDao;
    private final AlarmApplicationResolver applicationResolver;
    private final AlarmBundleLocks locks;
    private final AlarmConfigValidator configValidator;
    private final AlarmRuleStamper ruleStamper;
    private final AlarmRuleDeleter ruleDeleter;
    private final AlarmDataSourceRegistry dataSourceRegistry;

    public AlarmTemplateService(AlarmRuleV2Dao ruleDao,
                                AlarmTemplateDao templateDao,
                                AlarmTemplateItemDao templateItemDao,
                                AlarmApplicationResolver applicationResolver,
                                AlarmBundleLocks locks,
                                AlarmConfigValidator configValidator,
                                AlarmRuleStamper ruleStamper,
                                AlarmRuleDeleter ruleDeleter,
                                AlarmDataSourceRegistry dataSourceRegistry) {
        this.ruleDao = Objects.requireNonNull(ruleDao, "ruleDao");
        this.templateDao = Objects.requireNonNull(templateDao, "templateDao");
        this.templateItemDao = Objects.requireNonNull(templateItemDao, "templateItemDao");
        this.applicationResolver = Objects.requireNonNull(applicationResolver, "applicationResolver");
        this.locks = Objects.requireNonNull(locks, "locks");
        this.configValidator = Objects.requireNonNull(configValidator, "configValidator");
        this.ruleStamper = Objects.requireNonNull(ruleStamper, "ruleStamper");
        this.ruleDeleter = Objects.requireNonNull(ruleDeleter, "ruleDeleter");
        this.dataSourceRegistry = Objects.requireNonNull(dataSourceRegistry, "dataSourceRegistry");
    }

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public AlarmTemplate createTemplate(AlarmTemplate template) {
        AlarmOwnerships.verifyServiceName(template.getServiceName());
        List<AlarmTemplateItem> items = configValidator.requireItems(template);
        for (AlarmTemplateItem item : items) {
            configValidator.prepareItem(item);
        }
        templateDao.insert(template);
        for (AlarmTemplateItem item : items) {
            // Copy-from flows may carry source item ids; a create always mints new rows.
            item.setId(null);
            item.setTemplateId(template.getId());
            templateItemDao.insert(item);
        }
        return template;
    }

    // Header and items are two reads; without a snapshot a concurrent delete can
    // surface a bundle with an empty item list, which the write path forbids.
    @Transactional(transactionManager = "transactionManager", readOnly = true,
            isolation = Isolation.REPEATABLE_READ)
    public AlarmTemplate getTemplate(String serviceName, Long id) {
        AlarmOwnerships.verifyServiceName(serviceName);
        AlarmTemplate template = locks.requireTemplate(id);
        AlarmOwnerships.verifyTemplate(template, serviceName);
        template.setItems(templateItemDao.selectByTemplateId(id));
        return template;
    }

    @Transactional(transactionManager = "transactionManager", readOnly = true,
            isolation = Isolation.REPEATABLE_READ)
    public List<AlarmTemplate> getTemplates(String serviceName, String dataSource) {
        AlarmOwnerships.verifyServiceName(serviceName);
        List<AlarmTemplate> templates;
        if (dataSource == null) {
            templates = templateDao.selectByService(serviceName);
        } else {
            // Rejects a code no installed module owns, rather than answering "no templates".
            dataSourceRegistry.get(dataSource);
            templates = templateDao.selectByServiceAndDataSource(serviceName, dataSource);
        }
        if (templates.isEmpty()) {
            return templates;
        }
        Map<Long, List<AlarmTemplateItem>> itemsByTemplateId = templateItemDao
                .selectByTemplateIds(templates.stream().map(AlarmTemplate::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(AlarmTemplateItem::getTemplateId));
        for (AlarmTemplate template : templates) {
            template.setItems(itemsByTemplateId.getOrDefault(template.getId(), List.of()));
        }
        return templates;
    }

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void updateTemplate(AlarmTemplate template) {
        AlarmOwnerships.verifyServiceName(template.getServiceName());
        AlarmTemplate existing = locks.requireTemplateForUpdate(template.getId());
        AlarmOwnerships.verifyTemplate(existing, template.getServiceName());
        List<AlarmTemplateItem> requestedItems = configValidator.requireItems(template);

        // The header lock serializes concurrent bundle edits, so this read is stable.
        Map<Long, AlarmTemplateItem> currentItems = templateItemDao.selectByTemplateId(template.getId()).stream()
                .collect(Collectors.toMap(AlarmTemplateItem::getId, Function.identity(),
                        (left, right) -> right, LinkedHashMap::new));

        for (AlarmTemplateItem item : requestedItems) {
            configValidator.prepareItem(item);
            if (item.getId() == null) {
                continue;
            }
            AlarmTemplateItem currentItem = currentItems.get(item.getId());
            if (currentItem == null) {
                throw new IllegalArgumentException(
                        "Template item does not belong to the template: itemId=" + item.getId());
            }
            configValidator.validateItemDataSourceChange(currentItem, item);
        }

        templateDao.update(template);
        // Read before inserting, so the new items are not in the set and the lock is
        // taken while the current membership still holds. Only when there is something
        // to stamp: that read locks every rule row the bundle has produced, across every
        // application it is applied to, for the rest of the transaction, and an edit that
        // adds no item -- renaming the bundle, changing one threshold -- stamps nothing.
        // Holding those rows blocks the evaluation batch, which locks a rule before it
        // records an event.
        boolean stamping = requestedItems.stream().anyMatch(item -> item.getId() == null);
        List<AlarmApplication> appliedApplications = stamping
                ? ruleDao.selectAppliedApplicationsForUpdate(template.getId())
                : List.of();

        Map<Long, AlarmTemplateItem> removedItems = new HashMap<>(currentItems);
        List<AlarmTemplateItem> addedItems = new ArrayList<>();
        for (AlarmTemplateItem item : requestedItems) {
            item.setTemplateId(template.getId());
            if (item.getId() == null) {
                templateItemDao.insert(item);
                addedItems.add(item);
            } else {
                removedItems.remove(item.getId());
                templateItemDao.update(item);
            }
        }
        stampAddedItems(addedItems, appliedApplications);
        for (Long removedItemId : removedItems.keySet()) {
            // Soft-delete only: the rules stamped from this item disappear from evaluation,
            // notification and the API at once (activeTemplatePredicate / the outbox claim
            // query), and the cleanup batch removes them in chunks. Same contract as
            // deleting the whole bundle, so a single item never turns into an unbounded
            // delete inside the request transaction.
            templateItemDao.markDeleted(removedItemId);
        }
    }

    /**
     * An item added to a bundle has to reach the applications the bundle is already
     * applied to; without a rule row there is nothing for the batch to evaluate. Config
     * is inherited, so each new rule is just a link to the item.
     */
    private void stampAddedItems(List<AlarmTemplateItem> addedItems,
                                 List<AlarmApplication> applications) {
        if (addedItems.isEmpty() || applications.isEmpty()) {
            return;
        }
        for (AlarmApplication application : applications) {
            for (AlarmTemplateItem item : addedItems) {
                ruleStamper.stampFromItem(item, application);
            }
        }
    }

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void deleteTemplate(String serviceName, Long id) {
        AlarmOwnerships.verifyServiceName(serviceName);
        AlarmTemplate template = locks.requireTemplateForUpdate(id);
        AlarmOwnerships.verifyTemplate(template, serviceName);
        // Rules stamped from the items disappear from evaluation/API immediately
        // (activeTemplatePredicate) and are removed physically by the cleanup batch.
        // TODO: that batch arrives with the alarm batch module in a follow-up PR. Until it
        // does, a deployment that deletes templates keeps their rules, local overrides,
        // state, history and suppressed outbox rows.
        templateItemDao.markDeletedByTemplateId(id);
        templateDao.markDeleted(id);
    }

    // ---- Template apply ----

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public List<AlarmRuleV2> applyTemplate(String serviceName, String applicationName,
                                           Long templateId, String applicationType) {
        AlarmOwnerships.verifyServiceName(serviceName);
        AlarmTemplate template = locks.requireTemplateForUpdate(templateId);
        AlarmOwnerships.verifyTemplate(template, serviceName);
        AlarmApplication application =
                new AlarmApplication(serviceName, applicationName, applicationType);
        applicationResolver.verifyExists(application);
        List<AlarmRuleV2> existingRules = ruleDao.selectRulesByTemplateIdAndApplicationForUpdate(
                templateId, serviceName, applicationName, applicationType);
        if (!existingRules.isEmpty()) {
            throw new AlarmResourceConflictException(
                    "Template is already applied to the application: templateId=" + templateId
                            + ", application=" + application);
        }
        List<AlarmTemplateItem> items = templateItemDao.selectByTemplateId(templateId);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Template has no items: templateId=" + templateId);
        }

        List<AlarmRuleV2> createdRules = new ArrayList<>(items.size());
        for (AlarmTemplateItem item : items) {
            createdRules.add(ruleStamper.stampFromItem(item, application));
        }
        return createdRules;
    }

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void unapplyTemplate(String serviceName, String applicationName, Long templateId,
                                String applicationType) {
        AlarmOwnerships.verifyServiceName(serviceName);
        AlarmTemplate template = locks.requireTemplateForUpdate(templateId);
        AlarmOwnerships.verifyTemplate(template, serviceName);
        // Bind the authorization target to the deletion target, exactly like applyTemplate:
        // the permission check ran against applicationName, so the target must be it.
        applicationResolver.verifyExists(
                new AlarmApplication(serviceName, applicationName, applicationType));
        List<AlarmRuleV2> rules =
                ruleDao.selectRulesByTemplateIdAndApplicationForUpdate(
                        templateId, serviceName, applicationName, applicationType);
        for (AlarmRuleV2 rule : rules) {
            ruleDeleter.deleteRule(rule.getId());
        }
    }
}
