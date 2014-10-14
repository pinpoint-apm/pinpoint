package com.nhn.pinpoint.web.alarm;

import java.util.LinkedList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.web.alarm.checker.AlarmChecker;
import com.nhn.pinpoint.web.alarm.checker.SlowCountFilter;
import com.nhn.pinpoint.web.alarm.vo.Rule;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class WriterTest {

    @Autowired
    AlarmWriter writer;
    
    @Ignore
    @Test
    public void smsSendTest() throws Exception {
        Rule rule = new Rule("testService", CheckerCategory.SLOW_COUNT.getName(), 100, "testGroup", true, false);
        SlowCountFilter checker = new SlowCountFilter(null, rule) {
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
        Rule rule = new Rule("testService", CheckerCategory.SLOW_COUNT.getName(), 100, "testGroup", false, true);
        SlowCountFilter checker = new SlowCountFilter(null, rule) {
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
