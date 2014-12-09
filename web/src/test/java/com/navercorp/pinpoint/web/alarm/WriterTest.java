package com.navercorp.pinpoint.web.alarm;

import java.util.LinkedList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.navercorp.pinpoint.web.alarm.AlarmWriter;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.web.alarm.checker.SlowCountChecker;
import com.navercorp.pinpoint.web.alarm.vo.Rule;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class WriterTest {

    @Autowired
    AlarmWriter writer;
    
    @Ignore
    @Test
    public void smsSendTest() throws Exception {
        Rule rule = new Rule("testService", CheckerCategory.SLOW_COUNT.getName(), 100, "testGroup", true, false, "");
        SlowCountChecker checker = new SlowCountChecker(null, rule) {
            @Override
            public boolean isDetected() {
                return true;
            }
            
            @Override
            protected long getDetectedValue() {
                return 10000;
            }
        };
        
        List<AlarmChecker> checkers = new LinkedList<AlarmChecker>();
        checkers.add(checker);
        writer.write(checkers);
    }

    @Ignore
    @Test
    public void emailSendTest() throws Exception {
        Rule rule = new Rule("testService", CheckerCategory.SLOW_COUNT.getName(), 100, "testGroup", false, true, "");
        SlowCountChecker checker = new SlowCountChecker(null, rule) {
            @Override
            public boolean isDetected() {
                return true;
            }
            
            @Override
            protected long getDetectedValue() {
                return 10000;
            }
        };
        
        List<AlarmChecker> checkers = new LinkedList<AlarmChecker>();
        checkers.add(checker);
        writer.write(checkers);
    }
    
}
