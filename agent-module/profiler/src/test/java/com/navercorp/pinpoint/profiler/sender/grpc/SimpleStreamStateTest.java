package com.navercorp.pinpoint.profiler.sender.grpc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SimpleStreamStateTest {

    @Test
    void reportWithNoSuccess() {
        SimpleStreamState state = new SimpleStreamState(10, 1000);

        String report = state.report();
        Assertions.assertTrue(report.contains("successCount=0"));
        Assertions.assertTrue(report.contains("tps=0.00"));
    }

    @Test
    void reportWithSuccesses() {
        SimpleStreamState state = new SimpleStreamState(10, 1000);

        for (int i = 0; i < 100; i++) {
            state.success();
        }

        String report = state.report();
        Assertions.assertTrue(report.contains("successCount=100"));
        Assertions.assertTrue(report.contains("tps="));
        Assertions.assertTrue(report.contains("elapsedMs="));
    }

    @Test
    void reportResetsCounters() {
        SimpleStreamState state = new SimpleStreamState(10, 1000);

        state.success();
        state.success();
        state.success();

        String firstReport = state.report();
        Assertions.assertTrue(firstReport.contains("successCount=3"));

        String secondReport = state.report();
        Assertions.assertTrue(secondReport.contains("successCount=0"));
        Assertions.assertTrue(secondReport.contains("tps=0.00"));
    }

    @Test
    void reportAfterFailResetsSuccess() {
        SimpleStreamState state = new SimpleStreamState(10, 1000);

        state.success();
        state.success();
        state.fail();
        // success count should still be 2 since fail() doesn't reset success counters
        String report = state.report();
        Assertions.assertTrue(report.contains("successCount=2"));
    }

    @Test
    void successResetsFailCount() {
        SimpleStreamState state = new SimpleStreamState(10, 1000);

        state.fail();
        state.fail();
        state.success();

        // After success(), fail count should be reset
        Assertions.assertFalse(state.isFailure());
    }

    @Test
    void reportTpsIsNonZeroAfterDelay() throws InterruptedException {
        SimpleStreamState state = new SimpleStreamState(10, 1000);

        for (int i = 0; i < 10; i++) {
            state.success();
        }
        // Wait a bit so elapsed time > 0
        Thread.sleep(100);

        String report = state.report();
        Assertions.assertTrue(report.startsWith("SimpleStreamState.report{"));
        Assertions.assertTrue(report.contains("successCount=10"));
        // TPS should be > 0 since there were successes over time
        Assertions.assertFalse(report.contains("tps=0.00"));
    }
}
