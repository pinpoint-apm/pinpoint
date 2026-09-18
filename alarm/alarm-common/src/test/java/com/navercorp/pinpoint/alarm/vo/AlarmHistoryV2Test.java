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
package com.navercorp.pinpoint.alarm.vo;

import com.navercorp.pinpoint.alarm.validation.AlarmValidationConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlarmHistoryV2Test {

    /**
     * A CHECK_FAILED message carries a backend's exception text, which has no length of
     * its own. Overflowing the column rolls back the state update with the insert, which
     * leaves the rule due again and failing the same way on the next tick.
     */
    @Test
    void messageIsBoundedToTheColumnSize() {
        String tooLong = "x".repeat(AlarmValidationConstants.MAX_HISTORY_MESSAGE_LENGTH + 500);

        AlarmHistoryV2 history = AlarmHistoryV2.checkFailed(1L, tooLong, "{}");

        assertEquals(AlarmValidationConstants.MAX_HISTORY_MESSAGE_LENGTH, history.getMessage().length());
        assertTrue(history.getMessage().endsWith("..."), history.getMessage());
    }

    @Test
    void messageWithinTheLimitIsKeptWhole() {
        String message = "[CHECK_FAILED] rule-1: connection refused";

        AlarmHistoryV2 history = AlarmHistoryV2.checkFailed(1L, message, "{}");

        assertEquals(message, history.getMessage());
    }
}
