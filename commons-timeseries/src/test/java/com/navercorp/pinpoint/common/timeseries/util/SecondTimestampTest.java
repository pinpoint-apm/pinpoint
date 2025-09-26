/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.timeseries.util;

import com.navercorp.pinpoint.common.timeseries.window.DefaultTimeSlot;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SecondTimestampTest {
    TimeSlot secSlot = new DefaultTimeSlot(1000);

    @Test
    void convertSecondTimestamp() {
        long timestamp = System.currentTimeMillis();

        long timeSlot = secSlot.getTimeSlot(timestamp);


        int secondTimestamp = SecondTimestamp.convertSecondTimestamp(timestamp);
        long original = SecondTimestamp.restoreSecondTimestamp(secondTimestamp);

        Assertions.assertEquals(timeSlot, original);

    }

    @Test
    void reverseTimeSecondTimestamp() {
        long timestamp = System.currentTimeMillis();

        long timeSlot = secSlot.getTimeSlot(timestamp);

        int secondTimestamp = SecondTimestamp.convertSecondTimestamp(timestamp);

        int reverseTimeStamp = IntInverter.invert(secondTimestamp);
        int recover = IntInverter.restore(reverseTimeStamp);

        long original = SecondTimestamp.restoreSecondTimestamp(recover);

        Assertions.assertEquals(timeSlot, original);
    }
}