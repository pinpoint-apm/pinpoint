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

import com.navercorp.pinpoint.alarm.dao.AlarmChannelBindingDao;
import com.navercorp.pinpoint.alarm.dao.AlarmHistoryV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationOutboxDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleLocalConfigDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmStateDao;
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.vo.AlarmChannelOwnerType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Removes a rule together with everything that points at it.
 * <p>
 * A rule is deleted from three places -- one rule, a bundle unapplied from an
 * application, an application removed -- and all three have to visit the same six tables
 * in the same order as the cleanup batch ({@code AlarmTemplateCleanupTasklet}), or the
 * two sides take locks on the same rows in opposite order.
 */
@Component
class AlarmRuleDeleter {

    private final AlarmRuleV2Dao ruleDao;
    private final AlarmRuleLocalConfigDao localConfigDao;
    private final AlarmChannelBindingDao channelBindingDao;
    private final AlarmHistoryV2Dao historyDao;
    private final AlarmNotificationOutboxDao outboxDao;
    private final AlarmStateDao stateDao;

    AlarmRuleDeleter(AlarmRuleV2Dao ruleDao,
                     AlarmRuleLocalConfigDao localConfigDao,
                     AlarmChannelBindingDao channelBindingDao,
                     AlarmHistoryV2Dao historyDao,
                     AlarmNotificationOutboxDao outboxDao,
                     AlarmStateDao stateDao) {
        this.ruleDao = Objects.requireNonNull(ruleDao, "ruleDao");
        this.localConfigDao = Objects.requireNonNull(localConfigDao, "localConfigDao");
        this.channelBindingDao = Objects.requireNonNull(channelBindingDao, "channelBindingDao");
        this.historyDao = Objects.requireNonNull(historyDao, "historyDao");
        this.outboxDao = Objects.requireNonNull(outboxDao, "outboxDao");
        this.stateDao = Objects.requireNonNull(stateDao, "stateDao");
    }

    void deleteRule(Long id) {
        outboxDao.deleteByRuleId(id);
        historyDao.deleteByRuleId(id);
        channelBindingDao.deleteByOwner(AlarmChannelOwnerType.RULE, id);
        localConfigDao.delete(id);
        stateDao.deleteByRuleId(id);
        ruleDao.deleteRule(id);
    }

    /**
     * Deletes the rules of one application, by primary key.
     * <p>
     * The application columns carry no index, so every delete driven by them would scan
     * alarm_rule_v2 and hold a next-key lock on the whole table until commit -- blocking
     * rule writes in unrelated services. Resolving the ids once without a lock keeps all
     * six deletes on primary keys.
     */
    void deleteRulesByApplication(AlarmApplication application) {
        List<Long> ruleIds = ruleDao.selectRuleIdsByApplication(application.getServiceName(),
                application.getApplicationName(), application.getApplicationType());
        if (ruleIds.isEmpty()) {
            return;
        }
        outboxDao.deleteByRuleIds(ruleIds);
        historyDao.deleteByRuleIds(ruleIds);
        channelBindingDao.deleteRuleBindingsByRuleIds(ruleIds);
        localConfigDao.deleteByRuleIds(ruleIds);
        stateDao.deleteByRuleIds(ruleIds);
        ruleDao.deleteByIds(ruleIds);
    }
}
