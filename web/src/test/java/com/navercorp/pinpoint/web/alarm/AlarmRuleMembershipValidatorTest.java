package com.navercorp.pinpoint.web.alarm;

import com.navercorp.pinpoint.alarm.web.service.UserGroupMembershipValidator;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.dao.AlarmDao;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlarmRuleMembershipValidatorTest {

    private final AlarmDao alarmDao = mock(AlarmDao.class);
    private final UserGroupMembershipValidator membershipValidator = mock(UserGroupMembershipValidator.class);
    private final AlarmRuleMembershipValidator validator = new AlarmRuleMembershipValidator(alarmDao, membershipValidator);

    @Test
    void newRuleWithMailIsChecked() {
        validator.validateInsert(rule(null, "app", "team-a", false, true));

        verify(membershipValidator).validate("team-a");
    }

    @Test
    void ruleWithoutMailOrSmsIsNotChecked() {
        validator.validateInsert(rule(null, "app", "team-a", false, false));

        verify(membershipValidator, never()).validate("team-a");
    }

    @Test
    void sameGroupAndMethodsAreNotChecked() {
        saved(rule("1", "app", "team-a", true, true));

        validator.validateUpdate(rule("1", "app", "team-a", true, false));

        verify(membershipValidator, never()).validate("team-a");
    }

    @Test
    void anotherGroupIsChecked() {
        saved(rule("1", "app", "team-a", false, true));

        validator.validateUpdate(rule("1", "app", "team-b", false, true));

        verify(membershipValidator).validate("team-b");
    }

    @Test
    void newlyTurnedOnSmsIsChecked() {
        saved(rule("1", "app", "team-a", false, true));

        validator.validateUpdate(rule("1", "app", "team-a", true, true));

        verify(membershipValidator).validate("team-a");
    }

    @Test
    void ruleOfAnotherApplicationIsChecked() {
        saved(rule("1", "other-app", "team-a", false, true));

        validator.validateUpdate(rule("1", "app", "team-a", false, true));

        verify(membershipValidator).validate("team-a");
    }

    @Test
    void unknownRuleIsChecked() {
        validator.validateUpdate(rule("9", "app", "team-a", false, true));

        verify(membershipValidator).validate("team-a");
    }

    private void saved(Rule rule) {
        when(alarmDao.selectRuleByRuleId(rule.getRuleId())).thenReturn(rule);
    }

    private static Rule rule(String ruleId, String applicationName, String userGroupId, boolean smsSend, boolean emailSend) {
        Rule rule = new Rule(applicationName, "SPRING_BOOT", "SLOW COUNT", 1, userGroupId, smsSend, emailSend, false, "");
        rule.setRuleId(ruleId);
        return rule;
    }
}
