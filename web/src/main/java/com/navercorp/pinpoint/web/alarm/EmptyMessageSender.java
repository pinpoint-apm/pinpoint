package com.navercorp.pinpoint.web.alarm;

import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;

/**
 * @author minwoo.jung
 */
public class EmptyMessageSender implements AlarmMessageSender {

    @Override
    public void sendSms(AlarmChecker checker) {
    }

    @Override
    public void sendEmail(AlarmChecker checker) {
    }

}
