/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.util;

import static org.junit.Assert.*;
import static com.navercorp.pinpoint.web.util.TimeWindowSlotCentricSampler.*;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.navercorp.pinpoint.web.util.TimeWindowSampler;
import com.navercorp.pinpoint.web.util.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.web.vo.Range;

/**
 * @author hyungil.jeong
 */
public class TimeWindowSlotCentricSamplerTest {

    private static final long START_TIME_STAMP = 1234567890123L;
    private static final long ONE_SECOND = TimeUnit.SECONDS.toMillis(1);
    private static final long ONE_MINUTE = TimeUnit.MINUTES.toMillis(1);
    private static final long ONE_HOUR = TimeUnit.HOURS.toMillis(1);
    private static final long ONE_DAY = TimeUnit.DAYS.toMillis(1);

    private static final TimeWindowSampler sampler = new TimeWindowSlotCentricSampler();

    @Test
    public void getWindowSizeFor_1_second() {
        // Given
        final long from = START_TIME_STAMP;
        final long to = START_TIME_STAMP + ONE_SECOND;
        final Range range = new Range(from, to);
        // When
        final long idealWindowSize = sampler.getWindowSize(range);
        // Then
        assertWindowSizeIsIdeal(from, to, idealWindowSize);
    }

    @Test
    public void getWindowSizeFor_5_seconds() {
        // Given
        final long from = START_TIME_STAMP;
        final long to = START_TIME_STAMP + (5 * ONE_SECOND);
        final Range range = new Range(from, to);
        // When
        final long idealWindowSize = sampler.getWindowSize(range);
        // Then
        assertWindowSizeIsIdeal(from, to, idealWindowSize);
    }

    @Test
    public void getWindowSizeFor_5_minutes() {
        // Given
        final long from = START_TIME_STAMP;
        final long to = START_TIME_STAMP + (5 * ONE_MINUTE);
        final Range range = new Range(from, to);
        // When
        final long idealWindowSize = sampler.getWindowSize(range);
        // Then
        assertWindowSizeIsIdeal(from, to, idealWindowSize);
    }

    @Test
    public void getWindowSizeFor_20_minutes() {
        // Given
        final long from = START_TIME_STAMP;
        final long to = START_TIME_STAMP + (20 * ONE_MINUTE);
        final Range range = new Range(from, to);
        // When
        final long idealWindowSize = sampler.getWindowSize(range);
        // Then
        assertWindowSizeIsIdeal(from, to, idealWindowSize);
    }

    @Test
    public void getWindowSizeFor_1_hour() {
        // Given
        final long from = START_TIME_STAMP;
        final long to = START_TIME_STAMP + ONE_HOUR;
        final Range range = new Range(from, to);
        // When
        final long idealWindowSize = sampler.getWindowSize(range);
        // Then
        assertWindowSizeIsIdeal(from, to, idealWindowSize);
    }

    @Test
    public void getWindowSizeFor_3_hours() {
        // Given
        final long from = START_TIME_STAMP;
        final long to = START_TIME_STAMP + (3 * ONE_HOUR);
        final Range range = new Range(from, to);
        // When
        final long idealWindowSize = sampler.getWindowSize(range);
        // Then
        assertWindowSizeIsIdeal(from, to, idealWindowSize);
    }

    @Test
    public void getWindowSizeFor_6_hours() {
        // Given
        final long from = START_TIME_STAMP;
        final long to = START_TIME_STAMP + (6 * ONE_HOUR);
        final Range range = new Range(from, to);
        // When
        final long idealWindowSize = sampler.getWindowSize(range);
        // Then
        assertWindowSizeIsIdeal(from, to, idealWindowSize);
    }

    @Test
    public void getWindowSizeFor_12_hours() {
        // Given
        final long from = START_TIME_STAMP;
        final long to = START_TIME_STAMP + (12 * ONE_HOUR);
        final Range range = new Range(from, to);
        // When
        final long idealWindowSize = sampler.getWindowSize(range);
        // Then
        assertWindowSizeIsIdeal(from, to, idealWindowSize);
    }

    @Test
    public void getWindowSizeFor_1_day() {
        // Given
        final long from = START_TIME_STAMP;
        final long to = START_TIME_STAMP + ONE_DAY;
        final Range range = new Range(from, to);
        // When
        final long idealWindowSize = sampler.getWindowSize(range);
        // Then
        assertWindowSizeIsIdeal(from, to, idealWindowSize);
    }

    @Test
    public void getWindowSizeFor_2_days() {
        // Given
        final long from = START_TIME_STAMP;
        final long to = START_TIME_STAMP + (2 * ONE_DAY);
        final Range range = new Range(from, to);
        // When
        final long idealWindowSize = sampler.getWindowSize(range);
        // Then
        assertWindowSizeIsIdeal(from, to, idealWindowSize);
    }
    
    @Test
    public void getWindowSizeEverySecondsFor_5_years() {
        final long numSecondsPerYear = 60 * 60 * 24 * 365;
        for (long periodMs = 0; periodMs <= numSecondsPerYear * 5; periodMs += ONE_SECOND) {
            final long from = START_TIME_STAMP;
            final long to = START_TIME_STAMP + periodMs;
            final Range range = new Range(from, to);
            final long idealWindowSize = sampler.getWindowSize(range);
            assertWindowSizeIsIdeal(from, to, idealWindowSize);
        }
    }

    private void assertWindowSizeIsIdeal(final long from, final long to, final long idealWindowSize) {
        final long periodMs = to - from;
        long lowerWindowSize = idealWindowSize - DEFAULT_MINIMUM_TIMESLOT;
        if (lowerWindowSize < DEFAULT_MINIMUM_TIMESLOT) {
            lowerWindowSize = DEFAULT_MINIMUM_TIMESLOT;
        }
        long higherWindowSize = idealWindowSize + DEFAULT_MINIMUM_TIMESLOT;
        if (higherWindowSize > Long.MAX_VALUE) {
            higherWindowSize = idealWindowSize;
        }
        double numTimeslotsWithLowerWindowSize = (double)periodMs / lowerWindowSize;
        double numTimeslotsWithHigherWindowSize = (double)periodMs / higherWindowSize;
        double numTimeslotsWithIdealWindowSize = (double)periodMs / idealWindowSize;
        assertTrue(Math.abs(numTimeslotsWithIdealWindowSize - DEFAULT_IDEAL_NUM_TIMESLOTS) <= Math.abs(numTimeslotsWithLowerWindowSize - DEFAULT_IDEAL_NUM_TIMESLOTS));
        assertTrue(Math.abs(numTimeslotsWithIdealWindowSize - DEFAULT_IDEAL_NUM_TIMESLOTS) <= Math.abs(numTimeslotsWithHigherWindowSize - DEFAULT_IDEAL_NUM_TIMESLOTS));
    }

}
