/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.collector.grpc.config;


import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class GrpcStreamProperties {

    @DurationUnit(ChronoUnit.MILLIS)
    private Duration throttledLoggerInterval = ThrottledLogger.DEFAULT_INTERVAL;

    public GrpcStreamProperties() {
    }


    public Duration getThrottledLoggerInterval() {
        return throttledLoggerInterval;
    }

    public void setThrottledLoggerInterval(Duration throttledLoggerInterval) {
        this.throttledLoggerInterval = throttledLoggerInterval;
    }

    @Override
    public String toString() {
        return "GrpcStreamProperties{" +
                "throttledLoggerInterval=" + throttledLoggerInterval +
                '}';
    }
}
