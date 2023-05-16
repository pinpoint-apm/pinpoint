/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class TimeSlotTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final TimeSlot timeSlot = new DefaultTimeSlot();

    @Test
    public void testGetTimeSlot() throws Exception {
        long currentTime = System.currentTimeMillis();
        long timeSlot = this.timeSlot.getTimeSlot(currentTime);

        logger.debug("{} currentTime ", currentTime);
        logger.debug("{} timeSlot", timeSlot);
        Assertions.assertTrue(currentTime >= timeSlot);
    }

    @Test
    public void testSlotTime1() throws Exception {
        int slotTest = 60 * 1000;
        long timeSlot = this.timeSlot.getTimeSlot(slotTest);

        logger.debug("{} slotTest ", slotTest);
        logger.debug("{} timeSlot", timeSlot);
        Assertions.assertEquals(slotTest, timeSlot);
    }

    @Test
    public void testSlotTime2() throws Exception {
        int sourceTest = 60 * 1000;
        int slotTest = sourceTest + 1;
        long timeSlot = this.timeSlot.getTimeSlot(slotTest);

        logger.debug("{} slotTest ", slotTest);
        logger.debug("{} timeSlot", timeSlot);
        Assertions.assertEquals(sourceTest, timeSlot);
    }
}
