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

import com.navercorp.pinpoint.common.profiler.logging.CountingTimeLogThrottle;
import com.navercorp.pinpoint.common.profiler.logging.LogThrottle;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.exception.PinpointException;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Logs the "Corrupted call stack found" warning of a trace at most once per interval.
 *
 * <p>A corrupted call stack is usually not a one-off: once a trace's stack is out of balance every
 * following interceptor reports it again, and each report captures a stack trace, renders the trace
 * root and the call stack and goes through every appender. One production transaction produced about
 * a thousand of them. The throttle is shared by all traces of a class (one process-wide cap per trace
 * type), and while it holds, nothing is built - not even the exception that carries the stack trace.
 * The count of all reports so far is written into the warning that does get through.
 */
final class CallStackDumpLogger {

    private static final long DEFAULT_INTERVAL_MILLIS = ThrottledLogger.DEFAULT_INTERVAL.toMillis();

    private final Logger logger;
    private final LogThrottle throttle;

    static CallStackDumpLogger of(Logger logger) {
        return new CallStackDumpLogger(logger, new CountingTimeLogThrottle(DEFAULT_INTERVAL_MILLIS));
    }

    CallStackDumpLogger(Logger logger, LogThrottle throttle) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.throttle = Objects.requireNonNull(throttle, "throttle");
    }

    /**
     * @param caused    why the call stack is considered corrupted; becomes the exception message
     * @param traceRoot the trace root, rendered only when the warning is written
     * @param callStack the call stack, rendered only when the warning is written
     */
    void dump(String caused, Object traceRoot, Object callStack) {
        if (!logger.isWarnEnabled()) {
            return;
        }
        if (!throttle.tryAcquire()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Corrupted call stack found (suppressed, count={}) {}", throttle.getCounter(), caused);
            }
            return;
        }
        final PinpointException exception = new PinpointException(caused);
        logger.warn("Corrupted call stack found (count={}) TraceRoot:{}, CallStack:{}", throttle.getCounter(), traceRoot, callStack, exception);
    }
}
