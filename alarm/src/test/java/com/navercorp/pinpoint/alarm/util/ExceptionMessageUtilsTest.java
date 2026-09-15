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

import static com.navercorp.pinpoint.alarm.util.ExceptionMessageUtils.ruleOwnerMessage;
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

    /**
     * A check failure goes out to the rule's own channels, one of which can be a webhook at
     * an operator-supplied url. The backend's text is not written with that in mind -- here a
     * driver error carrying a connection string -- so what leaves is the message this module
     * wrote, from the exception the failure was classified on.
     */
    @Test
    void ruleOwnerMessageStopsAtTheExceptionThisModuleRaised() {
        RuntimeException failure = new IllegalStateException("evaluation failed",
                new IllegalArgumentException("unsupported metric: error_rat",
                        new SQLException("connect to jdbc:backend://user:secret@host failed")));

        assertEquals("unsupported metric: error_rat", ruleOwnerMessage(failure));
    }

    // The root cause still goes to the history context, which stays inside.
    @Test
    void rootCauseMessageKeepsGoingForTheRecordThatStaysInternal() {
        RuntimeException failure = new IllegalStateException("evaluation failed",
                new IllegalArgumentException("unsupported metric",
                        new SQLException("connect to jdbc:backend://user:secret@host failed")));

        assertEquals("connect to jdbc:backend://user:secret@host failed",
                ExceptionMessageUtils.rootCauseMessage(failure));
    }

    /**
     * Nothing in the chain is ours, so there is no message worth showing: whatever went wrong
     * is not something the rule's owner can act on, and naming the type says that much without
     * forwarding text from somewhere else.
     */
    @Test
    void aFailureWithNothingOfOursNamesOnlyItsType() {
        RuntimeException failure = new IllegalStateException("pool exhausted",
                new SQLException("connect to jdbc:backend://user:secret@host failed"));

        assertEquals("IllegalStateException", ruleOwnerMessage(failure));
    }

    // A blank message is no better than none, so the search carries on past it.
    @Test
    void aBlankMessageIsSkipped() {
        RuntimeException failure = new IllegalArgumentException("  ",
                new IllegalArgumentException("conditions must not be empty"));

        assertEquals("conditions must not be empty", ruleOwnerMessage(failure));
    }
}
