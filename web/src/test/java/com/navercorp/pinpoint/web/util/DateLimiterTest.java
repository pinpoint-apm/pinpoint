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

import com.navercorp.pinpoint.web.util.DateLimiter;
import com.navercorp.pinpoint.web.util.Limiter;
import com.navercorp.pinpoint.web.vo.Range;

import org.junit.Assert;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
public class DateLimiterTest {

    @Test
    public void check() {
        Limiter limiter = new DateLimiter(2);

        limiter.limit(0, TimeUnit.DAYS.toMillis(2));

        long time = 1000;
        limiter.limit(time, time + TimeUnit.DAYS.toMillis(2));

        limiter.limit(TimeUnit.DAYS.toMillis(2), TimeUnit.DAYS.toMillis(2));
    }

    @Test
    public void checkRange() {
        Limiter limiter = new DateLimiter(2);

        limiter.limit(new Range(0, TimeUnit.DAYS.toMillis(2)));

        long time = 1000;
        limiter.limit(new Range(time, time + TimeUnit.DAYS.toMillis(2)));

        limiter.limit(new Range(TimeUnit.DAYS.toMillis(2), TimeUnit.DAYS.toMillis(2)));
    }

    @Test
    public void checkFail() {
        Limiter limiter = new DateLimiter(2);
        try {
            limiter.limit(0, TimeUnit.DAYS.toMillis(2) + 1);
            Assert.fail();
        } catch (Exception ignored) {
        }

        try {
            limiter.limit(TimeUnit.DAYS.toMillis(2), 0);
            Assert.fail();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void checkRangeFail() {
        Limiter limiter = new DateLimiter(2);
        try {
            limiter.limit(new Range(0, TimeUnit.DAYS.toMillis(2) + 1));
            Assert.fail();
        } catch (Exception ignored) {
        }

        try {
            limiter.limit(new Range(TimeUnit.DAYS.toMillis(2), 0));
            Assert.fail();
        } catch (Exception ignored) {
        }
    }

}
