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

import com.navercorp.pinpoint.alarm.dao.AlarmNotificationChannelDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateDao;
import com.navercorp.pinpoint.alarm.service.AlarmRuleDeleter;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.service.service.ServiceDeletionListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Takes every lock before it deletes anything, in the order the other writers use:
 * bundle headers (see {@link AlarmBundleLocks}), then rules, then channels. Deleting
 * the rules first would take their channel bindings before the channel rows.
 * It runs before the other listeners, so their rows are locked after the alarm rows.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AlarmServiceDeletionListener implements ServiceDeletionListener {
    private final AlarmTemplateDao templateDao;
    private final AlarmRuleV2Dao ruleDao;
    private final AlarmRuleDeleter ruleDeleter;
    private final AlarmNotificationChannelDao channelDao;
    private final AlarmTemplateService templateService;
    private final AlarmChannelService channelService;

    public AlarmServiceDeletionListener(AlarmTemplateDao templateDao,
                                        AlarmRuleV2Dao ruleDao,
                                        AlarmRuleDeleter ruleDeleter,
                                        AlarmNotificationChannelDao channelDao,
                                        AlarmTemplateService templateService,
                                        AlarmChannelService channelService) {
        this.templateDao = Objects.requireNonNull(templateDao, "templateDao");
        this.ruleDao = Objects.requireNonNull(ruleDao, "ruleDao");
        this.ruleDeleter = Objects.requireNonNull(ruleDeleter, "ruleDeleter");
        this.channelDao = Objects.requireNonNull(channelDao, "channelDao");
        this.templateService = Objects.requireNonNull(templateService, "templateService");
        this.channelService = Objects.requireNonNull(channelService, "channelService");
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void onServiceDelete(Service service) {
        String serviceName = service.getServiceName();
        List<Long> templateIds = new ArrayList<>();
        for (Long templateId : templateDao.selectByService(serviceName).stream().map(AlarmTemplate::getId).sorted().toList()) {
            // A concurrent deleteTemplate may have soft-deleted it since the unlocked read.
            if (templateDao.selectByIdForUpdate(templateId) != null) {
                templateIds.add(templateId);
            }
        }
        List<Long> ruleIds = ruleDao.selectRuleIdsByServiceForUpdate(serviceName);
        List<Long> channelIds = channelDao.selectIdsByServiceNameForUpdate(serviceName);

        ruleDeleter.deleteRules(ruleIds);
        templateIds.forEach(templateService::markTemplateDeleted);
        channelIds.forEach(channelService::removeChannel);
    }
}
