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

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * @author emeroad
 */
@Component
public class DateLimiter implements Limiter {

    private final Duration limitDay;

    public DateLimiter() {
        this(Duration.ofDays(2));
    }

    public DateLimiter(Duration limitDay) {
        this.limitDay = Objects.requireNonNull(limitDay, "limitDay");
    }

    @Override
    public void limit(Instant from, Instant to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");

        Duration duration = Duration.between(from, to);
        if (duration.isNegative()) {
            throw new  IllegalArgumentException("to - from < 0 from:" + from + " to:" + to);
        }

        if (limitDay.toMillis() < duration.toMillis()) {
            throw new IllegalArgumentException("limitDay:"+ limitDay + " from:" + from + " to:" + to);
        }
    }

    @Override
    public void limit(Range range) {
        Objects.requireNonNull(range, "range");
        limit(range.getFromInstant(), range.getToInstant());
    }
}
