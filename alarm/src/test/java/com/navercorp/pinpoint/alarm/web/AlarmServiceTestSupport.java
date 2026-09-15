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

import com.navercorp.pinpoint.alarm.service.AlarmApplicationExistenceChecker;
import com.navercorp.pinpoint.alarm.service.AlarmDataSourceRegistry;
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.vo.TestAlarmDataSource;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationChannelDao;
import com.navercorp.pinpoint.alarm.dao.AlarmChannelBindingDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleLocalConfigDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateItemDao;
import com.navercorp.pinpoint.alarm.vo.AlarmChannelBinding;
import com.navercorp.pinpoint.alarm.vo.AlarmChannelOwnerType;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationChannel;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleDetails;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleLocalConfig;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmSeverity;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplateItem;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Fakes and builders the three alarm service tests share.
 * <p>
 * The recording DAOs stand in for MyBatis mappers so a test can assert what was written
 * and in what order -- which is the point of most of these tests -- and they are shared
 * because rule, bundle and channel writes all reach the same tables.
 */
abstract class AlarmServiceTestSupport {
    static final String SERVICE_NAME = "DEFAULT";

    /** What a deployable installs. Immutable, so the tests can share one. */
    static final AlarmDataSourceRegistry DATA_SOURCE_REGISTRY =
            new AlarmDataSourceRegistry(List.of(new TestAlarmDataSource.Provider()));

    static final String APPLICATION_NAME = "test-app";
    static final AlarmApplication APPLICATION = new AlarmApplication(
            SERVICE_NAME, "test-app", AlarmApplication.TYPE_JAVASCRIPT);

    /** Rule ids the application delete resolves before it touches any child table. */
    static final List<Long> APPLICATION_RULE_IDS = List.of(11L, 12L);

    /**
     * The one order every path deletes a rule's dependants in, matching
     * AlarmTemplateCleanupTasklet. Two paths taking these rows in different orders
     * deadlock each other, and the rule row has to go last because the other five
     * statements join it to find their targets.
     */
    static final List<String> DELETION_ORDER =
            List.of("outbox", "history", "binding", "localConfig", "state", "rule");

    static Answer<Void> log(List<String> deletionLog, String name) {
        return invocation -> {
            deletionLog.add(name);
            return null;
        };
    }

    AlarmNotificationChannel channel(Long id) {
        AlarmNotificationChannel channel = new AlarmNotificationChannel();
        channel.setId(id);
        channel.setServiceName(SERVICE_NAME);
        return channel;
    }

    AlarmNotificationChannel webhookChannel(Long id, String destination) {
        AlarmNotificationChannel channel = channel(id);
        channel.setMethodType(AlarmMethodType.WEBHOOK);
        channel.setDestination(destination);
        return channel;
    }

    AlarmNotificationChannel userGroupChannel(Long id, AlarmMethodType methodType, String userGroupId) {
        AlarmNotificationChannel channel = channel(id);
        channel.setMethodType(methodType);
        channel.setDestination(userGroupId);
        return channel;
    }

    AlarmRuleV2 validRule(Long id) {
        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setId(id);
        rule.setName("rule");
        rule.setSeverity(AlarmSeverity.CRITICAL);
        rule.setDataSource(TestAlarmDataSource.PRIMARY.name());
        rule.setApplicationType(AlarmApplication.TYPE_JAVASCRIPT);
        rule.setServiceName(SERVICE_NAME);
        rule.setApplicationName(APPLICATION_NAME);
        rule.setCheckIntervalSec(60);
        rule.setActionIntervalSec(60);
        rule.setConditions(validCondition());
        rule.setEnabled(true);
        return rule;
    }

    AlarmCondition validCondition() {
        AlarmCondition condition = new AlarmCondition();
        condition.setType(AlarmCondition.Type.LEAF);
        condition.setMetric("event_count");
        condition.setOp(AlarmCondition.ComparisonOp.GTE);
        condition.setThreshold(100.0);
        condition.setWindowSec(300);
        condition.setAggregation(AlarmCondition.Aggregation.COUNT);
        return condition;
    }

