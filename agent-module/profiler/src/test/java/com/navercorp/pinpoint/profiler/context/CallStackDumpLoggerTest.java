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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.common.profiler.logging.LogThrottle;
import com.navercorp.pinpoint.exception.PinpointException;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CallStackDumpLoggerTest {

    /** Counts every call like CountingTimeLogThrottle and lets the test decide which calls get through. */
    private static class ScriptedThrottle implements LogThrottle {
        private final boolean[] script;
        private int calls;

        ScriptedThrottle(boolean... script) {
            this.script = script;
        }

        @Override
        public boolean tryAcquire() {
            final boolean acquired = calls < script.length && script[calls];
            calls++;
            return acquired;
        }

        @Override
        public long getCounter() {
            return calls;
        }
    }

    /** Renders only when the warning is actually written; a suppressed dump must never touch it. */
    private static class ExplodingOnToString {
        @Override
        public String toString() {
            throw new AssertionError("rendered while suppressed");
        }
    }

    @Test
    void firstDumpIsWritten_withStackTraceAndCount() {
        Logger logger = mock(Logger.class);
        when(logger.isWarnEnabled()).thenReturn(true);
        CallStackDumpLogger dumpLogger = new CallStackDumpLogger(logger, new ScriptedThrottle(true));

        dumpLogger.dump("call stack is empty", "root", "stack");

        ArgumentCaptor<Object> exception = ArgumentCaptor.forClass(Object.class);
        verify(logger).warn(anyString(), eq(1L), eq("root"), eq("stack"), exception.capture());
        assertThat(exception.getValue()).isInstanceOf(PinpointException.class);
        assertThat(((PinpointException) exception.getValue()).getMessage()).isEqualTo("call stack is empty");
    }

    @Test
    void suppressedDumps_writeNothing_andBuildNothing() {
        Logger logger = mock(Logger.class);
        when(logger.isWarnEnabled()).thenReturn(true);
        when(logger.isDebugEnabled()).thenReturn(false);
        CallStackDumpLogger dumpLogger = new CallStackDumpLogger(logger, new ScriptedThrottle(true, false, false, true));

        dumpLogger.dump("first", "root", "stack");
        // while the throttle holds, the trace root and call stack must not even be rendered
        dumpLogger.dump("second", new ExplodingOnToString(), new ExplodingOnToString());
        dumpLogger.dump("third", new ExplodingOnToString(), new ExplodingOnToString());
        dumpLogger.dump("fourth", "root", "stack");

        // two warnings, the second carrying the total count of four dumps
        verify(logger).warn(anyString(), eq(1L), eq("root"), eq("stack"), any(PinpointException.class));
        verify(logger).warn(anyString(), eq(4L), eq("root"), eq("stack"), any(PinpointException.class));
        verify(logger, never()).debug(anyString(), any(), any());
    }

    @Test
    void suppressedDump_isTracedAtDebugOnly() {
        Logger logger = mock(Logger.class);
        when(logger.isWarnEnabled()).thenReturn(true);
        when(logger.isDebugEnabled()).thenReturn(true);
        CallStackDumpLogger dumpLogger = new CallStackDumpLogger(logger, new ScriptedThrottle(false));

        dumpLogger.dump("not matched stack id", new ExplodingOnToString(), new ExplodingOnToString());

        verify(logger, never()).warn(anyString(), any(), any(), any(), any());
        verify(logger).debug(anyString(), eq(1L), eq("not matched stack id"));
    }

    @Test
    void warnDisabled_doesNotConsumeTheThrottle() {
        Logger logger = mock(Logger.class);
        when(logger.isWarnEnabled()).thenReturn(false);
        ScriptedThrottle throttle = new ScriptedThrottle(true);
        CallStackDumpLogger dumpLogger = new CallStackDumpLogger(logger, throttle);

        dumpLogger.dump("call stack is empty", new ExplodingOnToString(), new ExplodingOnToString());

        assertThat(throttle.getCounter()).isZero();
        verify(logger, never()).warn(anyString(), any(), any(), any(), any());
    }
}
