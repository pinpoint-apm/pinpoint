/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.util.timewindow;

import org.springframework.util.Assert;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class TimePrecision {
    private final TimeUnit timeUnit;
    private final long timeSize;

    TimePrecision(TimeUnit timeUnit, long timeSize) {
        this.timeUnit = Objects.requireNonNull(timeUnit, "timeUnit");
        Assert.isTrue(timeSize > 0, "timeSize must be greater than 0");
        this.timeSize = timeSize;
    }

    public static TimePrecision newTimePrecision(TimeUnit timeUnit, long timeSize) {
        return new TimePrecision(timeUnit, timeSize);
    }


    public String getTimeUnit() {
        return timeUnit.name();
    }

    public long getTimeSize() {
        return timeSize;
    }

    public long getInterval() {
        return timeUnit.toMillis(timeSize);
    }

    @Override
    public String toString() {
        return "TimePrecision{" +
                "timeUnit=" + timeUnit.name() +
                ", timeSize=" + timeSize +
                '}';
    }
}
