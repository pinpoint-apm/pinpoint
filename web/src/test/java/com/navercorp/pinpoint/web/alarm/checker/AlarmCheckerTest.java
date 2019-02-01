package com.navercorp.pinpoint.web.alarm.checker;

import com.navercorp.pinpoint.web.alarm.DataCollectorFactory;
import com.navercorp.pinpoint.web.alarm.collector.DataCollector;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AlarmCheckerTest {
    private static final String UNIT = "testUnit";
    private static final String DETECTED_VALUE = "testDetectedValue";
    private static final Rule RULE = new Rule("test service", "tomcat", "AlarmChecker", 1, "testGroup", false, false,
            "testNotes");
    ;

    @Test
    public void getSmsMessage() {
        DataCollector dataCollector = new DataCollector(DataCollectorFactory.DataCollectorCategory
                .RESPONSE_TIME) {
            @Override
            public void collect() {
                // IGNORE
            }
        };
        AlarmChecker alarmChecker = new AlarmChecker(RULE, UNIT, dataCollector) {
            @Override
            protected boolean decideResult(Object value) {
                return false;
            }

            @Override
            protected Object getDetectedValue() {
                return DETECTED_VALUE;
            }
        };
        String smsMessage = "[PINPOINT Alarm - " + RULE.getApplicationId() + "] " + RULE.getCheckerName() + " is " +
                alarmChecker.getDetectedValue() + alarmChecker.getUnit() + " Note " +
                "is " + RULE.getNotes() + " (Threshold : " + RULE.getThreshold() + alarmChecker.getUnit() + ")";
        List<String> list = new ArrayList<>();
        list.add(smsMessage);
        assertEquals(list, alarmChecker.getSmsMessage());
    }

    @Test
    public void getEmailMessage() {
        DataCollector dataCollector = new DataCollector(DataCollectorFactory.DataCollectorCategory
                .RESPONSE_TIME) {
            @Override
            public void collect() {
                // IGNORE
            }
        };
        AlarmChecker alarmChecker = new AlarmChecker(RULE, UNIT, dataCollector) {
            @Override
            protected boolean decideResult(Object value) {
                return false;
            }

            @Override
            protected Object getDetectedValue() {
                return DETECTED_VALUE;
            }
        };
        String EMAIL_MESSAGE = "[PINPOINT Alarm - " + RULE.getApplicationId() + "] " + RULE.getCheckerName() + " " +
                "value is " + alarmChecker.getDetectedValue() + alarmChecker.getUnit() + " Note is " + RULE.getNotes
                () + " during " + "the past 5 mins.(Threshold : " + RULE.getThreshold() + alarmChecker.getUnit() + ")" +
                "<br>";
        assertEquals(EMAIL_MESSAGE, alarmChecker.getEmailMessage());
    }
}