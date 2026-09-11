/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.collector.heatmap.vo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeatmapAgentStatTest {

    @Test
    public void elapsedTimeTest() {
        assertEquals(1000, new HeatmapAgentStat("svc", "app", "agent", 1000, 1000, 0).getElapsedTime());
        assertEquals(200, new HeatmapAgentStat("svc", "app", "agent", 1000, 200, 0).getElapsedTime());
        assertEquals(400, new HeatmapAgentStat("svc", "app", "agent", 1000, 201, 0).getElapsedTime());
        assertEquals(200, new HeatmapAgentStat("svc", "app", "agent", 1000, 199, 0).getElapsedTime());
        assertEquals(200, new HeatmapAgentStat("svc", "app", "agent", 1000, 100, 0).getElapsedTime());
        assertEquals(600, new HeatmapAgentStat("svc", "app", "agent", 1000, 401, 0).getElapsedTime());
    }

    @Test
    public void resultTypeTest() {
        assertEquals("SUCCESS", new HeatmapAgentStat("svc", "app", "agent", 1000, 100, 0).getResultType());
        assertEquals("FAILURE", new HeatmapAgentStat("svc", "app", "agent", 1000, 100, 1).getResultType());
        assertEquals("FAILURE", new HeatmapAgentStat("svc", "app", "agent", 1000, 100, -1).getResultType());
    }
}
