/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        Rule rule = new Rule("testService", "tomcat", CheckerCategory.SLOW_COUNT.getName(), 100, "testGroup", true, false, "");
        SlowCountChecker checker = new SlowCountChecker(null, rule) {
            @Override
            public boolean isDetected() {
                return true;
            }
            
            @Override
            protected Long getDetectedValue() {
                return 10000L;
            }
        };
        
        List<AlarmChecker> checkers = new LinkedList<AlarmChecker>();
        checkers.add(checker);
        writer.write(checkers);
    }

    @Ignore
    @Test
    public void emailSendTest() throws Exception {
        Rule rule = new Rule("testService", "tomcat", CheckerCategory.SLOW_COUNT.getName(), 100, "testGroup", false, true, "");
        SlowCountChecker checker = new SlowCountChecker(null, rule) {
            @Override
            public boolean isDetected() {
                return true;
            }
            
            @Override
            protected Long getDetectedValue() {
                return 10000L;
            }
        };
        
        List<AlarmChecker> checkers = new LinkedList<AlarmChecker>();
        checkers.add(checker);
        writer.write(checkers);
    }
    
}
