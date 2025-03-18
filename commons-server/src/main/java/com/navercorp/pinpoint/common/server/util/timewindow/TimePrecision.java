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

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class TimePrecision {
    private final TimeUnit timeUnit;
    private final int timeSize;

    private TimePrecision(TimeUnit timeUnit, Integer timeSize) {
        this.timeUnit = Objects.requireNonNull(timeUnit, "timeUnit");
        this.timeSize = Objects.requireNonNull(timeSize, "timeSize");
    }

    public static TimePrecision newTimePrecision(TimeUnit timeUnit, Integer timeSize) {
        final TimePrecision timePrecision = new TimePrecision(timeUnit, timeSize);
        validate(timePrecision);
        return timePrecision;
    }

    public static void validate(TimePrecision timePrecision) {
        if (timePrecision.timeSize < 1) {
            throw new IllegalArgumentException("invalid timePrecision:" + timePrecision);
        }
    }

    public String getTimeUnit() {
        return timeUnit.name();
    }

    public int getTimeSize() {
        return timeSize;
    }

    public long getInterval() {
        return timeUnit.toMillis(timeSize);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TimePrecision{");
        sb.append("timeUnit=").append(timeUnit.name());
        sb.append(", timeSize=").append(timeSize);
        sb.append('}');
        return sb.toString();
    }
}
