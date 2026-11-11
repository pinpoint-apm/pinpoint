package com.navercorp.pinpoint.collector.heatmap.scheduler;

import com.navercorp.pinpoint.collector.heatmap.counter.HeatmapCounter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HeatmapFlusherTest {

    private static final Duration INTERVAL = Duration.ofSeconds(5);
    private static final Duration GENEROUS_TIMEOUT = Duration.ofMinutes(1);

    @Mock
    private TaskScheduler scheduler;

    @Test
    void flushSendsAggregatedCountPerKeyAndClears() {
        HeatmapCounter<String> counter = new HeatmapCounter<>();
        counter.increment("a");
        counter.increment("a");
        counter.increment("b");
        Map<String, Long> sent = new HashMap<>();
        HeatmapFlusher<String> flusher = new HeatmapFlusher<>(counter, (key, count) -> {
            sent.put(key, count);
            return done();
        }, scheduler, INTERVAL, GENEROUS_TIMEOUT);

        flusher.flush();

        assertEquals(Map.of("a", 2L, "b", 1L), sent);
        assertTrue(counter.snapshotAndClear().isEmpty());
    }

    @Test
    void abortsWhenFlushTimeoutExceeded() {
        HeatmapCounter<String> counter = newCounter(5);
        AtomicInteger calls = new AtomicInteger();
        HeatmapFlusher<String> flusher = new HeatmapFlusher<>(counter, (key, count) -> {
            calls.incrementAndGet();
            sleep(20);
            return done();
        }, scheduler, INTERVAL, Duration.ofMillis(1));

        flusher.flush();

        assertEquals(1, calls.get());
        assertTrue(counter.snapshotAndClear().isEmpty());
    }

    @Test
    void failingSinkDoesNotStopFlushWithinBudget() {
        HeatmapCounter<String> counter = newCounter(5);
        AtomicInteger calls = new AtomicInteger();
        HeatmapFlusher<String> flusher = new HeatmapFlusher<>(counter, (key, count) -> {
            calls.incrementAndGet();
            throw new RuntimeException("send failed");
        }, scheduler, INTERVAL, GENEROUS_TIMEOUT);

        flusher.flush();

        assertEquals(5, calls.get());
    }

    @Test
    void asyncFailureDoesNotStopFlush() {
        HeatmapCounter<String> counter = newCounter(5);
        AtomicInteger calls = new AtomicInteger();
        HeatmapFlusher<String> flusher = new HeatmapFlusher<>(counter, (key, count) -> {
            calls.incrementAndGet();
            return CompletableFuture.failedFuture(new RuntimeException("delivery timeout"));
        }, scheduler, INTERVAL, GENEROUS_TIMEOUT);

        flusher.flush();

        assertEquals(5, calls.get());
    }

    @Test
    void startSchedulesTheFlushAndStopSendsTheRest() {
        HeatmapCounter<String> counter = new HeatmapCounter<>();
        Map<String, Long> sent = new HashMap<>();
        HeatmapFlusher<String> flusher = new HeatmapFlusher<>(counter, (key, count) -> {
            sent.put(key, count);
            return done();
        }, scheduler, INTERVAL, GENEROUS_TIMEOUT);
        ScheduledFuture<?> flushTask = mock(ScheduledFuture.class);
        doReturn(flushTask).when(scheduler).scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), eq(INTERVAL));

        // not scheduled by the constructor
        verify(scheduler, never()).scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class));
        assertFalse(flusher.isRunning());
        assertTrue(flusher.isAutoStartup());

        flusher.start();
        assertTrue(flusher.isRunning());
        verify(scheduler).scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), eq(INTERVAL));

        counter.increment("a");
        flusher.stop();

        assertFalse(flusher.isRunning());
        verify(flushTask).cancel(false);
        assertEquals(Map.of("a", 1L), sent);
    }

    @Test
    void stopsAfterTheDefaultPhaseAndBeforeTheKafkaProducer() {
        HeatmapFlusher<String> flusher = new HeatmapFlusher<>(new HeatmapCounter<>(), (key, count) -> done(),
                scheduler, INTERVAL, GENEROUS_TIMEOUT);

        // a higher phase stops first: receivers (DEFAULT_PHASE) > flusher > DefaultKafkaProducerFactory (MIN_VALUE)
        assertTrue(flusher.getPhase() < SmartLifecycle.DEFAULT_PHASE);
        assertTrue(flusher.getPhase() > Integer.MIN_VALUE);
    }

    private static CompletableFuture<?> done() {
        return CompletableFuture.completedFuture(null);
    }

    private static HeatmapCounter<String> newCounter(int keys) {
        HeatmapCounter<String> counter = new HeatmapCounter<>();
        for (int i = 0; i < keys; i++) {
            counter.increment("key" + i);
        }
        return counter;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
