/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.web.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DateTimeUtilsTest {

    @Test
    public void timestampToMidNight() {
        long current = System.currentTimeMillis();
        Assertions.assertEquals(timestampToMidNight(current), DateTimeUtils.timestampToStartOfDay(current));
    }

    private static long timestampToMidNight(long timestamp) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(timestamp));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime().getTime();
    }
}