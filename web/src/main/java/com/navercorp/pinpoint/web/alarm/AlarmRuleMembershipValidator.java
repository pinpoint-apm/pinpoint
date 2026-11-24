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

package com.navercorp.pinpoint.web.alarm;

import com.navercorp.pinpoint.alarm.web.service.UserGroupMembershipValidator;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.dao.AlarmDao;

import java.util.Objects;

public class AlarmRuleMembershipValidator {

    private final AlarmDao alarmDao;
    private final UserGroupMembershipValidator membershipValidator;

    public AlarmRuleMembershipValidator(AlarmDao alarmDao, UserGroupMembershipValidator membershipValidator) {
        this.alarmDao = Objects.requireNonNull(alarmDao, "alarmDao");
        this.membershipValidator = Objects.requireNonNull(membershipValidator, "membershipValidator");
    }

    public void validateInsert(Rule rule) {
        validate(null, rule);
    }

    public void validateUpdate(Rule rule) {
        validate(alarmDao.selectRuleByRuleId(rule.getRuleId()), rule);
    }

    private void validate(Rule before, Rule after) {
        if (reachesNewRecipients(before, after)) {
            membershipValidator.validate(after.getUserGroupId());
        }
    }

    // The update writes every column from the request, so a rule of another application is a new rule.
    static boolean reachesNewRecipients(Rule before, Rule after) {
        if (!after.isSmsSend() && !after.isEmailSend()) {
            return false;
        }
        if (before == null
                || !Objects.equals(before.getApplicationName(), after.getApplicationName())
                || !Objects.equals(before.getUserGroupId(), after.getUserGroupId())) {
            return true;
        }
        return (after.isSmsSend() && !before.isSmsSend()) || (after.isEmailSend() && !before.isEmailSend());
    }
}
