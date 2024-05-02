/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.common.hbase.async;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

public class ConcurrencyLimiterHelper implements LimiterHelper {
    private final AtomicLong counter = new AtomicLong();
    private final long maxCurrent;

    public ConcurrencyLimiterHelper(long maxCurrent) {
        this.maxCurrent = maxCurrent;
    }

    @Override
    public boolean acquire(int permits) {
        final long concurrency = counter.addAndGet(permits);
        if (concurrency > maxCurrent) {
            return false;
        }
        return true;
    }

    public long count() {
        return counter.get();
    }


    @Override
    public void release(int permits) {
        this.counter.addAndGet(-permits);
    }


    @Override
    public <R> BiConsumer<R, Throwable> release() {
        return new Release<>(counter);
    }

    private static class Release<R> implements BiConsumer<R, Throwable> {
        private final AtomicLong counter;
        public Release(AtomicLong counter) {
            this.counter = counter;
        }

        @Override
        public void accept(R result, Throwable throwable) {
            counter.decrementAndGet();
        }
    }

    @Override
    public String toString() {
        return "ConcurrencyLimiterHelper{" +
                "counter=" + counter +
                ", maxCurrent=" + maxCurrent +
                '}';
    }
}
