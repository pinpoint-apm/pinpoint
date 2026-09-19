/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.alarm.web;

import com.navercorp.pinpoint.alarm.dao.AlarmRuleV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateItemDao;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplateItem;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * The lock order every bundle-touching write shares: bundle headers first, lowest id
 * first, then the rule rows.
 * <p>
 * A bundle is applied to many applications, so an edit to the bundle and an edit to one
 * of the rules it stamped reach for the same rows. They deadlock unless every path takes
 * them in one order, and that order only holds if it lives in one place -- hence this
 * class rather than a helper on each service.
 */
@Component
class AlarmBundleLocks {

    private final AlarmRuleV2Dao ruleDao;
    private final AlarmTemplateDao templateDao;
    private final AlarmTemplateItemDao templateItemDao;

    AlarmBundleLocks(AlarmRuleV2Dao ruleDao,
                     AlarmTemplateDao templateDao,
                     AlarmTemplateItemDao templateItemDao) {
        this.ruleDao = Objects.requireNonNull(ruleDao, "ruleDao");
        this.templateDao = Objects.requireNonNull(templateDao, "templateDao");
        this.templateItemDao = Objects.requireNonNull(templateItemDao, "templateItemDao");
    }

    AlarmTemplate requireTemplate(Long templateId) {
        AlarmTemplate template = templateDao.selectById(templateId);
        if (template == null) {
            throw new AlarmResourceNotFoundException("Template not found: " + templateId);
        }
        return template;
    }

    AlarmTemplate requireTemplateForUpdate(Long templateId) {
        AlarmTemplate template = templateDao.selectByIdForUpdate(templateId);
        if (template == null) {
            throw new AlarmResourceNotFoundException("Template not found: " + templateId);
        }
        return template;
    }

    AlarmTemplateItem requireTemplateItem(Long templateItemId) {
        AlarmTemplateItem item = templateItemDao.selectById(templateItemId);
        if (item == null) {
            throw new IllegalArgumentException("Template item not found: " + templateItemId);
        }
        return item;
    }

    AlarmRuleV2 getRuleForUpdate(Long id) {
        AlarmRuleV2 rule = ruleDao.selectRuleByIdForUpdate(id);
        if (rule == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found: " + id);
        }
        return rule;
    }

    /**
     * Reads the bundle item a rule binds to, with a lock, assuming its header is already
     * locked by {@link #lockBundleHeadersOfItems}. The lock matters because validation
     * and the stamped dataSource have to see the latest committed values: an unlocked
     * read would return the transaction snapshot taken before the header lock was
     * granted, hiding a concurrent bundle edit.
     * <p>
     * Ownership is a precondition, not something re-checked here:
     * {@link #lockBundleHeadersOfItems} has already rejected an item whose header
     * belongs to another service, and neither the item's template_id nor the
     * header's service_name is updatable, so the pair cannot drift afterwards.
     */
    AlarmTemplateItem resolveTemplateItemUnderLock(AlarmRuleV2 rule) {
        if (rule.getTemplateItemId() == null) {
            return null;
        }
        AlarmTemplateItem item = templateItemDao.selectByIdForUpdate(rule.getTemplateItemId());
        if (item == null) {
            throw new IllegalArgumentException("Template item not found: " + rule.getTemplateItemId());
        }
        rule.setDataSource(item.getDataSource());
        return item;
    }

    /**
     * Locks the bundle headers owning the given items, lowest id first.
     * <p>
     * Every path that touches both a bundle and its rules takes locks in the
     * same order — headers, then rule rows — so a rule edit and a bundle edit
     * cannot deadlock each other. An item never moves between bundles (the item
     * update statement is keyed by template_id), so reading it without a lock
     * here only to learn which header to lock is safe.
     * <p>
     * Ownership is checked on that unlocked read, before the header is locked: a
     * caller-supplied item id must never let someone lock — or probe for — a bundle
     * of a service they cannot touch. Every caller passes a caller-supplied id, so
     * the guard belongs here rather than in each of them.
     */
    void lockBundleHeadersOfItems(String serviceName, Long... templateItemIds) {
        Stream.of(templateItemIds)
                .filter(Objects::nonNull)
                .distinct()
                .map(this::requireTemplateItem)
                .map(AlarmTemplateItem::getTemplateId)
                .distinct()
                .sorted()
                .forEach(templateId -> {
                    AlarmOwnerships.verifyTemplate(requireTemplate(templateId), serviceName);
                    requireTemplateForUpdate(templateId);
                });
    }

    /**
     * Locks the bundle headers involved in a rule mutation and only then the
     * rule row itself, keeping the header-before-rule order described in
     * {@link #lockBundleHeadersOfItems}.
     */
    AlarmRuleV2 lockRuleAfterBundleHeaders(Long ruleId,
                                           String serviceName,
                                           Consumer<AlarmRuleV2> verifyOwnership,
                                           Long... requestedTemplateItemIds) {
        AlarmRuleV2 rule = ruleDao.selectRuleById(ruleId);
        if (rule == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found: " + ruleId);
        }
        // Authorize before taking any lock: a caller-supplied template item id must
        // never be able to probe for, or lock, a bundle of a service they cannot touch.
        verifyOwnership.accept(rule);
        Long[] templateItemIds = Stream.concat(
                        Stream.of(rule.getTemplateItemId()),
                        Stream.of(requestedTemplateItemIds))
                .toArray(Long[]::new);
        lockBundleHeadersOfItems(serviceName, templateItemIds);

        AlarmRuleV2 lockedRule = getRuleForUpdate(ruleId);
        verifyOwnership.accept(lockedRule);
        if (!Objects.equals(rule.getTemplateItemId(), lockedRule.getTemplateItemId())) {
            throw new AlarmResourceConflictException(
                    "Rule was re-linked to another template item concurrently: ruleId=" + ruleId);
        }
        return lockedRule;
    }
}
