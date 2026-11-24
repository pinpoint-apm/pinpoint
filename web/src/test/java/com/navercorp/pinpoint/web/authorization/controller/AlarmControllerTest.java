package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.service.web.vo.ServiceName;
import com.navercorp.pinpoint.web.alarm.AlarmRuleMembershipValidator;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.service.AlarmService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class AlarmControllerTest {

    private final AlarmService alarmService = mock(AlarmService.class);
    private final AlarmRuleMembershipValidator ruleMembershipValidator = mock(AlarmRuleMembershipValidator.class);
    private final AlarmController controller = new AlarmController(alarmService, ruleMembershipValidator);

    @Test
    void rejectedInsertDoesNotWrite() {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(ruleMembershipValidator).validateInsert(any());

        assertThatThrownBy(() -> controller.insertRule(new ServiceName("DEFAULT"), rule(null)))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(alarmService);
    }

    @Test
    void rejectedUpdateDoesNotWrite() {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(ruleMembershipValidator).validateUpdate(any());

        assertThatThrownBy(() -> controller.updateRule(new ServiceName("DEFAULT"), rule("1")))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(alarmService);
    }

    private static Rule rule(String ruleId) {
        Rule rule = new Rule("app", "SPRING_BOOT", "SLOW COUNT", 1, "team-a", false, true, false, "");
        rule.setRuleId(ruleId);
        return rule;
    }
}
