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

package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.batch.alarm.checker.SlowCountChecker;
import com.navercorp.pinpoint.batch.alarm.vo.AppAlarmChecker;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@Disabled
@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class AlarmWriterTest {

    @Autowired
    AlarmWriter writer;

    @Disabled
    @Test
    public void smsSendTest() {
        Rule rule = new Rule("testService", "tomcat", CheckerCategory.SLOW_COUNT.getName(), 100, "testGroup", true, false, false, "");
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

        List<AlarmChecker<?>> checkers = List.of(checker);
        writer.write(List.of(new AppAlarmChecker(checkers)));
    }

    @Disabled
    @Test
    public void emailSendTest() {
        Rule rule = new Rule("testService", "tomcat", CheckerCategory.SLOW_COUNT.getName(), 100, "testGroup", false, true, false, "");
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

        List<AlarmChecker<?>> checkers = List.of(checker);
        writer.write(List.of(new AppAlarmChecker(checkers)));
    }

}
