package com.navercorp.pinpoint.profiler.sender.grpc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SimpleStreamStateTest {

    @Test
    void isFailure_initialState() {
        SimpleStreamState state = new SimpleStreamState(10, 1000);
        Assertions.assertFalse(state.isFailure());
    }

    @Test
    void isFailure_afterSuccess() {
        SimpleStreamState state = new SimpleStreamState(10, 1000);
        state.fail();
        state.success();
        Assertions.assertFalse(state.isFailure());
    }

    @Test
    void isFailure_belowThreshold() {
        SimpleStreamState state = new SimpleStreamState(10, 1000);
        for (int i = 0; i < 5; i++) {
            state.fail();
        }
        // failCount(5) <= limitCount(10), should not be failure
        Assertions.assertFalse(state.isFailure());
    }

    @Test
    void isFailure_countExceeded_timeNotExceeded() {
        SimpleStreamState state = new SimpleStreamState(2, 60_000);
        for (int i = 0; i < 5; i++) {
            state.fail();
        }
        // failCount(5) > limitCount(2), but time not exceeded
        Assertions.assertFalse(state.isFailure());
    }

    @Test
    void isFailure_bothExceeded() throws InterruptedException {
        SimpleStreamState state = new SimpleStreamState(2, 1);
        for (int i = 0; i < 5; i++) {
            state.fail();
        }
        Thread.sleep(10);
        // failCount(5) > limitCount(2), limitTime(1ms) exceeded after sleep
        Assertions.assertTrue(state.isFailure());
    }

    @Test
    void success_resetsState() throws InterruptedException {
        SimpleStreamState state = new SimpleStreamState(2, 1);
        for (int i = 0; i < 5; i++) {
            state.fail();
        }
        Thread.sleep(10);
        Assertions.assertTrue(state.isFailure());

        state.success();
        Assertions.assertFalse(state.isFailure());
    }

    @Test
    void isFailure_singleFail_belowCountThreshold() {
        SimpleStreamState state = new SimpleStreamState(10, 1000);
        state.fail();
        // Single fail, failCount(1) <= limitCount(10)
        Assertions.assertFalse(state.isFailure());
    }

    @Test
    void toString_containsState() {
        SimpleStreamState state = new SimpleStreamState(100, 5000);
        String str = state.toString();
        Assertions.assertTrue(str.contains("limitCount=100"));
        Assertions.assertTrue(str.contains("limitTime=5000"));
    }
}
