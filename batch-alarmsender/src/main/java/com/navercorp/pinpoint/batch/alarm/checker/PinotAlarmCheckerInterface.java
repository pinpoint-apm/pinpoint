package com.navercorp.pinpoint.batch.alarm.checker;

import java.math.BigDecimal;
import java.util.List;

public interface PinotAlarmCheckerInterface<T extends Number> {
    String getTarget();
    String getRuleId(int index);
    T getCollectedValue();
    boolean isSMSSend(int index);
    boolean isEmailSend(int index);
    boolean isWebhookSend(int index);
    String getUserGroupId(int index);
    boolean[] getAlarmDetected();
    String getEmailMessage(int index);
    List<String> getSmsMessage(int index);
    String getServiceName();
    String getApplicationName();
    String getUnit();
    BigDecimal getThreshold(int index);
    String getAlarmConditionText(int index);
    String getCheckerName(int index);
    String getNotes(int index);
    String getMenuUrl();
    String getRule(int index);
}
