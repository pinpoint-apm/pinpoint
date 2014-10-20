package com.nhn.pinpoint.web.alarm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

import com.nhn.pinpoint.web.alarm.checker.SlowCountChecker;
import com.nhn.pinpoint.web.alarm.vo.Rule;

public class CheckerCategoryTest {

    @Test
    public void createCheckerTest() {
        CheckerCategory slowCount = CheckerCategory.getValue("slow_count");
        
        Rule rule = new Rule(null, CheckerCategory.SLOW_COUNT.getName(), 75, "testGroup", false, false, "");
        SlowCountChecker checker = (SlowCountChecker) slowCount.createChecker(null, rule);
        rule = new Rule(null, CheckerCategory.SLOW_COUNT.getName(), 63, "testGroup", false, false, "");
        SlowCountChecker checker2 = (SlowCountChecker) slowCount.createChecker(null, rule);
        
        assertNotSame(checker, checker2);
        
        assertNotNull(checker);
        assertEquals(75, (int)checker.getRule().getThreshold());
        
        assertNotNull(checker2);
        assertEquals(63, (int)checker2.getRule().getThreshold());
    }
    
}