    AlarmCondition validSessionCondition() {
        AlarmCondition condition = validCondition();
        condition.setMetric("total_count");
        condition.setAggregation(AlarmCondition.Aggregation.SUM);
        return condition;
    }

    AlarmTemplate template(Long id, String serviceName) {
        AlarmTemplate template = new AlarmTemplate();
        template.setId(id);
        template.setServiceName(serviceName);
        template.setName("template");
        return template;
    }

    AlarmTemplateItem item(Long id, Long templateId) {
        AlarmTemplateItem item = itemRequest();
        item.setId(id);
        item.setTemplateId(templateId);
        return item;
    }

    AlarmTemplateItem itemRequest() {
        AlarmTemplateItem item = new AlarmTemplateItem();
        item.setName("item");
        item.setSeverity(AlarmSeverity.WARNING);
        item.setDataSource(TestAlarmDataSource.PRIMARY.name());
        item.setCheckIntervalSec(60);
        item.setActionIntervalSec(300);
        item.setConditions(validCondition());
        item.setFilters(List.of());
        return item;
    }

    @SuppressWarnings("unchecked")
    <T> T stub(Class<T> type) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> {
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    static class RecordingTemplateDao implements AlarmTemplateDao {
        final Map<Long, AlarmTemplate> templates = new HashMap<>();
        int insertedCount;
        int updatedCount;
        int markedDeletedCount;
        final List<Long> lockedIds = new ArrayList<>();
        List<String> lockLog = new ArrayList<>();

        RecordingTemplateDao() {
            this(null);
        }

        RecordingTemplateDao(AlarmTemplate template) {
            if (template != null) {
                templates.put(template.getId(), template);
            }
        }

        void addTemplate(AlarmTemplate template) {
            templates.put(template.getId(), template);
        }

        RecordingTemplateDao withLockLog(List<String> lockLog) {
            this.lockLog = lockLog;
            return this;
        }

        @Override
        public void insert(AlarmTemplate template) {
            insertedCount++;
            if (template.getId() == null) {
                template.setId(30L);
            }
            templates.put(template.getId(), template);
        }

        @Override
        public int update(AlarmTemplate template) {
            updatedCount++;
            return 1;
        }

        @Override
        public int markDeleted(Long id) {
            markedDeletedCount++;
            return 1;
        }

        @Override
        public AlarmTemplate selectById(Long id) {
            return templates.get(id);
        }

        @Override
        public AlarmTemplate selectByIdForUpdate(Long id) {
            lockedIds.add(id);
            lockLog.add("header:" + id);
            return templates.get(id);
        }

        @Override
        public List<AlarmTemplate> selectByIds(List<Long> ids) {
            return ids.stream()
                    .map(templates::get)
                    .filter(Objects::nonNull)
                    .toList();
        }

        @Override
        public List<AlarmTemplate> selectByService(String serviceName) {
            return List.of();
        }

        @Override
        public List<AlarmTemplate> selectByServiceAndDataSource(String serviceName,
                                                                String dataSource) {
            return List.of();
        }
    }

    static class RecordingTemplateItemDao implements AlarmTemplateItemDao {
        final Map<Long, AlarmTemplateItem> items = new LinkedHashMap<>();
        final Map<Long, Integer> ruleCounts = new HashMap<>();
        final List<AlarmTemplateItem> insertedItems = new ArrayList<>();
        final List<Long> updatedItemIds = new ArrayList<>();
        final List<Long> markedDeletedIds = new ArrayList<>();
        final List<Long> markedDeletedTemplateIds = new ArrayList<>();
        long nextId = 100L;

        RecordingTemplateItemDao() {
            this(null);
        }

        RecordingTemplateItemDao(AlarmTemplateItem item) {
            if (item != null) {
                items.put(item.getId(), item);
            }
        }

        void addItem(AlarmTemplateItem item) {
            items.put(item.getId(), item);
        }

        @Override
        public void insert(AlarmTemplateItem item) {
            item.setId(nextId++);
            items.put(item.getId(), item);
            insertedItems.add(item);
        }

        @Override
        public int update(AlarmTemplateItem item) {
            updatedItemIds.add(item.getId());
            items.put(item.getId(), item);
            return 1;
        }

        @Override
        public int markDeleted(Long id) {
            markedDeletedIds.add(id);
            items.remove(id);
            return 1;
        }

        @Override
        public int markDeletedByTemplateId(Long templateId) {
            markedDeletedTemplateIds.add(templateId);
            items.values().removeIf(item -> templateId.equals(item.getTemplateId()));
            return 1;
        }

        @Override
        public AlarmTemplateItem selectById(Long id) {
            return items.get(id);
        }

        @Override
        public AlarmTemplateItem selectByIdForUpdate(Long id) {
            return items.get(id);
        }

        @Override
        public List<AlarmTemplateItem> selectByIds(List<Long> ids) {
            return ids.stream()
                    .map(items::get)
                    .filter(Objects::nonNull)
                    .toList();
        }

        @Override
        public List<AlarmTemplateItem> selectByTemplateId(Long templateId) {
            return items.values().stream()
                    .filter(item -> templateId.equals(item.getTemplateId()))
                    .toList();
        }

        @Override
        public List<AlarmTemplateItem> selectByTemplateIds(List<Long> templateIds) {
            return items.values().stream()
                    .filter(item -> templateIds.contains(item.getTemplateId()))
                    .toList();
        }

        @Override
        public int countRulesByTemplateItemId(Long templateItemId) {
            return ruleCounts.getOrDefault(templateItemId, 0);
        }
    }

    static class RecordingLocalConfigDao implements AlarmRuleLocalConfigDao {
        final List<AlarmRuleLocalConfig> upserted = new ArrayList<>();
        final List<Long> deletedRuleIds = new ArrayList<>();

        @Override
        public void upsert(AlarmRuleLocalConfig config) {
            upserted.add(config);
        }

        @Override
        public void delete(Long ruleId) {
            deletedRuleIds.add(ruleId);
        }

        @Override
        public void deleteByRuleIds(List<Long> ruleIds) {
        }

        @Override
        public AlarmRuleLocalConfig selectByRuleId(Long ruleId) {
            return null;
        }

        @Override
        public List<AlarmRuleLocalConfig> selectByRuleIds(List<Long> ruleIds) {
            return List.of();
        }
    }

    static class RecordingChannelDao implements AlarmNotificationChannelDao {
        final boolean exists;
        final int affectedRuleCount;
        final int enabledAffectedRuleCount;
        final String serviceName;
        int templateCount;
        int insertedCount;
        int lockedCount;
        int updatedCount;
        AlarmNotificationChannel channel;
        List<AlarmNotificationChannel> channels = List.of();
        List<String> userGroupIds = List.of();
        List<Long> selectedIdsWithUsage = List.of();
        int selectedByIdCount;
        final List<Long> deletedIds = new ArrayList<>();
        final List<String> deletedChannelUserGroupIds = new ArrayList<>();

        RecordingChannelDao(int affectedRuleCount, int enabledAffectedRuleCount) {
            this(true, affectedRuleCount, enabledAffectedRuleCount, SERVICE_NAME);
        }

        RecordingChannelDao(int affectedRuleCount, int enabledAffectedRuleCount, String serviceName) {
            this(true, affectedRuleCount, enabledAffectedRuleCount, serviceName);
        }

        RecordingChannelDao(boolean exists, int affectedRuleCount, int enabledAffectedRuleCount) {
            this(exists, affectedRuleCount, enabledAffectedRuleCount, SERVICE_NAME);
        }

        RecordingChannelDao(boolean exists, int affectedRuleCount, int enabledAffectedRuleCount, String serviceName) {
            this.exists = exists;
            this.affectedRuleCount = affectedRuleCount;
            this.enabledAffectedRuleCount = enabledAffectedRuleCount;
            this.serviceName = serviceName;
        }

        RecordingChannelDao withTemplateCount(int templateCount) {
            this.templateCount = templateCount;
            return this;
        }

        void setChannels(List<AlarmNotificationChannel> channels) {
            this.channels = channels;
        }

        void setUserGroupIds(String... userGroupIds) {
            this.userGroupIds = List.of(userGroupIds);
        }

        void setChannel(AlarmNotificationChannel channel) {
            this.channel = channel;
        }

        @Override
        public void insert(AlarmNotificationChannel channel) {
            insertedCount++;
        }

        @Override
        public void update(AlarmNotificationChannel channel) {
            updatedCount++;
        }

        @Override
        public void delete(Long id) {
            deletedIds.add(id);
        }

        @Override
        public AlarmNotificationChannel selectById(Long id) {
            selectedByIdCount++;
            if (channel != null) {
                return channel;
            }
            return channel(id);
        }

        @Override
        public AlarmNotificationChannel selectByIdForUpdate(Long id) {
            lockedCount++;
            if (channel != null) {
                return channel;
            }
            return channel(id);
        }

        AlarmNotificationChannel channel(Long id) {
            if (!exists) {
                return null;
            }
            AlarmNotificationChannel channel = new AlarmNotificationChannel();
            channel.setId(id);
            channel.setServiceName(serviceName);
            channel.setTemplateCount(templateCount);
            channel.setAffectedRuleCount(affectedRuleCount);
            channel.setEnabledAffectedRuleCount(enabledAffectedRuleCount);
            return channel;
        }

        @Override
        public List<AlarmNotificationChannel> selectByIds(List<Long> ids) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<AlarmNotificationChannel> selectByIdsWithUsage(List<Long> ids) {
            selectedIdsWithUsage = List.copyOf(ids);
            return channels.stream()
                    .filter(channel -> ids.contains(channel.getId()))
                    .toList();
        }

        @Override
        public List<AlarmNotificationChannel> selectByRuleId(Long ruleId) {
            return filterExistingUserGroupChannels(channels);
        }

        @Override
        public List<AlarmNotificationChannel> selectAll() {
            return filterExistingUserGroupChannels(channels);
        }

        @Override
        public List<AlarmNotificationChannel> selectByService(String serviceName) {
            return filterExistingUserGroupChannels(channels);
        }

        @Override
        public List<AlarmNotificationChannel> selectByServiceName(String serviceName) {
            return filterExistingUserGroupChannels(channels);
        }

        @Override
        public boolean existsUserGroup(String userGroupId) {
            return userGroupIds.contains(userGroupId);
        }

        @Override
        public List<AlarmNotificationChannel> selectByUserGroupDestinationForUpdate(String userGroupId) {
            return channels.stream()
                    .filter(RecordingChannelDao::isUserGroupDestination)
                    .filter(channel -> userGroupId.equals(channel.getDestination()))
                    .toList();
        }

        @Override
        public void deleteByUserGroupDestination(String userGroupId) {
            deletedChannelUserGroupIds.add(userGroupId);
        }

        List<AlarmNotificationChannel> filterExistingUserGroupChannels(List<AlarmNotificationChannel> channels) {
            return channels.stream()
                    .filter(channel -> !isUserGroupDestination(channel) || existsUserGroup(channel.getDestination()))
                    .toList();
        }

        static boolean isUserGroupDestination(AlarmNotificationChannel channel) {
            return AlarmMethodType.EMAIL == channel.getMethodType() || AlarmMethodType.SMS == channel.getMethodType();
        }
    }

    /**
     * An existence checker reading the same fixture fields as the recording DAO, so a
     * test controls whether the target application exists the way it controls the rest
     * of the fixture. It claims one type, leaving the others unclaimed so the tests can
     * also exercise a type no checker owns.
     */
    static AlarmApplicationExistenceChecker existenceChecker(RecordingRuleDao ruleDao) {
        return new AlarmApplicationExistenceChecker() {
            @Override
            public Set<String> supportedTypes() {
                return Set.of(AlarmApplication.TYPE_JAVASCRIPT);
            }

            @Override
            public boolean exists(AlarmApplication application) {
                return ruleDao.existsApplication(
                        application.getServiceName(), application.getApplicationName());
            }
        };
    }

    static class RecordingRuleDao implements AlarmRuleV2Dao {
        final boolean exists;
        final boolean targetApplicationExists;
        final List<Long> lockedIds = new ArrayList<>();
        List<String> lockLog = new ArrayList<>();
        final List<Long> deletedIds = new ArrayList<>();
        final List<String> resolvedApplications = new ArrayList<>();
        List<String> deletionLog = new ArrayList<>();
        final List<AlarmRuleV2> insertedRules = new ArrayList<>();
        final List<AlarmRuleV2> updatedRules = new ArrayList<>();
        final List<Long> updatedEnabledIds = new ArrayList<>();
        final List<Boolean> updatedEnabledValues = new ArrayList<>();
        final List<Long> selectedDetailsIds = new ArrayList<>();
        final Map<String, List<Long>> appliedBundleRuleIds = new HashMap<>();
        Long templateItemId;
        AlarmRuleDetails ruleDetails;
        String targetApplicationServiceName = SERVICE_NAME;
        long nextInsertedRuleId = 11L;

        RecordingRuleDao(boolean exists) {
            this(exists, true);
        }

        RecordingRuleDao(boolean exists, boolean targetApplicationExists) {
            this.exists = exists;
            this.targetApplicationExists = targetApplicationExists;
        }

        RecordingRuleDao withLockLog(List<String> lockLog) {
            this.lockLog = lockLog;
            return this;
        }

        RecordingRuleDao withDeletionLog(List<String> deletionLog) {
            this.deletionLog = deletionLog;
            return this;
        }

        RecordingRuleDao withTemplateItemId(Long templateItemId) {
            this.templateItemId = templateItemId;
            return this;
        }

        RecordingRuleDao withTargetApplicationServiceName(String serviceName) {
            this.targetApplicationServiceName = serviceName;
            return this;
        }

        RecordingRuleDao withRuleDetails(AlarmRuleDetails ruleDetails) {
            this.ruleDetails = ruleDetails;
            return this;
        }

        @Override
        public List<AlarmApplication> selectAppliedApplicationsForUpdate(Long templateId) {
            return appliedBundleRuleIds.keySet().stream()
                    .filter(key -> key.startsWith(templateId + ":"))
                    .map(key -> key.substring(key.indexOf(':') + 1))
                    .distinct()
                    .sorted()
                    .map(name -> new AlarmApplication(
                            SERVICE_NAME, name, AlarmApplication.TYPE_JAVASCRIPT))
                    .toList();
        }

        RecordingRuleDao withAppliedBundleRule(Long templateId, String applicationName, Long ruleId) {
            appliedBundleRuleIds
                    .computeIfAbsent(templateId + ":" + applicationName, key -> new ArrayList<>())
                    .add(ruleId);
            return this;
        }

        @Override
        public void insertRule(AlarmRuleV2 rule) {
            rule.setId(nextInsertedRuleId++);
            insertedRules.add(rule);
        }

        @Override
        public int updateRule(AlarmRuleV2 rule) {
            updatedRules.add(rule);
            return 1;
        }

        @Override
        public void updateEnabled(Long id, boolean enabled) {
            updatedEnabledIds.add(id);
            updatedEnabledValues.add(enabled);
        }

        @Override
        public void deleteRule(Long id) {
            deletedIds.add(id);
            deletionLog.add("rule");
        }

        @Override
        public List<Long> selectRuleIdsByApplication(
                String serviceName, String applicationName, String applicationType) {
            resolvedApplications.add(applicationName);
            return APPLICATION_RULE_IDS;
        }

        @Override
        public void deleteByIds(List<Long> ruleIds) {
            deletedIds.addAll(ruleIds);
            deletionLog.add("rule");
        }

        @Override
        public AlarmRuleV2 selectRuleById(Long id) {
            return rule(id);
        }

        @Override
        public AlarmRuleDetails selectRuleDetailsById(Long id) {
            selectedDetailsIds.add(id);
            return ruleDetails;
        }

        @Override
        public AlarmRuleV2 selectRuleByIdForUpdate(Long id) {
            lockedIds.add(id);
            lockLog.add("rule:" + id);
            return rule(id);
        }

        @Override
        public List<AlarmRuleV2> selectRulesByTemplateIdAndApplicationForUpdate(
                Long templateId, String serviceName, String applicationName, String applicationType) {
            return appliedBundleRuleIds.getOrDefault(templateId + ":" + applicationName, List.of()).stream()
                    .map(this::rule)
                    .toList();
        }

        AlarmRuleV2 rule(Long id) {
            if (!exists) {
                return null;
            }
            AlarmRuleV2 rule = new AlarmRuleV2();
            rule.setId(id);
            rule.setApplicationType(AlarmApplication.TYPE_JAVASCRIPT);
            rule.setServiceName(SERVICE_NAME);
            rule.setApplicationName(APPLICATION_NAME);
            rule.setTemplateItemId(templateItemId);
            return rule;
        }

        @Override
        public List<AlarmRuleV2> selectEnabledRules() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<AlarmRuleV2> selectEnabledRulesAfter(long afterId, int limit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<AlarmRuleV2> selectDueEnabledRulesAfter(long afterId, int limit, LocalDateTime now) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<AlarmRuleV2> selectRulesByApplication(
                String serviceName, String applicationName, String applicationType) {
            throw new UnsupportedOperationException();
        }

        boolean existsApplication(String serviceName, String applicationName) {
            return targetApplicationExists
                    && targetApplicationServiceName.equals(serviceName)
                    && APPLICATION_NAME.equals(applicationName);
        }
    }

    static class RecordingChannelBindingDao implements AlarmChannelBindingDao {
        List<String> deletionLog = new ArrayList<>();
        final List<AlarmChannelBinding> inserted = new ArrayList<>();

        RecordingChannelBindingDao withDeletionLog(List<String> deletionLog) {
            this.deletionLog = deletionLog;
            return this;
        }

        final List<Long> deletedOwnerIds = new ArrayList<>();
        final List<Long> deletedChannelIds = new ArrayList<>();
        final List<String> deletedUserGroupDestinations = new ArrayList<>();

        @Override
        public void insert(AlarmChannelBinding binding) {
            inserted.add(binding);
        }

        @Override
        public void delete(AlarmChannelOwnerType ownerType, Long ownerId, Long channelId) {
            deletedOwnerIds.add(ownerId);
            deletedChannelIds.add(channelId);
        }

        @Override
        public void deleteByOwner(AlarmChannelOwnerType ownerType, Long ownerId) {
            deletedOwnerIds.add(ownerId);
            deletionLog.add("binding");
        }

        @Override
        public void deleteByChannelId(Long channelId) {
            deletedChannelIds.add(channelId);
            inserted.removeIf(binding -> binding.getChannelId().equals(channelId));
        }

        @Override
        public void deleteRuleBindingsByRuleIds(List<Long> ruleIds) {
            deletedOwnerIds.addAll(ruleIds);
            deletionLog.add("binding");
        }

        @Override
        public void deleteByUserGroupDestination(String userGroupId) {
            deletedUserGroupDestinations.add(userGroupId);
        }

        @Override
        public boolean existsByChannelId(Long channelId) {
            return inserted.stream().anyMatch(binding -> binding.getChannelId().equals(channelId));
        }

        @Override
        public List<AlarmChannelBinding> selectByOwner(AlarmChannelOwnerType ownerType, Long ownerId) {
            return inserted.stream()
                    .filter(binding -> binding.getOwnerType() == ownerType && binding.getOwnerId().equals(ownerId))
                    .toList();
        }
    }
}
