package com.navercorp.pinpoint.profiler.context.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author intr3p1d
 */
class ExceptionRecordingStateTest {

    @Test
    void testNew() {
        Throwable clean = null;
        Throwable newException = new RuntimeException("New exception");

        ExceptionRecordingState actual = ExceptionRecordingState.stateOf(clean, newException);

        Assertions.assertEquals(ExceptionRecordingState.NEW, actual);
    }

    @Test
    void testContinued() {
        Throwable firstOne = new RuntimeException("level 1 error");
        Throwable secondOne = new RuntimeException("level 2 error", firstOne);

        ExceptionRecordingState actual1 = ExceptionRecordingState.stateOf(firstOne, secondOne);

        Assertions.assertEquals(ExceptionRecordingState.CONTINUED, actual1);

        ExceptionRecordingState actual2 = ExceptionRecordingState.stateOf(firstOne, firstOne);
        Assertions.assertEquals(ExceptionRecordingState.CONTINUED, actual2);

        Throwable thirdOne = new RuntimeException("level 3 error", secondOne);

        ExceptionRecordingState actual3 = ExceptionRecordingState.stateOf(firstOne, thirdOne);
        Assertions.assertEquals(ExceptionRecordingState.CONTINUED, actual3);

        Throwable fourthOne = new RuntimeException("level 4 error", thirdOne);

        ExceptionRecordingState actual4 = ExceptionRecordingState.stateOf(secondOne, fourthOne);
        Assertions.assertEquals(ExceptionRecordingState.CONTINUED, actual4);


        Throwable anotherNewChain = new RuntimeException("not chained exception");

        ExceptionRecordingState actual5 = ExceptionRecordingState.stateOf(fourthOne, anotherNewChain);
        Assertions.assertEquals(ExceptionRecordingState.NEW, actual5);
    }

    @Test
    void testClean() {
        Throwable clean = null;

        ExceptionRecordingState actual1 = ExceptionRecordingState.stateOf(clean, clean);
        Assertions.assertEquals(ExceptionRecordingState.CLEAN, actual1);

        Throwable newException = new RuntimeException("New exception");

        ExceptionRecordingState actual2 = ExceptionRecordingState.stateOf(newException, clean);
        Assertions.assertEquals(ExceptionRecordingState.CLEAN, actual2);
    }

}