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

import com.navercorp.pinpoint.batch.alarm.checker.SlowCountChecker;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

public class CheckerCategoryTest {
    private final CheckerRegistry registry = CheckerRegistry.newCheckerRegistry();

    @Test
    public void createCheckerTest() {

        CheckerCategory slowCount = CheckerCategory.getValue("slow count");
        AlarmCheckerFactory checkerFactory = registry.getCheckerFactory(slowCount);

        Rule rule = new Rule(null, "", CheckerCategory.SLOW_COUNT.getName(), 75, "testGroup", false, false, false, "");
        SlowCountChecker checker = (SlowCountChecker) checkerFactory.createChecker(null, rule);
        rule = new Rule(null, "", CheckerCategory.SLOW_COUNT.getName(), 63, "testGroup", false, false, false, "");
        SlowCountChecker checker2 = (SlowCountChecker) checkerFactory.createChecker(null, rule);
        
        assertNotSame(checker, checker2);
        
        assertNotNull(checker);
        assertEquals(75, (int)checker.getRule().getThreshold());
        
        assertNotNull(checker2);
        assertEquals(63, (int)checker2.getRule().getThreshold());
    }
    
}
