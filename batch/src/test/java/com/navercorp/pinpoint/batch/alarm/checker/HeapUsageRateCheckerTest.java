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

package com.navercorp.pinpoint.batch.alarm.checker;

import com.navercorp.pinpoint.batch.alarm.collector.pinot.HeapDataCollector;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
public class HeapUsageRateCheckerTest {

    private static final String SERVICE_NAME = "local_service";
    private static final String SERVICE_TYPE = "tomcat";

    @Mock
    HeapDataCollector heapDataCollector;

    @Test
    public void checkTest1() {
        Rule rule = new Rule(SERVICE_NAME, SERVICE_TYPE, CheckerCategory.HEAP_USAGE_RATE.getName(), 70, "testGroup", false, false, false, "");
        AgentChecker<Long> checker = new HeapUsageRateChecker(heapDataCollector, rule);

        Map<String, Long> heapUsageRateMap = Map.ofEntries(
            Map.entry("local_tomcat01", 71L),
            Map.entry("local_tomcat02", 70L),
            Map.entry("local_tomcat03", 60L)
        );

        when(heapDataCollector.getHeapUsageRate()).thenReturn(heapUsageRateMap);
        checker.check();
        assertTrue(checker.isDetected());
    }

    @Test
    public void checkTest2() {
        Rule rule = new Rule(SERVICE_NAME, SERVICE_TYPE, CheckerCategory.HEAP_USAGE_RATE.getName(), 70, "testGroup", false, false, false, "");
        AgentChecker<Long> checker = new HeapUsageRateChecker(heapDataCollector, rule);

        Map<String, Long> heapUsageRateMap = Map.ofEntries(
            Map.entry("local_tomcat01", 50L),
            Map.entry("local_tomcat02", 40L),
            Map.entry("local_tomcat03", 60L)
        );

        when(heapDataCollector.getHeapUsageRate()).thenReturn(heapUsageRateMap);

        checker.check();
        assertFalse(checker.isDetected());
    }

}
