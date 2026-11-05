/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.alarm.core.vo;

import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoreAlarmDataSourceTest {

    private static final long FROM = LocalDateTime.of(2026, 9, 29, 17, 57, 32)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    private static final long TO = LocalDateTime.of(2026, 9, 29, 18, 17, 32)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

    // The web parses only this date format; epoch millis make it open its default range.
    @Test
    void anAgentMetricLinksToTheApplicationInspector() {
        assertEquals("/inspector/svc/app@SPRING_BOOT?from=2026-09-29-17-57-32&to=2026-09-29-18-17-32",
                CoreAlarmDataSource.AGENT_STAT.detailPath(rule(), FROM, TO));
        assertEquals("/inspector/svc/app@SPRING_BOOT?from=2026-09-29-17-57-32&to=2026-09-29-18-17-32",
                CoreAlarmDataSource.AGENT_EVENT.detailPath(rule(), FROM, TO));
    }

    @Test
    void aMapStatisticLinksToTheServiceMapOfTheRule() {
        assertEquals("/serviceMap/svc/app@SPRING_BOOT?from=2026-09-29-17-57-32&to=2026-09-29-18-17-32",
                CoreAlarmDataSource.APPLICATION_OUT_CALL.detailPath(rule(), FROM, TO));
    }

    private static AlarmRuleV2 rule() {
        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setServiceName("svc");
        rule.setApplicationName("app");
        rule.setApplicationType("SPRING_BOOT");
        return rule;
    }
}
