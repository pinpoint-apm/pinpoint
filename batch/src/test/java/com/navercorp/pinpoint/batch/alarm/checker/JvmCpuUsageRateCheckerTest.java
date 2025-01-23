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

import com.navercorp.pinpoint.batch.alarm.collector.pinot.JvmCpuDataCollector;
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
public class JvmCpuUsageRateCheckerTest {

    private static final String SERVICE_NAME = "local_service";
    private static final String SERVICE_TYPE = "tomcat";

    @Mock
    JvmCpuDataCollector jvmCpuDataCollector;

    @Test
    public void checkTest1() {
        Rule rule = new Rule(SERVICE_NAME, SERVICE_TYPE, CheckerCategory.JVM_CPU_USAGE_RATE.getName(), 60, "testGroup", false, false, false, "");

        JvmCpuUsageRateChecker checker = new JvmCpuUsageRateChecker(jvmCpuDataCollector, rule);
        Map<String, Long> jvmCpuUsageRate = Map.ofEntries(
            Map.entry("local_tomcat01", 71L),
            Map.entry("local_tomcat02", 72L),
            Map.entry("local_tomcat03", 73L)
        );

        when(jvmCpuDataCollector.getJvmCpuUsageRate()).thenReturn(jvmCpuUsageRate);

        checker.check();
        assertTrue(checker.isDetected());
    }

    @Test
    public void checkTest2() {
        Rule rule = new Rule(SERVICE_NAME, SERVICE_TYPE, CheckerCategory.JVM_CPU_USAGE_RATE.getName(), 60, "testGroup", false, false, false, "");

        JvmCpuUsageRateChecker checker = new JvmCpuUsageRateChecker(jvmCpuDataCollector, rule);
        Map<String, Long> jvmCpuUsageRate = Map.ofEntries(
            Map.entry("local_tomcat01", 51L),
            Map.entry("local_tomcat02", 52L),
            Map.entry("local_tomcat03", 53L)
        );

        when(jvmCpuDataCollector.getJvmCpuUsageRate()).thenReturn(jvmCpuUsageRate);

        checker.check();
        assertFalse(checker.isDetected());
    }

}
