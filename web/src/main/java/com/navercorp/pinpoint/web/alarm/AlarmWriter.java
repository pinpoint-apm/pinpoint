package com.navercorp.pinpoint.web.alarm;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;

/**
 * @author minwoo.jung
 */
public class AlarmWriter implements ItemWriter<AlarmChecker> {
    
    @Autowired(required=false)
    private AlarmMessageSender alarmMessageSender = new EmptyMessageSender();
    
    @Override
    public void write(List<? extends AlarmChecker> checkers) throws Exception {
        for(AlarmChecker checker : checkers) {
            send(checker);
        }
    }
    
    private void send(AlarmChecker checker) {
        if (!checker.isDetected()) {
            return;
        }
        if (checker.isSMSSend()) {
            alarmMessageSender.sendSms(checker);
        }
        if (checker.isEmailSend()) {
            alarmMessageSender.sendEmail(checker);
        }
    }
}
