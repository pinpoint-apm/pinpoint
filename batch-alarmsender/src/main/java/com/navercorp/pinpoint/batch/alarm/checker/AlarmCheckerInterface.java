package com.navercorp.pinpoint.batch.alarm.checker;

import com.navercorp.pinpoint.batch.alarm.vo.sender.payload.CheckerDetectedValue;
import com.navercorp.pinpoint.web.vo.RuleInterface;

import java.util.List;

public interface AlarmCheckerInterface {
    boolean isDetected();
    boolean isSMSSend();
    boolean isEmailSend();
    boolean isWebhookSend();
    String getUserGroupId();
    String getUnit();
    String getRuleId();
    void check();
    RuleInterface getRule();
    String getEmailMessage(String pinpointUrl, String applicationId, String serviceType, String currentTime);
    List<String> getSmsMessage();
    String getCheckerType();
    CheckerDetectedValue getCheckerDetectedValue();

}
