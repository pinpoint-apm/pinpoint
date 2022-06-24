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

import com.navercorp.pinpoint.common.server.util.time.Range;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

/**
 * @author emeroad
 */
public class DateLimiterTest {

    @Test
    public void check() {
        Limiter limiter = new DateLimiter(Duration.ofDays(2));

        limiter.limit(Instant.EPOCH, ofDays(2));

        Instant time = Instant.ofEpochMilli(1000);
        limiter.limit(time, time.plus(Duration.ofDays(2)));

        limiter.limit(ofDays(2), ofDays(2));
    }

    @Test
    public void checkRange() {
        Limiter limiter = new DateLimiter(Duration.ofDays(2));

        limiter.limit(Range.between(Instant.EPOCH, ofDays(2)));

        Instant time = Instant.ofEpochMilli(1000);
        limiter.limit(Range.between(time, time.plus(Duration.ofDays(2))));

        limiter.limit(Range.between(ofDays(2), ofDays(2)));
    }

    @Test
    public void checkFail() {
        Limiter limiter = new DateLimiter(Duration.ofDays(2));
        try {
            limiter.limit(Instant.EPOCH, ofDays(2).plusMillis(1));
            Assertions.fail();
        } catch (Exception ignored) {
        }

        try {
            limiter.limit(ofDays(2), Instant.EPOCH);
            Assertions.fail();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void checkRangeFail() {
        Limiter limiter = new DateLimiter(Duration.ofDays(2));
        try {
            limiter.limit(Range.between(Instant.EPOCH, ofDays(2).plusMillis(1)));
            Assertions.fail();
        } catch (Exception ignored) {
        }

        try {
            limiter.limit(Range.between(ofDays(2), Instant.EPOCH));
            Assertions.fail();
        } catch (Exception ignored) {
        }
    }

    private Instant ofDays(long days) {
        return Instant.EPOCH.plus(Duration.ofDays(days));
    }
}
