package com.navercorp.pinpoint.alarm.web;

import com.navercorp.pinpoint.alarm.validation.AlarmValidationConstants;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationChannel;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

/**
 * Checks that a resource the caller named actually belongs to the service and
 * application the request is scoped to.
 * <p>
 * A mismatch answers 404, not 403. Telling a caller "this exists but is not yours"
 * lets them walk ids to learn what other services own, and the caller cannot act on
 * the resource either way, so the two cases are indistinguishable on purpose.
 */
final class AlarmOwnerships {

    private AlarmOwnerships() {
    }

    /** The scope every alarm request is bound to, taken from the {@code pServiceName} header. */
    static void verifyServiceName(String serviceName) {
        if (!com.navercorp.pinpoint.common.util.StringUtils.hasText(serviceName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "serviceName is required");
        }
        if (serviceName.length() > AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "serviceName is too long");
        }
    }

    static AlarmRuleV2 requireRule(AlarmRuleV2 rule, Long ruleId) {
        if (rule == null) {
            throw new AlarmResourceNotFoundException("Rule not found: " + ruleId);
        }
        return rule;
    }

    static AlarmNotificationChannel requireChannel(AlarmNotificationChannel channel, Long channelId) {
        if (channel == null) {
            throw new AlarmResourceNotFoundException("Channel not found: " + channelId);
        }
        return channel;
    }

    static void verifyRule(AlarmRuleV2 rule, String serviceName, String applicationName) {
        if (!AlarmServiceNames.isSame(rule.getServiceName(), serviceName)
                || !Objects.equals(rule.getApplicationName(), applicationName)) {
            throw new AlarmResourceNotFoundException("Rule not found");
        }
    }

    static void verifyRule(AlarmRuleV2 rule, AlarmApplication application) {
        if (!Objects.equals(rule.getServiceName(), application.getServiceName())
                || !Objects.equals(rule.getApplicationName(), application.getApplicationName())
                || !Objects.equals(rule.getApplicationType(), application.getApplicationType())) {
            throw new AlarmResourceNotFoundException("Rule not found");
        }
    }

    static void verifyTemplate(AlarmTemplate template, String serviceName) {
        if (!AlarmServiceNames.isSame(template.getServiceName(), serviceName)) {
            throw new AlarmResourceNotFoundException("Template not found");
        }
    }

    static void verifyChannel(AlarmNotificationChannel channel, String serviceName) {
        if (!AlarmServiceNames.isSame(channel.getServiceName(), serviceName)) {
            throw new AlarmResourceNotFoundException("Channel not found");
        }
    }
}
