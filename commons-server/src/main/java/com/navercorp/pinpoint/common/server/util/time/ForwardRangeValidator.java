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

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * @author emeroad
 */
public class ForwardRangeValidator implements RangeValidator {

    private final Duration limitDay;

    public ForwardRangeValidator(Duration limitDay) {
        this.limitDay = Objects.requireNonNull(limitDay, "limitDay");
    }

    @Override
    public void validate(Instant from, Instant to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");

        Duration duration = Duration.between(from, to);
        if (duration.isNegative()) {
            throw new IllegalArgumentException("to - from < 0 from:" + from + " to:" + to);
        }

        if (limitDay.minus(duration).isNegative()) {
            throw new IllegalArgumentException("limitDay:"+ limitDay + " from:" + from + " to:" + to);
        }
    }

    @Override
    public void validate(Range range) {
        Objects.requireNonNull(range, "range");
        validate(range.getFromInstant(), range.getToInstant());
    }
}
