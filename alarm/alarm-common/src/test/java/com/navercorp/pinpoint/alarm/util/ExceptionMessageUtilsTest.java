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
package com.navercorp.pinpoint.alarm.util;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionMessageUtilsTest {

    @Test
    void rootCauseMessageReturnsDeepestCauseMessage() {
        RuntimeException failure = new IllegalStateException(
                "outer", new IllegalArgumentException("root failure"));

        assertEquals("root failure", ExceptionMessageUtils.rootCauseMessage(failure));
    }

    @Test
    void rootCauseMessageFallsBackToClassNameForNullMessage() {
        RuntimeException failure = new IllegalStateException((String) null);

        assertEquals("IllegalStateException", ExceptionMessageUtils.rootCauseMessage(failure));
    }

    @Test
    void rootCauseMessageFallsBackToClassNameForBlankMessage() {
        RuntimeException failure = new IllegalStateException("   ");

        assertEquals("IllegalStateException", ExceptionMessageUtils.rootCauseMessage(failure));
    }

    // The root cause is what the history context keeps, and it stays inside the process:
    // nothing reached by a check failure is sent to a rule's own channels any more.
    @Test
    void rootCauseMessageKeepsGoingForTheRecordThatStaysInternal() {
        RuntimeException failure = new IllegalStateException("evaluation failed",
                new IllegalArgumentException("unsupported metric",
                        new SQLException("connect to jdbc:backend://user:secret@host failed")));

        assertEquals("connect to jdbc:backend://user:secret@host failed",
                ExceptionMessageUtils.rootCauseMessage(failure));
    }

}
