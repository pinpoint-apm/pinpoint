package com.navercorp.pinpoint.collector.heatmap.vo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeatmapStatKeyTest {

    @Test
    void elapsedTimeTest() {
        assertEquals(1000, HeatmapStatKey.of("svc", "app", "agent", 1000, 1000, 0).elapsedTime());
        assertEquals(200, HeatmapStatKey.of("svc", "app", "agent", 1000, 200, 0).elapsedTime());
        assertEquals(400, HeatmapStatKey.of("svc", "app", "agent", 1000, 201, 0).elapsedTime());
        assertEquals(200, HeatmapStatKey.of("svc", "app", "agent", 1000, 199, 0).elapsedTime());
        assertEquals(200, HeatmapStatKey.of("svc", "app", "agent", 1000, 100, 0).elapsedTime());
        assertEquals(400, HeatmapStatKey.of("svc", "app", "agent", 1000, 400, 0).elapsedTime());
        assertEquals(600, HeatmapStatKey.of("svc", "app", "agent", 1000, 401, 0).elapsedTime());
    }

    @Test
    void sortKeyTest() {
        HeatmapStatKey success = HeatmapStatKey.of("svc", "app", "agent", 1000, 100, 0);
        assertTrue(success.success());
        assertEquals("svc#app#suc", HeatmapStatRecord.of(success, 1).sortKey());

        HeatmapStatKey failure = HeatmapStatKey.of("svc", "app", "agent", 1000, 100, 1);
        assertFalse(failure.success());
        assertEquals("svc#app#fal", HeatmapStatRecord.of(failure, 1).sortKey());
    }
}
