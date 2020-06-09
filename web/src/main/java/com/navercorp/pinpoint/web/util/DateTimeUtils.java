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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class DateTimeUtils {

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    private DateTimeUtils() {
    }

    public static long timestampToStartOfDay(long epochMilli) {
        // Java 8 date-time: get start of day from ZonedDateTime
        // https://stackoverflow.com/questions/29143910/java-8-date-time-get-start-of-day-from-zoneddatetime
        // Get Today's date in Java at midnight time
        // https://stackoverflow.com/questions/9629636/get-todays-date-in-java-at-midnight-time/31683549
        // how to create a Java Date object of midnight today and midnight tomorrow?
        // https://stackoverflow.com/questions/6850874/how-to-create-a-java-date-object-of-midnight-today-and-midnight-tomorrow

        Instant instant = Instant.ofEpochMilli(epochMilli);
        // java 9
//        LocalDate localDate = LocalDate.ofInstant(instant, ZONE_ID);
        LocalDate localDate = instant.atZone(ZONE_ID).toLocalDate();
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZONE_ID);
        return zonedDateTime.toInstant().toEpochMilli();
    }
}
