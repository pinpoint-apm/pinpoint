/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.collector.monitor.micrometer;

import io.micrometer.core.instrument.Counter;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author intr3p1d
 */
public class CounterRejectedExecutionHandler implements RejectedExecutionHandler {

    private final Counter counter;

    public CounterRejectedExecutionHandler(Counter counter) {
        this.counter = Objects.requireNonNull(counter, "counter");
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        counter.increment();
    }
}
