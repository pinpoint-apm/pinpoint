/*
 * Copyright 2025 NAVER Corp.
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author minwoo-jung
 */
class HeatmapStatTest {


    @Test
    public void elapsedTimeTest() {
        HeatmapStat heatmapStat = new HeatmapStat("applicationName", "agentId", 1000, 1000, 0);
        assertEquals(1000, heatmapStat.getElapsedTime());

        HeatmapStat heatmapStat2 = new HeatmapStat("applicationName", "agentId", 1000, 200, 0);
        assertEquals(200, heatmapStat2.getElapsedTime());

        HeatmapStat heatmapStat3 = new HeatmapStat("applicationName", "agentId", 1000, 201, 0);
        assertEquals(400, heatmapStat3.getElapsedTime());

        HeatmapStat heatmapStat4 = new HeatmapStat("applicationName", "agentId", 1000, 199, 0);
        assertEquals(200, heatmapStat4.getElapsedTime());

        HeatmapStat heatmapStat5 = new HeatmapStat("applicationName", "agentId", 1000, 100, 0);
        assertEquals(200, heatmapStat5.getElapsedTime());

        HeatmapStat heatmapStat6 = new HeatmapStat("applicationName", "agentId", 1000, 300, 0);
        assertEquals(400, heatmapStat6.getElapsedTime());

        HeatmapStat heatmapStat7 = new HeatmapStat("applicationName", "agentId", 1000, 399, 0);
        assertEquals(400, heatmapStat7.getElapsedTime());

        HeatmapStat heatmapStat8 = new HeatmapStat("applicationName", "agentId", 1000, 400, 0);
        assertEquals(400, heatmapStat8.getElapsedTime());

        HeatmapStat heatmapStat9 = new HeatmapStat("applicationName", "agentId", 1000, 401, 0);
        assertEquals(600, heatmapStat9.getElapsedTime());
    }

}