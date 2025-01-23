/*
 * Copyright 2019 NAVER Corp.
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

import com.navercorp.pinpoint.batch.alarm.collector.pinot.SystemCpuDataCollector;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */
@ExtendWith({MockitoExtension.class})
public class SystemCpuUsageRateCheckerTest {


    private static final String SERVICE_NAME = "local_service";
    private static final String SERVICE_TYPE = "tomcat";

    @Mock
    SystemCpuDataCollector systemCpuDataCollector;

    @Test
    public void checkTest() {
        Rule rule = new Rule(SERVICE_NAME, SERVICE_TYPE, CheckerCategory.SYSTEM_CPU_USAGE_RATE.getName(), 10, "testGroup", false, false, false, "");

        Map<String, Long> systemCpuUsageRate = Map.ofEntries(
            Map.entry("local_tomcat01", 21L),
            Map.entry("local_tomcat02", 22L),
            Map.entry("local_tomcat03", 23L)
        );
        when(systemCpuDataCollector.getSystemCpuUsageRate()).thenReturn(systemCpuUsageRate);

        SystemCpuUsageRateChecker systemCpuUsageRateChecker = new SystemCpuUsageRateChecker(systemCpuDataCollector, rule);
        systemCpuUsageRateChecker.check();
        assertTrue(systemCpuUsageRateChecker.isDetected());
    }

    @Test
    public void check2Test() {
        Rule rule = new Rule(SERVICE_NAME, SERVICE_TYPE, CheckerCategory.SYSTEM_CPU_USAGE_RATE.getName(), 70, "testGroup", false, false, false, "");

        Map<String, Long> systemCpuUsageRate = Map.ofEntries(
            Map.entry("local_tomcat01", 30L),
            Map.entry("local_tomcat02", 50L),
            Map.entry("local_tomcat03", 40L)
        );
        when(systemCpuDataCollector.getSystemCpuUsageRate()).thenReturn(systemCpuUsageRate);

        SystemCpuUsageRateChecker systemCpuUsageRateChecker = new SystemCpuUsageRateChecker(systemCpuDataCollector, rule);
        systemCpuUsageRateChecker.check();
        assertFalse(systemCpuUsageRateChecker.isDetected());
    }
}