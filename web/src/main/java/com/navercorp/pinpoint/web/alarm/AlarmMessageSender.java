package com.navercorp.pinpoint.web.alarm;

import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;

/**
 * @author minwoo.jung
 */
public interface AlarmMessageSender {
    void sendSms(AlarmChecker checker);
    void sendEmail(AlarmChecker checker);
}
