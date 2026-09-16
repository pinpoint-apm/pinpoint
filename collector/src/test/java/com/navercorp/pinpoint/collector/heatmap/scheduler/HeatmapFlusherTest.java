package com.navercorp.pinpoint.collector.heatmap.scheduler;

import com.navercorp.pinpoint.collector.heatmap.counter.HeatmapCounter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class HeatmapFlusherTest {

    @Mock
    private TaskScheduler scheduler;

    @Test
    void flushSendsAggregatedCountPerKeyAndClears() {
        HeatmapCounter<String> counter = new HeatmapCounter<>();
        counter.increment("a");
        counter.increment("a");
        counter.increment("b");
        Map<String, Long> sent = new HashMap<>();
        HeatmapFlusher<String> flusher = new HeatmapFlusher<>("test", counter, sent::put, scheduler, Duration.ofSeconds(5));

        flusher.flush();

        assertEquals(Map.of("a", 2L, "b", 1L), sent);
        assertTrue(counter.snapshotAndClear().isEmpty());
    }
}
