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

package com.navercorp.pinpoint.common.server.util.time;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author emeroad
 */
public class RangeTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void testCreate() {
        Range range1 = Range.between(0, 0);
        Range range2 = Range.between(0, 1);

        Assertions.assertThrows(Exception.class, () -> {
            Range range3 = Range.between(0, -1);
        });
    }

    @Test
    public void testRange() {
        Range range1 = Range.between(0, 0);
        Assertions.assertEquals(0, range1.durationMillis());

        Range range2 = Range.between(0, 1);
        Assertions.assertEquals(1, range2.durationMillis());
    }

    @Test
    public void testRange_String() {
        Range range1 = Range.between(0, 0);
        assertThat(range1.toString()).contains(" = ");

        Range range2 = Range.between(0, 1);
        assertThat(range2.toString()).contains(" < ");

        Range range3 = Range.newUncheckedRange(1, 0);
        assertThat(range3.toString()).contains(" > ");
    }

    @Test
    public void format() {
        Instant now = Instant.now();
        Range range = Range.between(now, now.plusSeconds(100));
        logger.debug("{}", range);
    }

    @Test
    public void truncatedTo() {
        Instant now = Instant.now();
        Instant truncated = now.truncatedTo(ChronoUnit.MILLIS);
        Instant millis = Instant.ofEpochMilli(now.toEpochMilli());

        Assertions.assertEquals(millis, truncated);
    }
}
