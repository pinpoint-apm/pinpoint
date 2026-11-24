package com.navercorp.pinpoint.web.webhook.facade;

import com.navercorp.pinpoint.web.alarm.AlarmRuleMembershipValidator;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.dao.AlarmDao;
import com.navercorp.pinpoint.web.webhook.service.WebhookAlarmService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class WebhookAlarmServiceFacadeImplTest {

    private final WebhookAlarmService webhookAlarmService = mock(WebhookAlarmService.class);
    private final AlarmDao alarmDao = mock(AlarmDao.class);
    private final AlarmRuleMembershipValidator ruleMembershipValidator = mock(AlarmRuleMembershipValidator.class);
    private final WebhookAlarmServiceFacadeImpl facade =
            new WebhookAlarmServiceFacadeImpl(webhookAlarmService, alarmDao, ruleMembershipValidator);

    @Test
    void rejectedInsertDoesNotWrite() {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(ruleMembershipValidator).validateInsert(any());

        assertThatThrownBy(() -> facade.insertRuleWithWebhooks(rule(null), List.of("1")))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(alarmDao, webhookAlarmService);
    }

    @Test
    void rejectedUpdateDoesNotWrite() {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST)).when(ruleMembershipValidator).validateUpdate(any());

        assertThatThrownBy(() -> facade.updateRuleWithWebhooks(rule("1"), List.of("1")))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(alarmDao, webhookAlarmService);
    }

    private static Rule rule(String ruleId) {
        Rule rule = new Rule("app", "SPRING_BOOT", "SLOW COUNT", 1, "team-a", false, true, true, "");
        rule.setRuleId(ruleId);
        return rule;
    }
}
